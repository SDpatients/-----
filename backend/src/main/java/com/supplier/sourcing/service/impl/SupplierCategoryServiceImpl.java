package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.SupplierCategoryConverter;
import com.supplier.sourcing.dto.SupplierCategoryCreateDTO;
import com.supplier.sourcing.dto.SupplierCategoryUpdateDTO;
import com.supplier.sourcing.entity.SupplierCategory;
import com.supplier.sourcing.mapper.SupplierCategoryMapper;
import com.supplier.sourcing.query.SupplierCategoryQuery;
import com.supplier.sourcing.service.SupplierCategoryService;
import com.supplier.sourcing.vo.SupplierCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierCategoryServiceImpl implements SupplierCategoryService {

    private final SupplierCategoryMapper supplierCategoryMapper;

    @Override
    public PageResult<SupplierCategoryVO> page(SupplierCategoryQuery query) {
        LambdaQueryWrapper<SupplierCategory> wrapper = new LambdaQueryWrapper<SupplierCategory>()
                .eq(query.getStatus() != null, SupplierCategory::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getKeyword()), SupplierCategory::getCategoryName, query.getKeyword())
                .orderByAsc(SupplierCategory::getSort)
                .orderByDesc(SupplierCategory::getCreateTime);
        Page<SupplierCategory> page = supplierCategoryMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(SupplierCategoryConverter::toVO));
    }

    @Override
    public List<SupplierCategoryVO> listAll() {
        LambdaQueryWrapper<SupplierCategory> wrapper = new LambdaQueryWrapper<SupplierCategory>()
                .eq(SupplierCategory::getStatus, 1)
                .orderByAsc(SupplierCategory::getSort);
        return supplierCategoryMapper.selectList(wrapper).stream()
                .map(SupplierCategoryConverter::toVO).toList();
    }

    @Override
    public SupplierCategoryVO getDetail(Long id) {
        SupplierCategory entity = supplierCategoryMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return SupplierCategoryConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierCategoryCreateDTO dto) {
        Long count = supplierCategoryMapper.selectCount(new LambdaQueryWrapper<SupplierCategory>()
                .eq(SupplierCategory::getCategoryCode, dto.getCategoryCode()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "分类编码已存在");
        }
        SupplierCategory entity = SupplierCategoryConverter.toEntity(dto);
        supplierCategoryMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierCategoryUpdateDTO dto) {
        SupplierCategory entity = supplierCategoryMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        SupplierCategoryConverter.updateEntity(entity, dto);
        supplierCategoryMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SupplierCategory entity = supplierCategoryMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        supplierCategoryMapper.deleteById(id);
    }
}