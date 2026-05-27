package com.supplier.logistics.converter;

import com.supplier.logistics.dto.VmiInventorySyncDTO;
import com.supplier.logistics.entity.VmiInventory;
import com.supplier.logistics.vo.VmiInventoryVO;

public class VmiInventoryConverter {

    public static VmiInventory toEntity(VmiInventorySyncDTO dto) {
        VmiInventory entity = new VmiInventory();
        entity.setSupplierId(dto.getSupplierId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setOnhandQty(dto.getOnhandQty());
        entity.setAvailableQty(dto.getAvailableQty());
        entity.setSafetyQty(dto.getSafetyQty());
        entity.setMaxQty(dto.getMaxQty());
        return entity;
    }

    public static VmiInventoryVO toVO(VmiInventory entity) {
        VmiInventoryVO vo = new VmiInventoryVO();
        vo.setId(entity.getId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setWarehouseId(entity.getWarehouseId());
        vo.setWarehouseName(entity.getWarehouseName());
        vo.setOnhandQty(entity.getOnhandQty());
        vo.setAvailableQty(entity.getAvailableQty());
        vo.setSafetyQty(entity.getSafetyQty());
        vo.setMaxQty(entity.getMaxQty());
        vo.setInventoryStatus(entity.getInventoryStatus());
        vo.setLastSyncTime(entity.getLastSyncTime());
        return vo;
    }
}