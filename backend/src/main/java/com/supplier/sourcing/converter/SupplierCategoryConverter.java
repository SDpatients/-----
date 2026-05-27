package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.SupplierCategoryCreateDTO;
import com.supplier.sourcing.dto.SupplierCategoryUpdateDTO;
import com.supplier.sourcing.entity.SupplierCategory;
import com.supplier.sourcing.vo.SupplierCategoryVO;

public class SupplierCategoryConverter {

    public static SupplierCategory toEntity(SupplierCategoryCreateDTO dto) {
        SupplierCategory entity = new SupplierCategory();
        entity.setParentId(dto.getParentId());
        entity.setCategoryName(dto.getCategoryName());
        entity.setCategoryCode(dto.getCategoryCode());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setStatus(1);
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static void updateEntity(SupplierCategory entity, SupplierCategoryUpdateDTO dto) {
        if (dto.getParentId() != null) entity.setParentId(dto.getParentId());
        if (dto.getCategoryName() != null) entity.setCategoryName(dto.getCategoryName());
        if (dto.getCategoryCode() != null) entity.setCategoryCode(dto.getCategoryCode());
        if (dto.getSort() != null) entity.setSort(dto.getSort());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
    }

    public static SupplierCategoryVO toVO(SupplierCategory entity) {
        SupplierCategoryVO vo = new SupplierCategoryVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}