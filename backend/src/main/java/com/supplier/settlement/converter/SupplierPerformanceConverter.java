package com.supplier.settlement.converter;

import com.supplier.settlement.dto.SupplierPerformanceCreateDTO;
import com.supplier.settlement.dto.SupplierPerformanceUpdateDTO;
import com.supplier.settlement.entity.SupplierPerformance;
import com.supplier.settlement.vo.SupplierPerformanceVO;

public class SupplierPerformanceConverter {

    public static SupplierPerformance toEntity(SupplierPerformanceCreateDTO dto) {
        SupplierPerformance entity = new SupplierPerformance();
        entity.setSupplierId(dto.getSupplierId());
        entity.setEvaluatePeriod(dto.getEvaluatePeriod());
        entity.setQualityScore(dto.getQualityScore());
        entity.setDeliveryScore(dto.getDeliveryScore());
        entity.setServiceScore(dto.getServiceScore());
        entity.setPriceScore(dto.getPriceScore());
        entity.setQualifiedRate(dto.getQualifiedRate());
        entity.setOntimeRate(dto.getOntimeRate());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static void updateEntity(SupplierPerformance entity, SupplierPerformanceUpdateDTO dto) {
        entity.setEvaluatePeriod(dto.getEvaluatePeriod());
        entity.setQualityScore(dto.getQualityScore());
        entity.setDeliveryScore(dto.getDeliveryScore());
        entity.setServiceScore(dto.getServiceScore());
        entity.setPriceScore(dto.getPriceScore());
        entity.setQualifiedRate(dto.getQualifiedRate());
        entity.setOntimeRate(dto.getOntimeRate());
        entity.setRemark(dto.getRemark());
    }

    public static SupplierPerformanceVO toVO(SupplierPerformance entity) {
        SupplierPerformanceVO vo = new SupplierPerformanceVO();
        vo.setId(entity.getId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setEvaluatePeriod(entity.getEvaluatePeriod());
        vo.setQualityScore(entity.getQualityScore());
        vo.setDeliveryScore(entity.getDeliveryScore());
        vo.setServiceScore(entity.getServiceScore());
        vo.setPriceScore(entity.getPriceScore());
        vo.setTotalScore(entity.getTotalScore());
        vo.setQualifiedRate(entity.getQualifiedRate());
        vo.setOntimeRate(entity.getOntimeRate());
        vo.setEvaluateBy(entity.getEvaluateBy());
        vo.setEvaluateTime(entity.getEvaluateTime());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}