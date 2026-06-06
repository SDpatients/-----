package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.SupplierCreateDTO;
import com.supplier.sourcing.dto.SupplierUpdateDTO;
import com.supplier.sourcing.entity.SupplierInfo;
import com.supplier.sourcing.enums.SupplierStatusEnum;
import com.supplier.sourcing.vo.SupplierVO;

public class SupplierConverter {

    public static SupplierInfo toEntity(SupplierCreateDTO dto) {
        SupplierInfo entity = new SupplierInfo();
        entity.setSupplierCode(dto.getSupplierCode());
        entity.setSupplierName(dto.getSupplierName());
        entity.setSupplierShortName(dto.getSupplierShortName());
        entity.setCategoryId(dto.getCategoryId());
        entity.setSupplierType(dto.getSupplierType());
        entity.setCreditCode(dto.getCreditCode());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setContactName(dto.getContactName());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setAddress(dto.getAddress());
        entity.setRemark(dto.getRemark());
        entity.setStatus(SupplierStatusEnum.APPROVED.getCode());
        return entity;
    }

    public static void updateEntity(SupplierUpdateDTO dto, SupplierInfo entity) {
        entity.setSupplierName(dto.getSupplierName());
        entity.setSupplierShortName(dto.getSupplierShortName());
        entity.setCategoryId(dto.getCategoryId());
        entity.setSupplierType(dto.getSupplierType());
        entity.setCreditCode(dto.getCreditCode());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setContactName(dto.getContactName());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setAddress(dto.getAddress());
        entity.setBankName(dto.getBankName());
        entity.setBankAccount(dto.getBankAccount());
        entity.setTaxNumber(dto.getTaxNumber());
        entity.setRemark(dto.getRemark());
    }

    public static SupplierVO toVO(SupplierInfo entity) {
        SupplierVO vo = new SupplierVO();
        vo.setId(entity.getId());
        vo.setSupplierCode(entity.getSupplierCode());
        vo.setSupplierName(entity.getSupplierName());
        vo.setSupplierShortName(entity.getSupplierShortName());
        vo.setCategoryId(entity.getCategoryId());
        vo.setSupplierType(entity.getSupplierType());
        vo.setCreditCode(entity.getCreditCode());
        vo.setLegalPerson(entity.getLegalPerson());
        vo.setRegisteredCapital(entity.getRegisteredCapital());
        vo.setEstablishDate(entity.getEstablishDate());
        vo.setBusinessScope(entity.getBusinessScope());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setContactEmail(entity.getContactEmail());
        vo.setBankName(entity.getBankName());
        vo.setBankAccount(entity.getBankAccount());
        vo.setTaxNumber(entity.getTaxNumber());
        vo.setStatus(entity.getStatus());
        vo.setAuditTime(entity.getAuditTime());
        vo.setAuditBy(entity.getAuditBy());
        vo.setAuditRemark(entity.getAuditRemark());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}