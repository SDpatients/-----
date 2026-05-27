package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.ExchangeRateCreateDTO;
import com.supplier.sourcing.dto.ExchangeRateUpdateDTO;
import com.supplier.sourcing.entity.ExchangeRate;
import com.supplier.sourcing.vo.ExchangeRateVO;

public class ExchangeRateConverter {

    public static ExchangeRate toEntity(ExchangeRateCreateDTO dto) {
        ExchangeRate entity = new ExchangeRate();
        entity.setFromCurrency(dto.getFromCurrency());
        entity.setToCurrency(dto.getToCurrency());
        entity.setRate(dto.getRate());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setSource(dto.getSource());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static void updateEntity(ExchangeRate entity, ExchangeRateUpdateDTO dto) {
        if (dto.getRate() != null) {
            entity.setRate(dto.getRate());
        }
        if (dto.getEffectiveDate() != null) {
            entity.setEffectiveDate(dto.getEffectiveDate());
        }
        if (dto.getSource() != null) {
            entity.setSource(dto.getSource());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
    }

    public static ExchangeRateVO toVO(ExchangeRate entity) {
        ExchangeRateVO vo = new ExchangeRateVO();
        vo.setId(entity.getId());
        vo.setFromCurrency(entity.getFromCurrency());
        vo.setToCurrency(entity.getToCurrency());
        vo.setRate(entity.getRate());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setSource(entity.getSource());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}