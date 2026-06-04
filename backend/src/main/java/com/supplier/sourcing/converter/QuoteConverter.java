package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.vo.QuoteVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuoteConverter {

    public static Quote toEntity(QuoteCreateDTO dto) {
        Quote entity = new Quote();
        entity.setQuoteNo(dto.getQuoteNo());
        entity.setRfqId(dto.getRfqId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setCurrency(dto.getCurrency());
        entity.setExchangeRate(dto.getExchangeRate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setPaymentTerms(dto.getPaymentTerms());
        entity.setValidUntil(dto.getValidUntil() != null ? dto.getValidUntil().atStartOfDay() : null);
        entity.setRemark(dto.getRemark());
        entity.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
        entity.setNegotiationRound(0);
        return entity;
    }

    public static QuoteVO toVO(Quote entity, String supplierName, String rfqNo, String rfqTitle) {
        QuoteVO vo = new QuoteVO();
        vo.setId(entity.getId());
        vo.setQuoteNo(entity.getQuoteNo());
        vo.setRfqId(entity.getRfqId());
        vo.setRfqNo(rfqNo);
        vo.setRfqTitle(rfqTitle);
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(supplierName);
        vo.setCurrency(entity.getCurrency());
        vo.setExchangeRate(entity.getExchangeRate());
        vo.setTotalAmount(entity.getTotalAmount());
        vo.setTaxAmount(entity.getTaxAmount());
        vo.setQuoteStatus(entity.getQuoteStatus());
        vo.setNegotiationRound(entity.getNegotiationRound());
        vo.setPaymentTerms(entity.getPaymentTerms());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setValidUntil(entity.getValidUntil());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public static QuoteVO toVO(Quote entity, String supplierName) {
        return toVO(entity, supplierName, null, null);
    }

    public static QuoteVO toVO(Quote entity) {
        return toVO(entity, null, null, null);
    }
}