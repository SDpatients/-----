package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.converter.VmiInventoryConverter;
import com.supplier.logistics.dto.VmiInventorySyncDTO;
import com.supplier.logistics.entity.VmiInventory;
import com.supplier.logistics.mapper.VmiInventoryMapper;
import com.supplier.logistics.query.VmiInventoryQuery;
import com.supplier.logistics.service.VmiInventoryService;
import com.supplier.logistics.vo.VmiInventoryVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VmiInventoryServiceImpl implements VmiInventoryService {

    private final VmiInventoryMapper vmiInventoryMapper;

    @Override
    public PageResult<VmiInventoryVO> page(VmiInventoryQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<VmiInventory> wrapper = new LambdaQueryWrapper<VmiInventory>()
                .eq(supplierId != null, VmiInventory::getSupplierId, supplierId)
                .eq(StringUtils.hasText(query.getMaterialCode()), VmiInventory::getMaterialCode, query.getMaterialCode())
                .eq(query.getInventoryStatus() != null, VmiInventory::getInventoryStatus, query.getInventoryStatus())
                .orderByDesc(VmiInventory::getCreateTime);
        Page<VmiInventory> page = vmiInventoryMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(VmiInventoryConverter::toVO));
    }

    @Override
    public VmiInventoryVO getDetail(Long id) {
        VmiInventory inventory = getInventoryWithDataScope(id);
        return VmiInventoryConverter.toVO(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sync(VmiInventorySyncDTO dto) {
        VmiInventory inventory = vmiInventoryMapper.selectOne(new LambdaQueryWrapper<VmiInventory>()
                .eq(VmiInventory::getSupplierId, dto.getSupplierId())
                .eq(VmiInventory::getMaterialCode, dto.getMaterialCode())
                .eq(dto.getWarehouseId() != null, VmiInventory::getWarehouseId, dto.getWarehouseId()));
        if (inventory == null) {
            inventory = VmiInventoryConverter.toEntity(dto);
            inventory.setInventoryStatus(1);
            inventory.setLastSyncTime(LocalDateTime.now());
            vmiInventoryMapper.insert(inventory);
        } else {
            inventory.setOnhandQty(dto.getOnhandQty() != null ? dto.getOnhandQty() : inventory.getOnhandQty());
            inventory.setAvailableQty(dto.getAvailableQty() != null ? dto.getAvailableQty() : inventory.getAvailableQty());
            inventory.setSafetyQty(dto.getSafetyQty() != null ? dto.getSafetyQty() : inventory.getSafetyQty());
            inventory.setMaxQty(dto.getMaxQty() != null ? dto.getMaxQty() : inventory.getMaxQty());
            if (dto.getWarehouseId() != null) {
                inventory.setWarehouseId(dto.getWarehouseId());
            }
            if (StringUtils.hasText(dto.getWarehouseName())) {
                inventory.setWarehouseName(dto.getWarehouseName());
            }
            inventory.setLastSyncTime(LocalDateTime.now());
            evaluateInventoryStatus(inventory);
            vmiInventoryMapper.updateById(inventory);
        }
    }

    private void evaluateInventoryStatus(VmiInventory inventory) {
        if (inventory.getAvailableQty().compareTo(inventory.getSafetyQty()) <= 0) {
            inventory.setInventoryStatus(3);
        } else if (inventory.getMaxQty() != null && inventory.getAvailableQty().compareTo(inventory.getMaxQty()) >= 0) {
            inventory.setInventoryStatus(2);
        } else {
            inventory.setInventoryStatus(1);
        }
    }

    private VmiInventory getInventoryWithDataScope(Long id) {
        VmiInventory inventory = vmiInventoryMapper.selectById(id);
        if (inventory == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !inventory.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return inventory;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商用户未绑定供应商");
        }
        if (querySupplierId != null && !supplierId.equals(querySupplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }
}