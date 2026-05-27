package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.converter.DeductionConverter;
import com.supplier.settlement.dto.DeductionActionDTO;
import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.dto.ReconciliationDetailCreateDTO;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.enums.DeductionStatusEnum;
import com.supplier.settlement.mapper.DeductionMapper;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.query.DeductionQuery;
import com.supplier.settlement.service.DeductionService;
import com.supplier.settlement.service.ReconciliationDetailService;
import com.supplier.settlement.vo.DeductionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeductionServiceImpl implements DeductionService {

    private final DeductionMapper deductionMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final ReconciliationMapper reconciliationMapper;
    private final ReconciliationDetailService reconciliationDetailService;

    @Override
    public PageResult<DeductionVO> page(DeductionQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Deduction> wrapper = new LambdaQueryWrapper<Deduction>()
                .eq(supplierId != null, Deduction::getSupplierId, supplierId)
                .eq(query.getDeductionStatus() != null, Deduction::getDeductionStatus, query.getDeductionStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Deduction::getDeductionNo, query.getKeyword())
                        .or()
                        .like(Deduction::getDeductionReason, query.getKeyword()))
                .orderByDesc(Deduction::getCreateTime);
        Page<Deduction> page = deductionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(DeductionConverter::toVO));
    }

    @Override
    public DeductionVO getDetail(Long id) {
        Deduction entity = getWithDataScope(id);
        return DeductionConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeductionCreateDTO dto) {
        Long count = deductionMapper.selectCount(new LambdaQueryWrapper<Deduction>()
                .eq(Deduction::getDeductionNo, dto.getDeductionNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "扣款单号已存在");
        }
        Deduction entity = DeductionConverter.toEntity(dto);
        deductionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id, DeductionActionDTO dto) {
        dto = normalizeAction(dto);
        Deduction entity = getWithDataScope(id);
        if (!Integer.valueOf(DeductionStatusEnum.DRAFT.getCode()).equals(entity.getDeductionStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可以提交");
        }
        entity.setDeductionStatus(DeductionStatusEnum.SUBMITTED.getCode());
        deductionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id, DeductionActionDTO dto) {
        dto = normalizeAction(dto);
        Deduction entity = getWithDataScope(id);
        if (!Integer.valueOf(DeductionStatusEnum.SUBMITTED.getCode()).equals(entity.getDeductionStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可以确认");
        }
        entity.setDeductionStatus(DeductionStatusEnum.CONFIRMED.getCode());
        deductionMapper.updateById(entity);

        // 联动8: 扣款确认 → 纳入对账汇总
        includeInReconciliation(entity);

        domainEventPublisher.publish("supplier.settlement", "settlement.deduction.confirmed",
                DomainEvent.builder()
                        .eventType("settlement.deduction.confirmed")
                        .data(Map.of("businessId", entity.getId(), "businessNo", entity.getDeductionNo(),
                                "supplierId", entity.getSupplierId(), "deductionAmount", entity.getDeductionAmount()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispute(Long id, DeductionActionDTO dto) {
        Deduction entity = getWithDataScope(id);
        if (!Integer.valueOf(DeductionStatusEnum.SUBMITTED.getCode()).equals(entity.getDeductionStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可以提异议");
        }
        entity.setDeductionStatus(DeductionStatusEnum.DISPUTED.getCode());
        deductionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void book(Long id, DeductionActionDTO dto) {
        dto = normalizeAction(dto);
        Deduction entity = getWithDataScope(id);
        if (!Integer.valueOf(DeductionStatusEnum.CONFIRMED.getCode()).equals(entity.getDeductionStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已确认状态可以入账");
        }
        entity.setDeductionStatus(DeductionStatusEnum.BOOKED.getCode());
        deductionMapper.updateById(entity);
    }

    // ---- 联动8: 扣款确认 → 纳入对账汇总 ----
    private void includeInReconciliation(Deduction deduction) {
        if (deduction.getSupplierId() == null) {
            return;
        }
        // 查找当前对账周期内"对账中"(status=1)的对账单
        Reconciliation reconciliation = reconciliationMapper.selectOne(
                new LambdaQueryWrapper<Reconciliation>()
                        .eq(Reconciliation::getSupplierId, deduction.getSupplierId())
                        .eq(Reconciliation::getReconStatus, 1) // 对账中（已发送）
                        .orderByDesc(Reconciliation::getCreateTime)
                        .last("LIMIT 1"));

        if (reconciliation == null) {
            return;
        }

        // 在对账单中追加扣款明细行（负数）
        ReconciliationDetailCreateDTO detailDto = new ReconciliationDetailCreateDTO();
        detailDto.setReconId(reconciliation.getId());
        detailDto.setMaterialName("扣款-" + deduction.getDeductionNo());
        detailDto.setQuantity(java.math.BigDecimal.ONE);
        detailDto.setOrderAmount(deduction.getDeductionAmount());
        detailDto.setConfirmedAmount(deduction.getDeductionAmount() != null
                ? deduction.getDeductionAmount().negate() : java.math.BigDecimal.ZERO);
        detailDto.setDiffReason(deduction.getDeductionReason());
        detailDto.setConfirmStatus(0);
        detailDto.setRemark("扣款确认自动纳入对账, 扣款单号: " + deduction.getDeductionNo());
        reconciliationDetailService.create(detailDto);

        // 更新对账单总金额
        if (deduction.getDeductionAmount() != null) {
            reconciliation.setTotalAmount(
                    (reconciliation.getTotalAmount() != null ? reconciliation.getTotalAmount() : BigDecimal.ZERO)
                            .subtract(deduction.getDeductionAmount()));
            reconciliationMapper.updateById(reconciliation);
        }

        // 回写对账单ID到扣款单
        deduction.setReconId(reconciliation.getId());
        deductionMapper.updateById(deduction);
    }

    private Deduction getWithDataScope(Long id) {
        Deduction entity = deductionMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !entity.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return entity;
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

    private DeductionActionDTO normalizeAction(DeductionActionDTO dto) {
        return dto == null ? new DeductionActionDTO() : dto;
    }
}