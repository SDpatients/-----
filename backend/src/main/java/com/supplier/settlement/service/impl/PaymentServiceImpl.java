package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.converter.InvoiceConverter;
import com.supplier.settlement.converter.PaymentConverter;
import com.supplier.settlement.dto.PaymentActionDTO;
import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.dto.PaymentScheduleDTO;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.mapper.InvoiceMapper;
import com.supplier.settlement.mapper.PaymentMapper;
import com.supplier.settlement.query.PaymentQuery;
import com.supplier.settlement.service.PaymentService;
import com.supplier.settlement.vo.InvoiceVO;
import com.supplier.settlement.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final InvoiceMapper invoiceMapper;

    @Override
    public PageResult<PaymentVO> page(PaymentQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                .eq(supplierId != null, Payment::getSupplierId, supplierId)
                .eq(query.getReconId() != null, Payment::getReconId, query.getReconId())
                .eq(query.getInvoiceId() != null, Payment::getInvoiceId, query.getInvoiceId())
                .eq(query.getPaymentStatus() != null, Payment::getPaymentStatus, query.getPaymentStatus())
                .eq(query.getApproveStatus() != null, Payment::getApproveStatus, query.getApproveStatus())
                .ge(query.getScheduleDateStart() != null, Payment::getScheduleDate, query.getScheduleDateStart())
                .le(query.getScheduleDateEnd() != null, Payment::getScheduleDate, query.getScheduleDateEnd())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Payment::getPaymentNo, query.getKeyword())
                        .or()
                        .like(Payment::getSupplierName, query.getKeyword()))
                .orderByDesc(Payment::getCreateTime);
        Page<Payment> page = paymentMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(PaymentConverter::toVO));
    }

    @Override
    public PaymentVO getDetail(Long id) {
        Payment entity = getWithDataScope(id);
        return PaymentConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PaymentCreateDTO dto) {
        Long count = paymentMapper.selectCount(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPaymentNo, dto.getPaymentNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "付款单号已存在");
        }
        Payment entity = PaymentConverter.toEntity(dto);
        paymentMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id, PaymentActionDTO dto) {
        dto = normalizeAction(dto);
        Payment entity = getWithDataScope(id);
        if (!Integer.valueOf(PaymentStatusEnum.APPROVED.getCode()).equals(entity.getPaymentStatus())
                && !Integer.valueOf(PaymentStatusEnum.SCHEDULED.getCode()).equals(entity.getPaymentStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前付款单状态不允许付款，需审批通过并排期");
        }
        entity.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
        entity.setPaymentTime(LocalDateTime.now());
        entity.setReceiptNo(dto.getRemark());
        paymentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, PaymentActionDTO dto) {
        dto = normalizeAction(dto);
        Payment entity = getWithDataScope(id);
        Integer status = entity.getPaymentStatus();
        if (!Integer.valueOf(PaymentStatusEnum.PENDING.getCode()).equals(status)
                && !Integer.valueOf(PaymentStatusEnum.APPROVING.getCode()).equals(status)) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待付款或审批中状态可以拒绝");
        }
        entity.setPaymentStatus(PaymentStatusEnum.REJECTED.getCode());
        entity.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : entity.getRemark());
        paymentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long id) {
        Payment entity = getWithDataScope(id);
        if (!Integer.valueOf(PaymentStatusEnum.PENDING.getCode()).equals(entity.getPaymentStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待付款状态可以提交审批");
        }
        entity.setPaymentStatus(PaymentStatusEnum.APPROVING.getCode());
        paymentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedule(Long id, PaymentScheduleDTO dto) {
        Payment entity = getWithDataScope(id);
        if (!Integer.valueOf(PaymentStatusEnum.APPROVED.getCode()).equals(entity.getPaymentStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已审批的付款单才能排期");
        }
        entity.setScheduleDate(dto.getScheduleDate());
        entity.setPaymentTerms(dto.getPaymentTerms());
        entity.setPaymentStatus(PaymentStatusEnum.SCHEDULED.getCode());
        paymentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, PaymentActionDTO dto) {
        dto = normalizeAction(dto);
        Payment entity = getWithDataScope(id);
        Integer status = entity.getPaymentStatus();
        if (Integer.valueOf(PaymentStatusEnum.PAID.getCode()).equals(status)) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "已付款的单据不能取消");
        }
        entity.setPaymentStatus(PaymentStatusEnum.CANCELLED.getCode());
        entity.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : entity.getRemark());
        paymentMapper.updateById(entity);
    }

    @Override
    public List<Map<String, Object>> getCallbackLogs(Long id) {
        getWithDataScope(id);
        // 回传日志 - 返回基础结构
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("id", id);
        log.put("paymentId", id);
        log.put("callbackType", "ERP");
        log.put("callbackStatus", "pending");
        log.put("callbackTime", LocalDateTime.now().toString());
        log.put("remark", "待回传");
        logs.add(log);
        return logs;
    }

    @Override
    public List<InvoiceVO> getLinkedInvoices(Long id) {
        Payment payment = getWithDataScope(id);
        List<InvoiceVO> vos = new ArrayList<>();
        if (payment.getInvoiceId() != null) {
            Invoice invoice = invoiceMapper.selectById(payment.getInvoiceId());
            if (invoice != null) {
                vos.add(InvoiceConverter.toVO(invoice));
            }
        }
        return vos;
    }

    private Payment getWithDataScope(Long id) {
        Payment entity = paymentMapper.selectById(id);
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

    private PaymentActionDTO normalizeAction(PaymentActionDTO dto) {
        return dto == null ? new PaymentActionDTO() : dto;
    }
}