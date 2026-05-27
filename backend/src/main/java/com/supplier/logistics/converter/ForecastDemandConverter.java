package com.supplier.logistics.converter;

import com.supplier.logistics.dto.ForecastDemandCreateDTO;
import com.supplier.logistics.entity.ForecastDemand;
import com.supplier.logistics.vo.ForecastDemandVO;

public class ForecastDemandConverter {

    public static ForecastDemand toEntity(ForecastDemandCreateDTO dto) {
        ForecastDemand entity = new ForecastDemand();
        entity.setSupplierId(dto.getSupplierId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setDemandDate(dto.getDemandDate());
        entity.setDemandQty(dto.getDemandQty());
        entity.setDemandType(dto.getDemandType());
        return entity;
    }

    public static ForecastDemandVO toVO(ForecastDemand entity) {
        ForecastDemandVO vo = new ForecastDemandVO();
        vo.setId(entity.getId());
        vo.setDemandNo(entity.getDemandNo());
        vo.setSupplierId(entity.getSupplierId());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setDemandDate(entity.getDemandDate());
        vo.setDemandQty(entity.getDemandQty());
        vo.setDemandType(entity.getDemandType());
        vo.setDemandStatus(entity.getDemandStatus());
        return vo;
    }
}