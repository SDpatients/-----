package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.RfqCreateDTO;
import com.supplier.sourcing.dto.RfqUpdateDTO;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.vo.RfqVO;

public class RfqConverter {

    public static Rfq toEntity(RfqCreateDTO dto) {
        Rfq entity = new Rfq();
        entity.setRfqNo(dto.getRfqNo());
        entity.setRfqTitle(dto.getRfqTitle());
        entity.setOrgId(dto.getOrgId());
        entity.setCurrency(dto.getCurrency());
        entity.setQuoteDeadline(dto.getQuoteDeadline());
        entity.setRemark(dto.getRemark());
        entity.setRfqStatus(RfqStatusEnum.DRAFT.getCode());
        return entity;
    }

    public static void updateEntity(Rfq entity, RfqUpdateDTO dto) {
        if (dto.getRfqTitle() != null) {
            entity.setRfqTitle(dto.getRfqTitle());
        }
        if (dto.getOrgId() != null) {
            entity.setOrgId(dto.getOrgId());
        }
        if (dto.getCurrency() != null) {
            entity.setCurrency(dto.getCurrency());
        }
        if (dto.getQuoteDeadline() != null) {
            entity.setQuoteDeadline(dto.getQuoteDeadline());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
    }

    public static RfqVO toVO(Rfq entity) {
        RfqVO vo = new RfqVO();
        vo.setId(entity.getId());
        vo.setRfqNo(entity.getRfqNo());
        vo.setRfqTitle(entity.getRfqTitle());
        vo.setOrgId(entity.getOrgId());
        vo.setCurrency(entity.getCurrency());
        vo.setQuoteDeadline(entity.getQuoteDeadline());
        vo.setRfqStatus(entity.getRfqStatus());
        vo.setPublishTime(entity.getPublishTime());
        vo.setCloseTime(entity.getCloseTime());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}