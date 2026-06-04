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
import com.supplier.settlement.entity.ReconciliationDetail;
import com.supplier.settlement.entity.ThreeWayMatch;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.mapper.ReconciliationDetailMapper;
import com.supplier.settlement.mapper.ThreeWayMatchMapper;
import com.supplier.settlement.mapper.InvoiceMapper;
import com.supplier.settlement.query.ReconciliationQuery;
import com.supplier.settlement.service.ReconciliationService;
import com.supplier.settlement.vo.ReconciliationDetailVO;
import com.supplier.settlement.vo.ReconciliationVO;
import com.supplier.settlement.vo.ThreeWayMatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {
    private final ReconciliationMapper reconciliationMapper;
    private final ReconciliationDetailMapper reconciliationDetailMapper;
    private final ThreeWayMatchMapper threeWayMatchMapper;
    private final InvoiceMapper invoiceMapper;
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

    @Override
    public List<ReconciliationDetailVO> getLines(Long id) {
        getWithScope(id);
        List<ReconciliationDetail> details = reconciliationDetailMapper.selectList(
                new LambdaQueryWrapper<ReconciliationDetail>().eq(ReconciliationDetail::getReconId, id));
        List<ReconciliationDetailVO> vos = new ArrayList<>();
        for (ReconciliationDetail d : details) {
            ReconciliationDetailVO vo = new ReconciliationDetailVO();
            vo.setId(d.getId());
            vo.setReconId(d.getReconId());
            vo.setOrderId(d.getOrderId());
            vo.setOrderNo(d.getOrderNo());
            vo.setDeliveryId(d.getDeliveryId());
            vo.setDeliveryNo(d.getDeliveryNo());
            vo.setMaterialCode(d.getMaterialCode());
            vo.setMaterialName(d.getMaterialName());
            vo.setQuantity(d.getQuantity());
            vo.setUnitPrice(d.getUnitPrice());
            vo.setOrderAmount(d.getOrderAmount());
            vo.setConfirmedAmount(d.getConfirmedAmount());
            vo.setDiffAmount(d.getDiffAmount());
            vo.setDiffReason(d.getDiffReason());
            vo.setConfirmStatus(d.getConfirmStatus());
            vo.setConfirmTime(d.getConfirmTime());
            vo.setConfirmRemark(d.getConfirmRemark());
            vo.setRemark(d.getRemark());
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "对账", businessType = "reconciliation", action = "冻结对账单", businessIdExpr = "#id")
    public void freeze(Long id, Map<String, Object> data) {
        Reconciliation reconciliation = getWithScope(id);
        if (Integer.valueOf(2).equals(reconciliation.getReconStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "已确认的对账单不能冻结");
        }
        reconciliation.setReconStatus(4);
        if (data != null && data.get("remark") != null) {
            reconciliation.setRemark(data.get("remark").toString());
        }
        reconciliationMapper.updateById(reconciliation);
        bizStatusTrackService.writeTrack("reconciliation", reconciliation.getId(), reconciliation.getReconStatus(), 4, "冻结对账单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "对账", businessType = "reconciliation", action = "解冻对账单", businessIdExpr = "#id")
    public void unfreeze(Long id, Map<String, Object> data) {
        Reconciliation reconciliation = getWithScope(id);
        if (!Integer.valueOf(4).equals(reconciliation.getReconStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有冻结状态的对账单可以解冻");
        }
        reconciliation.setReconStatus(1);
        if (data != null && data.get("remark") != null) {
            reconciliation.setRemark(data.get("remark").toString());
        }
        reconciliationMapper.updateById(reconciliation);
        bizStatusTrackService.writeTrack("reconciliation", reconciliation.getId(), 4, 1, "解冻对账单");
    }

    @Override
    public List<ThreeWayMatchVO> getThreeWayMatch(Long id) {
        getWithScope(id);
        List<ThreeWayMatch> matches = threeWayMatchMapper.selectList(
                new LambdaQueryWrapper<ThreeWayMatch>().eq(ThreeWayMatch::getReconId, id));
        List<ThreeWayMatchVO> vos = new ArrayList<>();
        for (ThreeWayMatch m : matches) {
            ThreeWayMatchVO vo = new ThreeWayMatchVO();
            vo.setId(m.getId());
            vo.setOrderId(m.getOrderId());
            vo.setOrderNo(m.getOrderNo());
            vo.setOrderAmount(m.getOrderAmount());
            vo.setReceiptId(m.getReceiptId());
            vo.setReceiptAmount(m.getReceiptAmount());
            vo.setInvoiceId(m.getInvoiceId());
            vo.setInvoiceNo(m.getInvoiceNo());
            vo.setInvoiceAmount(m.getInvoiceAmount());
            vo.setSupplierId(m.getSupplierId());
            vo.setMatchResult(m.getMatchResult());
            vo.setDiffAmount(m.getDiffAmount());
            vo.setDiffReason(m.getDiffReason());
            vo.setMatchTime(m.getMatchTime());
            vo.setRemark(m.getRemark());
            if (m.getMatchResult() != null) {
                vo.setMatchResultDesc(switch (m.getMatchResult()) {
                    case 0 -> "完全匹配";
                    case 1 -> "部分匹配";
                    case 2 -> "不匹配";
                    default -> "未知";
                });
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Map<String, Object> getInvoicableAmount(Long reconId) {
        getWithScope(reconId);
        List<Invoice> invoices = invoiceMapper.selectList(
                new LambdaQueryWrapper<Invoice>().eq(Invoice::getReconId, reconId));
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal invoicedAmount = BigDecimal.ZERO;
        for (Invoice inv : invoices) {
            totalAmount = totalAmount.add(inv.getInvoiceAmount() != null ? inv.getInvoiceAmount() : BigDecimal.ZERO);
            if (inv.getInvoiceStatus() != null && inv.getInvoiceStatus() != 5) {
                invoicedAmount = invoicedAmount.add(inv.getInvoiceAmount() != null ? inv.getInvoiceAmount() : BigDecimal.ZERO);
            }
        }
        Reconciliation recon = reconciliationMapper.selectById(reconId);
        BigDecimal reconTotal = recon != null && recon.getTotalAmount() != null ? recon.getTotalAmount() : BigDecimal.ZERO;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("invoicableAmount", reconTotal.subtract(invoicedAmount));
        result.put("totalAmount", reconTotal);
        result.put("invoicedAmount", invoicedAmount);
        return result;
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