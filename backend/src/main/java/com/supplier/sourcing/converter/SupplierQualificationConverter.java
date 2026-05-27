package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.SupplierQualificationCreateDTO;
import com.supplier.sourcing.dto.SupplierQualificationUpdateDTO;
import com.supplier.sourcing.entity.SupplierQualification;
import com.supplier.sourcing.vo.SupplierQualificationVO;

public class SupplierQualificationConverter {

    public static SupplierQualification toEntity(SupplierQualificationCreateDTO dto) {
        SupplierQualification entity = new SupplierQualification();
        entity.setSupplierId(dto.getSupplierId());
        entity.setQualType(dto.getQualType());
        entity.setQualName(dto.getQualName());
        entity.setQualNo(dto.getQualNo());
        entity.setQualOrg(dto.getQualOrg());
        entity.setValidStart(dto.getValidStart());
        entity.setValidEnd(dto.getValidEnd());
        entity.setFileId(dto.getFileId());
        entity.setRemindDays(dto.getRemindDays());
        entity.setRemark(dto.getRemark());
        entity.setStatus(1);
        return entity;
    }

    public static void updateEntity(SupplierQualification entity, SupplierQualificationUpdateDTO dto) {
        if (dto.getQualType() != null) {
            entity.setQualType(dto.getQualType());
        }
        if (dto.getQualName() != null) {
            entity.setQualName(dto.getQualName());
        }
        if (dto.getQualNo() != null) {
            entity.setQualNo(dto.getQualNo());
        }
        if (dto.getQualOrg() != null) {
            entity.setQualOrg(dto.getQualOrg());
        }
        if (dto.getValidStart() != null) {
            entity.setValidStart(dto.getValidStart());
        }
        if (dto.getValidEnd() != null) {
            entity.setValidEnd(dto.getValidEnd());
        }
        if (dto.getFileId() != null) {
            entity.setFileId(dto.getFileId());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getRemindDays() != null) {
            entity.setRemindDays(dto.getRemindDays());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
    }

    public static SupplierQualificationVO toVO(SupplierQualification entity) {
        SupplierQualificationVO vo = new SupplierQualificationVO();
        vo.setId(entity.getId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setQualType(entity.getQualType());
        vo.setQualName(entity.getQualName());
        vo.setQualNo(entity.getQualNo());
        vo.setQualOrg(entity.getQualOrg());
        vo.setValidStart(entity.getValidStart());
        vo.setValidEnd(entity.getValidEnd());
        vo.setFileId(entity.getFileId());
        vo.setStatus(entity.getStatus());
        vo.setRemindDays(entity.getRemindDays());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}