package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.vo.QuoteVO;

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
        entity.setValidUntil(dto.getValidUntil());
        entity.setRemark(dto.getRemark());
        entity.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
        entity.setNegotiationRound(0);
        return entity;
    }

    public static QuoteVO toVO(Quote entity) {
        QuoteVO vo = new QuoteVO();
        vo.setId(entity.getId());
        vo.setQuoteNo(entity.getQuoteNo());
        vo.setRfqId(entity.getRfqId());
        vo.setSupplierId(entity.getSupplierId());
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
}