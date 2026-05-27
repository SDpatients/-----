package com.supplier.sourcing.converter;

import com.supplier.sourcing.dto.RfqItemCreateDTO;
import com.supplier.sourcing.dto.RfqItemUpdateDTO;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.vo.RfqItemVO;

public class RfqItemConverter {

    public static RfqItem toEntity(RfqItemCreateDTO dto) {
        RfqItem entity = new RfqItem();
        entity.setRfqId(dto.getRfqId());
        entity.setLineNo(dto.getLineNo());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setMaterialSpec(dto.getMaterialSpec());
        entity.setUnit(dto.getUnit());
        entity.setQuantity(dto.getQuantity());
        entity.setTargetDeliveryDate(dto.getTargetDeliveryDate());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static void updateEntity(RfqItem entity, RfqItemUpdateDTO dto) {
        if (dto.getLineNo() != null) {
            entity.setLineNo(dto.getLineNo());
        }
        if (dto.getMaterialCode() != null) {
            entity.setMaterialCode(dto.getMaterialCode());
        }
        if (dto.getMaterialName() != null) {
            entity.setMaterialName(dto.getMaterialName());
        }
        if (dto.getMaterialSpec() != null) {
            entity.setMaterialSpec(dto.getMaterialSpec());
        }
        if (dto.getUnit() != null) {
            entity.setUnit(dto.getUnit());
        }
        if (dto.getQuantity() != null) {
            entity.setQuantity(dto.getQuantity());
        }
        if (dto.getTargetDeliveryDate() != null) {
            entity.setTargetDeliveryDate(dto.getTargetDeliveryDate());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
    }

    public static RfqItemVO toVO(RfqItem entity) {
        RfqItemVO vo = new RfqItemVO();
        vo.setId(entity.getId());
        vo.setRfqId(entity.getRfqId());
        vo.setLineNo(entity.getLineNo());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setMaterialName(entity.getMaterialName());
        vo.setMaterialSpec(entity.getMaterialSpec());
        vo.setUnit(entity.getUnit());
        vo.setQuantity(entity.getQuantity());
        vo.setTargetDeliveryDate(entity.getTargetDeliveryDate());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}