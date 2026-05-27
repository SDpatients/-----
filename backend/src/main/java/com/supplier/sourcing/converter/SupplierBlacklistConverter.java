package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.SupplierBlacklistCreateDTO;
import com.supplier.sourcing.dto.SupplierBlacklistUpdateDTO;
import com.supplier.sourcing.entity.SupplierBlacklist;
import com.supplier.sourcing.enums.SupplierBlacklistStatusEnum;
import com.supplier.sourcing.vo.SupplierBlacklistVO;

public class SupplierBlacklistConverter {

    public static SupplierBlacklist toEntity(SupplierBlacklistCreateDTO dto) {
        SupplierBlacklist entity = new SupplierBlacklist();
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setCreditCode(dto.getCreditCode());
        entity.setReason(dto.getReason());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setStatus(SupplierBlacklistStatusEnum.ACTIVE.getCode());
        return entity;
    }

    public static void updateEntity(SupplierBlacklist entity, SupplierBlacklistUpdateDTO dto) {
        if (dto.getReason() != null) {
            entity.setReason(dto.getReason());
        }
        if (dto.getEndTime() != null) {
            entity.setEndTime(dto.getEndTime());
        }
    }

    public static SupplierBlacklistVO toVO(SupplierBlacklist entity) {
        SupplierBlacklistVO vo = new SupplierBlacklistVO();
        vo.setId(entity.getId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setCreditCode(entity.getCreditCode());
        vo.setReason(entity.getReason());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}