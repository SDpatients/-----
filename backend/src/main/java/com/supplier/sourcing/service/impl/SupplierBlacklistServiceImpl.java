package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.SupplierBlacklistConverter;
import com.supplier.sourcing.dto.SupplierBlacklistCreateDTO;
import com.supplier.sourcing.dto.SupplierBlacklistUpdateDTO;
import com.supplier.sourcing.entity.SupplierBlacklist;
import com.supplier.sourcing.entity.SupplierInfo;
import com.supplier.sourcing.enums.SupplierBlacklistStatusEnum;
import com.supplier.sourcing.enums.SupplierStatusEnum;
import com.supplier.sourcing.mapper.SupplierBlacklistMapper;
import com.supplier.sourcing.mapper.SupplierInfoMapper;
import com.supplier.sourcing.query.SupplierBlacklistQuery;
import com.supplier.sourcing.service.SupplierBlacklistService;
import com.supplier.sourcing.vo.SupplierBlacklistVO;
import com.supplier.system.entity.SysUser;
import com.supplier.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupplierBlacklistServiceImpl implements SupplierBlacklistService {

    private final SupplierBlacklistMapper supplierBlacklistMapper;
    private final SupplierInfoMapper supplierInfoMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<SupplierBlacklistVO> page(SupplierBlacklistQuery query) {
        LambdaQueryWrapper<SupplierBlacklist> wrapper = new LambdaQueryWrapper<SupplierBlacklist>()
                .eq(query.getStatus() != null, SupplierBlacklist::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getSupplierName()), SupplierBlacklist::getSupplierName, query.getSupplierName())
                .orderByDesc(SupplierBlacklist::getCreateTime);
        Page<SupplierBlacklist> page = supplierBlacklistMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(SupplierBlacklistConverter::toVO));
    }

    @Override
    public SupplierBlacklistVO getDetail(Long id) {
        SupplierBlacklist entity = supplierBlacklistMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return SupplierBlacklistConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierBlacklistCreateDTO dto) {
        Long count = supplierBlacklistMapper.selectCount(new LambdaQueryWrapper<SupplierBlacklist>()
                .eq(SupplierBlacklist::getSupplierId, dto.getSupplierId())
                .eq(SupplierBlacklist::getStatus, SupplierBlacklistStatusEnum.ACTIVE.getCode()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "该供应商已在黑名单中");
        }
        SupplierBlacklist entity = SupplierBlacklistConverter.toEntity(dto);
        supplierBlacklistMapper.insert(entity);

        // 已准入供应商加入黑名单后自动禁用账号
        disableSupplierIfApproved(dto.getSupplierId());

        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierBlacklistUpdateDTO dto) {
        SupplierBlacklist entity = supplierBlacklistMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        SupplierBlacklistConverter.updateEntity(entity, dto);
        supplierBlacklistMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        SupplierBlacklist entity = supplierBlacklistMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        entity.setStatus(SupplierBlacklistStatusEnum.RESOLVED.getCode());
        entity.setEndTime(LocalDateTime.now());
        supplierBlacklistMapper.updateById(entity);

        restoreSupplierStatusIfNeeded(entity.getSupplierId());
    }

    /**
     * 黑名单解除后恢复供应商状态（手动移除 + 定时任务到期共用）
     * 只有当该供应商没有其他活跃黑名单时才恢复
     */
    @Override
    public void restoreSupplierStatusIfNeeded(Long supplierId) {
        Long activeCount = supplierBlacklistMapper.selectCount(
                new LambdaQueryWrapper<SupplierBlacklist>()
                        .eq(SupplierBlacklist::getSupplierId, supplierId)
                        .eq(SupplierBlacklist::getStatus, SupplierBlacklistStatusEnum.ACTIVE.getCode()));
        if (activeCount > 0) {
            return;
        }

        SupplierInfo supplier = supplierInfoMapper.selectById(supplierId);
        if (supplier == null) {
            return;
        }
        supplier.setStatus(SupplierStatusEnum.APPROVED.getCode());
        supplierInfoMapper.updateById(supplier);

        sysUserMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SysUser>()
                .eq("supplier_id", supplierId)
                .set("status", 1));
    }

    /**
     * 已准入供应商加入黑名单后自动禁用供应商及其关联账号
     */
    private void disableSupplierIfApproved(Long supplierId) {
        SupplierInfo supplier = supplierInfoMapper.selectById(supplierId);
        if (supplier != null && SupplierStatusEnum.APPROVED.getCode().equals(supplier.getStatus())) {
            // 禁用供应商
            supplier.setStatus(SupplierStatusEnum.DISABLED.getCode());
            supplierInfoMapper.updateById(supplier);

            // 禁用关联的供应商用户账号
            sysUserMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SysUser>()
                    .eq("supplier_id", supplierId)
                    .set("status", 0));
        }
    }
}