package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.service.BizStatusTrackService;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.ReconciliationConfirmDTO;
import com.supplier.settlement.dto.ReconciliationCreateDTO;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.query.ReconciliationQuery;
import com.supplier.settlement.service.ReconciliationService;
import com.supplier.settlement.vo.ReconciliationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {
    private final ReconciliationMapper reconciliationMapper;
    private final PortalTodoService portalTodoService;
    private final MessageNoticeService messageNoticeService;
    private final DomainEventPublisher domainEventPublisher;
    private final BizStatusTrackService bizStatusTrackService;

    @Override
    public PageResult<ReconciliationVO> page(ReconciliationQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Reconciliation> wrapper = new LambdaQueryWrapper<Reconciliation>()
                .eq(supplierId != null, Reconciliation::getSupplierId, supplierId)
                .eq(StringUtils.hasText(query.getReconPeriod()), Reconciliation::getReconPeriod, query.getReconPeriod())
                .eq(query.getReconStatus() != null, Reconciliation::getReconStatus, query.getReconStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w.like(Reconciliation::getReconNo, query.getKeyword()).or().like(Reconciliation::getSupplierName, query.getKeyword()))
                .orderByDesc(Reconciliation::getCreateTime);
        Page<Reconciliation> page = reconciliationMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public ReconciliationVO getDetail(Long id) {
        return toVO(getWithScope(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReconciliationCreateDTO dto) {
        if (reconciliationMapper.selectCount(new LambdaQueryWrapper<Reconciliation>().eq(Reconciliation::getReconNo, dto.getReconNo())) > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "对账单号已存在");
        }
        Reconciliation entity = new Reconciliation();
        entity.setReconNo(dto.getReconNo());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setReconPeriod(dto.getReconPeriod());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setConfirmedAmount(BigDecimal.ZERO);
        entity.setDiffAmount(BigDecimal.ZERO);
        entity.setReconStatus(0);
        entity.setRemark(dto.getRemark());
        reconciliationMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "对账", businessType = "reconciliation", action = "对账单发送", businessIdExpr = "#id")
    public void send(Long id) {
        Reconciliation reconciliation = getWithScope(id);
        if (!Integer.valueOf(0).equals(reconciliation.getReconStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待对账单可以发送");
        }
        reconciliation.setReconStatus(1);
        reconciliation.setSendTime(LocalDateTime.now());
        reconciliationMapper.updateById(reconciliation);
        bizStatusTrackService.writeTrack("reconciliation", reconciliation.getId(), 0, 1, "发送对账单");
        // 为供应商创建对账待办和通知
        if (reconciliation.getSupplierId() != null) {
            createSupplierTodo(reconciliation);
            createSupplierMessage(reconciliation);
        }
        domainEventPublisher.publish("supplier.settlement", "settlement.reconciliation.sent",
                DomainEvent.builder()
                        .eventType("settlement.reconciliation.sent")
                        .data(Map.of("businessId", reconciliation.getId(), "businessNo", reconciliation.getReconNo(),
                                "supplierId", reconciliation.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "对账", businessType = "reconciliation", action = "对账确认", businessIdExpr = "#id")
    public void confirm(Long id, ReconciliationConfirmDTO dto) {
        Reconciliation reconciliation = getWithScope(id);
        if (!Integer.valueOf(1).equals(reconciliation.getReconStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有对账中单据可以确认");
        }
        reconciliation.setConfirmedAmount(dto.getConfirmedAmount());
        reconciliation.setDiffAmount(dto.getDiffAmount());
        reconciliation.setReconStatus(Boolean.TRUE.equals(dto.getDisputed()) ? 3 : 2);
        reconciliation.setConfirmTime(LocalDateTime.now());
        reconciliation.setConfirmBy(SecurityUtils.getUserId());
        reconciliation.setConfirmRemark(dto.getConfirmRemark());
        reconciliationMapper.updateById(reconciliation);
        bizStatusTrackService.writeTrack("reconciliation", reconciliation.getId(), 1, reconciliation.getReconStatus(),
                Boolean.TRUE.equals(dto.getDisputed()) ? "对账有异议" : (StringUtils.hasText(dto.getConfirmRemark()) ? dto.getConfirmRemark() : "确认对账单"));
        domainEventPublisher.publish("supplier.settlement", "settlement.reconciliation.confirmed",
                DomainEvent.builder()
                        .eventType("settlement.reconciliation.confirmed")
                        .data(Map.of("businessId", reconciliation.getId(), "businessNo", reconciliation.getReconNo(),
                                "supplierId", reconciliation.getSupplierId(), "disputed", dto.getDisputed()))
                        .build());
    }

    private void createSupplierTodo(Reconciliation reconciliation) {
        PortalTodoCreateDTO todoDto = new PortalTodoCreateDTO();
        todoDto.setSupplierId(reconciliation.getSupplierId());
        todoDto.setTodoType("recon_confirm");
        todoDto.setBusinessType("reconciliation");
        todoDto.setBusinessId(reconciliation.getId());
        todoDto.setBusinessNo(reconciliation.getReconNo());
        todoDto.setTitle("待确认对账单" + reconciliation.getReconNo());
        todoDto.setDueTime(LocalDateTime.now().plusDays(7));
        portalTodoService.create(todoDto);
    }

    private void createSupplierMessage(Reconciliation reconciliation) {
        MessageNoticeCreateDTO msgDto = new MessageNoticeCreateDTO();
        msgDto.setReceiverSupplierId(reconciliation.getSupplierId());
        msgDto.setChannel(1);
        msgDto.setTitle("新对账单待确认");
        msgDto.setContent("对账单" + reconciliation.getReconNo() + "（周期" + reconciliation.getReconPeriod() + "）已发送，金额￥" + reconciliation.getTotalAmount() + "，请及时确认。");
        msgDto.setBusinessType("reconciliation");
        msgDto.setBusinessId(reconciliation.getId());
        messageNoticeService.create(msgDto);
    }

    private Reconciliation getWithScope(Long id) {
        Reconciliation reconciliation = reconciliationMapper.selectById(id);
        if (reconciliation == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !reconciliation.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return reconciliation;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null || (querySupplierId != null && !supplierId.equals(querySupplierId))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }

    private ReconciliationVO toVO(Reconciliation e) {
        ReconciliationVO vo = new ReconciliationVO();
        vo.setId(e.getId()); vo.setReconNo(e.getReconNo()); vo.setSupplierId(e.getSupplierId()); vo.setSupplierName(e.getSupplierName()); vo.setReconPeriod(e.getReconPeriod()); vo.setStartDate(e.getStartDate()); vo.setEndDate(e.getEndDate()); vo.setTotalAmount(e.getTotalAmount()); vo.setConfirmedAmount(e.getConfirmedAmount()); vo.setDiffAmount(e.getDiffAmount()); vo.setReconStatus(e.getReconStatus()); vo.setSendTime(e.getSendTime()); vo.setConfirmTime(e.getConfirmTime()); vo.setConfirmRemark(e.getConfirmRemark()); vo.setRemark(e.getRemark());
        return vo;
    }
}