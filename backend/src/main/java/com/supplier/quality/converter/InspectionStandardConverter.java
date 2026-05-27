package com.supplier.quality.converter;

import com.supplier.quality.dto.InspectionStandardCreateDTO;
import com.supplier.quality.dto.InspectionStandardItemDTO;
import com.supplier.quality.entity.InspectionStandard;
import com.supplier.quality.entity.InspectionStandardItem;
import com.supplier.quality.vo.InspectionStandardItemVO;
import com.supplier.quality.vo.InspectionStandardVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InspectionStandardConverter {

    public static InspectionStandard toEntity(InspectionStandardCreateDTO dto) {
        InspectionStandard entity = new InspectionStandard();
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setStandardName(dto.getStandardName());
        entity.setSampleRule(dto.getSampleRule());
        entity.setVersionNo(dto.getVersionNo());
        entity.setRemark(dto.getRemark());
        entity.setStatus(1);
        return entity;
    }

    public static InspectionStandardItem toItemEntity(InspectionStandardItemDTO dto) {
        InspectionStandardItem item = new InspectionStandardItem();
        item.setStandardId(dto.getStandardId());
        item.setItemName(dto.getItemName());
        item.setItemType(dto.getItemType());
        item.setStandardValue(dto.getStandardValue());
        item.setUpperLimit(dto.getUpperLimit());
        item.setLowerLimit(dto.getLowerLimit());
        item.setUnit(dto.getUnit());
        item.setRequired(dto.getRequired() != null ? dto.getRequired() : 1);
        item.setSort(dto.getSort() != null ? dto.getSort() : 0);
        return item;
    }

    public static InspectionStandardVO toVO(InspectionStandard entity) {
        InspectionStandardVO vo = new InspectionStandardVO();
        vo.setId(entity.getId());
        vo.setStandardNo(entity.getStandardNo());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setMaterialName(entity.getMaterialName());
        vo.setStandardName(entity.getStandardName());
        vo.setSampleRule(entity.getSampleRule());
        vo.setVersionNo(entity.getVersionNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public static InspectionStandardItemVO toItemVO(InspectionStandardItem entity) {
        InspectionStandardItemVO vo = new InspectionStandardItemVO();
        vo.setId(entity.getId());
        vo.setStandardId(entity.getStandardId());
        vo.setItemName(entity.getItemName());
        vo.setItemType(entity.getItemType());
        vo.setStandardValue(entity.getStandardValue());
        vo.setUpperLimit(entity.getUpperLimit());
        vo.setLowerLimit(entity.getLowerLimit());
        vo.setUnit(entity.getUnit());
        vo.setRequired(entity.getRequired());
        vo.setSort(entity.getSort());
        return vo;
    }

    public static String generateStandardNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "STD" + datePart;
    }
}