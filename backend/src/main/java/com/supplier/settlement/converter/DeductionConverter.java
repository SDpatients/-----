package com.supplier.settlement.converter;

import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.enums.DeductionStatusEnum;
import com.supplier.settlement.vo.DeductionVO;

public class DeductionConverter {

    public static Deduction toEntity(DeductionCreateDTO dto) {
        Deduction entity = new Deduction();
        entity.setDeductionNo(dto.getDeductionNo());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceId(dto.getSourceId());
        entity.setDeductionType(dto.getDeductionType());
        entity.setDeductionAmount(dto.getDeductionAmount());
        entity.setDeductionReason(dto.getDeductionReason());
        entity.setReconId(dto.getReconId());
        entity.setDeductionStatus(DeductionStatusEnum.DRAFT.getCode());
        return entity;
    }

    public static DeductionVO toVO(Deduction entity) {
        DeductionVO vo = new DeductionVO();
        vo.setId(entity.getId());
        vo.setDeductionNo(entity.getDeductionNo());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceId(entity.getSourceId());
        vo.setDeductionType(entity.getDeductionType());
        vo.setDeductionAmount(entity.getDeductionAmount());
        vo.setDeductionReason(entity.getDeductionReason());
        vo.setDeductionStatus(entity.getDeductionStatus());
        vo.setReconId(entity.getReconId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}