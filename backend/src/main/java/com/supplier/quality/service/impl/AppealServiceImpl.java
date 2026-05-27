package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.quality.dto.AppealCreateDTO;
import com.supplier.quality.dto.AppealReviewDTO;
import com.supplier.quality.entity.Appeal;
import com.supplier.quality.mapper.AppealMapper;
import com.supplier.quality.query.AppealQuery;
import com.supplier.quality.service.AppealService;
import com.supplier.quality.vo.AppealVO;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.mapper.DeductionMapper;
import com.supplier.settlement.mapper.ReconciliationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 质检申诉服务实现（联动9: 质检申诉 → 申诉通过后关联扣款调整）
 * <p>
 * 跨模块联动: quality → settlement
 * 申诉审核通过后自动调整关联扣款单的金额
 */
@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;
    private final DeductionMapper deductionMapper;
    private final ReconciliationMapper reconciliationMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResult<AppealVO> page(AppealQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<Appeal>()
                .eq(supplierId != null, Appeal::getSupplierId, supplierId)
                .eq(query.getNcrId() != null, Appeal::getNcrId, query.getNcrId())
                .eq(query.getAppealStatus() != null, Appeal::getAppealStatus, query.getAppealStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Appeal::getAppealNo, query.getKeyword())
                        .or()
                        .like(Appeal::getMaterialName, query.getKeyword()))
                .orderByDesc(Appeal::getCreateTime);
        Page<Appeal> page = appealMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public AppealVO getDetail(Long id) {
        return toVO(getWithScope(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AppealCreateDTO dto) {
        Appeal entity = new Appeal();
        entity.setAppealNo(generateAppealNo());
        entity.setNcrId(dto.getNcrId());
        entity.setInspectionId(dto.getInspectionId());
        entity.setDeductionId(dto.getDeductionId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setAppealReason(dto.getAppealReason());
        entity.setAppealDesc(dto.getAppealDesc());
        entity.setAdjustAmount(dto.getAdjustAmount());
        entity.setAppealStatus(0); // 草稿
        appealMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        Appeal entity = getWithScope(id);
        if (!Integer.valueOf(0).equals(entity.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可以提交");
        }
        entity.setAppealStatus(1); // 已提交
        entity.setSubmitTime(LocalDateTime.now());
        appealMapper.updateById(entity);
    }

    // ---- 联动9: 申诉审核通过 → 关联扣款调整 ----
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, AppealReviewDTO dto) {
        Appeal entity = getWithScope(id);
        if (!Integer.valueOf(1).equals(entity.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交的申诉可以审核");
        }
        entity.setAppealStatus(2); // 审核通过
        entity.setReviewer(SecurityUtils.getUserId());
        entity.setReviewerName(SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getRealName() : null);
        entity.setReviewTime(LocalDateTime.now());
        entity.setReviewOpinion(dto.getReviewOpinion());
        appealMapper.updateById(entity);

        // 申诉通过后调整关联扣款单金额
        adjustDeductionOnApproval(entity);

        domainEventPublisher.publish("supplier.quality", "quality.appeal.approved",
                DomainEvent.builder()
                        .eventType("quality.appeal.approved")
                        .data(Map.of("businessId", entity.getId(), "appealNo", entity.getAppealNo(),
                                "supplierId", entity.getSupplierId(), "deductionId", entity.getDeductionId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, AppealReviewDTO dto) {
        Appeal entity = getWithScope(id);
        if (!Integer.valueOf(1).equals(entity.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交的申诉可以审核");
        }
        entity.setAppealStatus(3); // 审核驳回
        entity.setReviewer(SecurityUtils.getUserId());
        entity.setReviewerName(SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getRealName() : null);
        entity.setReviewTime(LocalDateTime.now());
        entity.setReviewOpinion(dto.getReviewOpinion());
        appealMapper.updateById(entity);

        domainEventPublisher.publish("supplier.quality", "quality.appeal.rejected",
                DomainEvent.builder()
                        .eventType("quality.appeal.rejected")
                        .data(Map.of("businessId", entity.getId(), "appealNo", entity.getAppealNo(),
                                "supplierId", entity.getSupplierId()))
                        .build());
    }

    /**
     * 联动9: 申诉审核通过 → 调整关联扣款单金额
     */
    private void adjustDeductionOnApproval(Appeal appeal) {
        if (appeal.getDeductionId() == null) {
            return;
        }
        Deduction deduction = deductionMapper.selectById(appeal.getDeductionId());
        if (deduction == null) {
            return;
        }
        BigDecimal adjustAmount = appeal.getAdjustAmount() != null ? appeal.getAdjustAmount() : BigDecimal.ZERO;
        if (adjustAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        // 调整扣款金额：原扣款金额 - 申诉调整金额
        BigDecimal newAmount = (deduction.getDeductionAmount() != null ? deduction.getDeductionAmount() : BigDecimal.ZERO)
                .subtract(adjustAmount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            newAmount = BigDecimal.ZERO;
        }
        deduction.setDeductionAmount(newAmount);
        deduction.setDeductionReason((deduction.getDeductionReason() != null ? deduction.getDeductionReason() : "")
                + " 【申诉" + appeal.getAppealNo() + "通过，调整金额-" + adjustAmount + "】");
        deductionMapper.updateById(deduction);

        // 如果扣款已关联对账单，同步更新对账单总金额
        if (deduction.getReconId() != null) {
            Reconciliation reconciliation = reconciliationMapper.selectById(deduction.getReconId());
            if (reconciliation != null) {
                reconciliation.setTotalAmount(
                        (reconciliation.getTotalAmount() != null ? reconciliation.getTotalAmount() : BigDecimal.ZERO)
                                .add(adjustAmount)); // 扣款减少了，对账单应付金额相应增加
                reconciliationMapper.updateById(reconciliation);
            }
        }
    }

    private String generateAppealNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "APL" + datePart;
        Long count = appealMapper.selectCount(
                new LambdaQueryWrapper<Appeal>().likeRight(Appeal::getAppealNo, prefix));
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    private Appeal getWithScope(Long id) {
        Appeal entity = appealMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && (entity.getSupplierId() == null
                || !entity.getSupplierId().equals(SecurityUtils.getSupplierId()))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return entity;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商用户未绑定供应商");
        }
        if (querySupplierId != null && !supplierId.equals(querySupplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }

    private AppealVO toVO(Appeal e) {
        AppealVO vo = new AppealVO();
        vo.setId(e.getId());
        vo.setAppealNo(e.getAppealNo());
        vo.setNcrId(e.getNcrId());
        vo.setInspectionId(e.getInspectionId());
        vo.setDeductionId(e.getDeductionId());
        vo.setSupplierId(e.getSupplierId());
        vo.setMaterialCode(e.getMaterialCode());
        vo.setMaterialName(e.getMaterialName());
        vo.setAppealReason(e.getAppealReason());
        vo.setAppealDesc(e.getAppealDesc());
        vo.setAdjustAmount(e.getAdjustAmount());
        vo.setAppealStatus(e.getAppealStatus());
        vo.setAppealStatusDesc(switch (e.getAppealStatus() != null ? e.getAppealStatus() : -1) {
            case 0 -> "草稿";
            case 1 -> "已提交";
            case 2 -> "审核通过";
            case 3 -> "审核驳回";
            default -> "未知";
        });
        vo.setSubmitTime(e.getSubmitTime());
        vo.setReviewerName(e.getReviewerName());
        vo.setReviewTime(e.getReviewTime());
        vo.setReviewOpinion(e.getReviewOpinion());
        vo.setRemark(e.getRemark());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}