package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.PaymentApprovalActionDTO;
import com.supplier.settlement.dto.PaymentApprovalSubmitDTO;
import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.entity.PaymentApproval;
import com.supplier.settlement.enums.ApprovalStatusEnum;
import com.supplier.settlement.enums.ApproveStatusEnum;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.mapper.PaymentApprovalMapper;
import com.supplier.settlement.mapper.PaymentMapper;
import com.supplier.settlement.query.PaymentApprovalQuery;
import com.supplier.settlement.service.PaymentApprovalService;
import com.supplier.settlement.vo.PaymentApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApprovalServiceImpl implements PaymentApprovalService {

    private final PaymentApprovalMapper approvalMapper;
    private final PaymentMapper paymentMapper;

    /** 大额付款阈值：超过此金额需多级审批 */
    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("100000.00");

    /** 大额付款审批级数 */
    private static final int LARGE_AMOUNT_LEVELS = 3;

    /** 普通付款审批级数 */
    private static final int NORMAL_LEVELS = 1;

    @Override
    public PageResult<PaymentApprovalVO> page(PaymentApprovalQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<PaymentApproval> wrapper = new LambdaQueryWrapper<PaymentApproval>()
                .eq(query.getPaymentId() != null, PaymentApproval::getPaymentId, query.getPaymentId())
                .eq(supplierId != null, PaymentApproval::getSupplierId, supplierId)
                .eq(query.getApprovalStatus() != null, PaymentApproval::getApprovalStatus, query.getApprovalStatus())
                .eq(query.getApprovalLevel() != null, PaymentApproval::getApprovalLevel, query.getApprovalLevel())
                .orderByDesc(PaymentApproval::getCreateTime);
        Page<PaymentApproval> page = approvalMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PaymentApprovalVO getDetail(Long id) {
        PaymentApproval entity = approvalMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(PaymentApprovalSubmitDTO dto) {
        Payment payment = paymentMapper.selectById(dto.getPaymentId());
        if (payment == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "付款单不存在");
        }
        if (!Integer.valueOf(PaymentStatusEnum.PENDING.getCode()).equals(payment.getPaymentStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前付款单状态不允许提交审批");
        }

        // 删除旧的审批记录
        approvalMapper.delete(new LambdaQueryWrapper<PaymentApproval>()
                .eq(PaymentApproval::getPaymentId, payment.getId()));

        // 判断审批级数
        boolean isLarge = payment.getPaymentAmount() != null
                && payment.getPaymentAmount().compareTo(LARGE_AMOUNT_THRESHOLD) >= 0;
        int levels = isLarge ? LARGE_AMOUNT_LEVELS : NORMAL_LEVELS;

        // 生成多级审批记录
        for (int i = 1; i <= levels; i++) {
            PaymentApproval approval = new PaymentApproval();
            approval.setPaymentId(payment.getId());
            approval.setPaymentNo(payment.getPaymentNo());
            approval.setSupplierId(payment.getSupplierId());
            approval.setSupplierName(payment.getSupplierName());
            approval.setPaymentAmount(payment.getPaymentAmount());
            approval.setApprovalLevel(i);
            approval.setApprovalStatus(ApprovalStatusEnum.PENDING.getCode());
            approvalMapper.insert(approval);
        }

        // 更新付款单状态
        payment.setPaymentStatus(PaymentStatusEnum.APPROVING.getCode());
        payment.setApproveStatus(ApproveStatusEnum.PENDING.getCode());
        paymentMapper.updateById(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, PaymentApprovalActionDTO dto) {
        PaymentApproval approval = approvalMapper.selectById(id);
        if (approval == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "审批记录不存在");
        }
        if (!Integer.valueOf(ApprovalStatusEnum.PENDING.getCode()).equals(approval.getApprovalStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "该审批已经处理过");
        }
        // 更新审批记录
        approval.setApprovalStatus(ApprovalStatusEnum.APPROVED.getCode());
        approval.setApproverId(SecurityUtils.getUserId());
        approval.setApproverName(SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getUser().getRealName() : null);
        approval.setApproveRemark(dto.getApproveRemark());
        approval.setApproveTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        // 检查当前级审批通过后，是否所有级别的审批都已完成
        checkAndAdvanceApproval(approval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, PaymentApprovalActionDTO dto) {
        PaymentApproval approval = approvalMapper.selectById(id);
        if (approval == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "审批记录不存在");
        }
        if (!Integer.valueOf(ApprovalStatusEnum.PENDING.getCode()).equals(approval.getApprovalStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "该审批已经处理过");
        }
        // 更新当前审批为驳回
        approval.setApprovalStatus(ApprovalStatusEnum.REJECTED.getCode());
        approval.setApproverId(SecurityUtils.getUserId());
        approval.setApproverName(SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getUser().getRealName() : null);
        approval.setApproveRemark(dto.getApproveRemark());
        approval.setApproveTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        // 驳回时，将所有同级及未审批的记录都置为驳回
        Payment payment = paymentMapper.selectById(approval.getPaymentId());
        if (payment != null) {
            payment.setPaymentStatus(PaymentStatusEnum.REJECTED.getCode());
            payment.setApproveStatus(ApproveStatusEnum.REJECTED.getCode());
            paymentMapper.updateById(payment);
        }
    }

    private void checkAndAdvanceApproval(PaymentApproval approval) {
        // 检查该付款单是否还有未审批的记录
        Long pendingCount = approvalMapper.selectCount(new LambdaQueryWrapper<PaymentApproval>()
                .eq(PaymentApproval::getPaymentId, approval.getPaymentId())
                .eq(PaymentApproval::getApprovalStatus, ApprovalStatusEnum.PENDING.getCode()));

        Payment payment = paymentMapper.selectById(approval.getPaymentId());
        if (payment == null) {
            return;
        }

        if (pendingCount == 0) {
            // 所有级别审批通过
            payment.setPaymentStatus(PaymentStatusEnum.APPROVED.getCode());
            payment.setApproveStatus(ApproveStatusEnum.APPROVED.getCode());
        }
        paymentMapper.updateById(payment);
    }

    private PaymentApprovalVO toVO(PaymentApproval entity) {
        PaymentApprovalVO vo = new PaymentApprovalVO();
        vo.setId(entity.getId());
        vo.setPaymentId(entity.getPaymentId());
        vo.setPaymentNo(entity.getPaymentNo());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setPaymentAmount(entity.getPaymentAmount());
        vo.setApprovalLevel(entity.getApprovalLevel());
        vo.setApprovalStatus(entity.getApprovalStatus());
        vo.setApproverId(entity.getApproverId());
        vo.setApproverName(entity.getApproverName());
        vo.setApproveRemark(entity.getApproveRemark());
        vo.setApproveTime(entity.getApproveTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
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
}