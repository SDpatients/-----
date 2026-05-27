package com.supplier.settlement.converter;

import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.vo.PaymentVO;

public class PaymentConverter {

    public static Payment toEntity(PaymentCreateDTO dto) {
        Payment entity = new Payment();
        entity.setPaymentNo(dto.getPaymentNo());
        entity.setInvoiceId(dto.getInvoiceId());
        entity.setInvoiceNo(dto.getInvoiceNo());
        entity.setReconId(dto.getReconId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setPaymentAccount(dto.getPaymentAccount());
        entity.setPaymentBank(dto.getPaymentBank());
        entity.setReceiveAccount(dto.getReceiveAccount());
        entity.setReceiveBank(dto.getReceiveBank());
        entity.setScheduleDate(dto.getScheduleDate());
        entity.setPaymentTerms(dto.getPaymentTerms());
        entity.setRemark(dto.getRemark());
        entity.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
        entity.setApproveStatus(0);
        return entity;
    }

    public static PaymentVO toVO(Payment entity) {
        PaymentVO vo = new PaymentVO();
        vo.setId(entity.getId());
        vo.setPaymentNo(entity.getPaymentNo());
        vo.setInvoiceId(entity.getInvoiceId());
        vo.setInvoiceNo(entity.getInvoiceNo());
        vo.setReconId(entity.getReconId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setPaymentAmount(entity.getPaymentAmount());
        vo.setPaymentMethod(entity.getPaymentMethod());
        vo.setPaymentAccount(entity.getPaymentAccount());
        vo.setPaymentBank(entity.getPaymentBank());
        vo.setReceiveAccount(entity.getReceiveAccount());
        vo.setReceiveBank(entity.getReceiveBank());
        vo.setScheduleDate(entity.getScheduleDate());
        vo.setPaymentTerms(entity.getPaymentTerms());
        vo.setPaymentTime(entity.getPaymentTime());
        vo.setPaymentStatus(entity.getPaymentStatus());
        vo.setReceiptNo(entity.getReceiptNo());
        vo.setApproveStatus(entity.getApproveStatus());
        vo.setVoucherNo(entity.getVoucherNo());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}