package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.RfqSupplierCreateDTO;
import com.supplier.sourcing.entity.RfqSupplier;
import com.supplier.sourcing.vo.RfqSupplierVO;

import java.time.LocalDateTime;

public class RfqSupplierConverter {

    public static RfqSupplier toEntity(RfqSupplierCreateDTO dto) {
        RfqSupplier entity = new RfqSupplier();
        entity.setRfqId(dto.getRfqId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setInviteStatus(0);
        entity.setInviteTime(LocalDateTime.now());
        return entity;
    }

    public static RfqSupplierVO toVO(RfqSupplier entity) {
        RfqSupplierVO vo = new RfqSupplierVO();
        vo.setId(entity.getId());
        vo.setRfqId(entity.getRfqId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setInviteStatus(entity.getInviteStatus());
        vo.setQuoteId(entity.getQuoteId());
        vo.setInviteTime(entity.getInviteTime());
        vo.setResponseTime(entity.getResponseTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}