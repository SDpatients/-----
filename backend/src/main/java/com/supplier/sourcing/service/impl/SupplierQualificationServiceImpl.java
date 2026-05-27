package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.converter.SupplierQualificationConverter;
import com.supplier.sourcing.dto.SupplierQualificationCreateDTO;
import com.supplier.sourcing.dto.SupplierQualificationUpdateDTO;
import com.supplier.sourcing.entity.SupplierQualification;
import com.supplier.sourcing.mapper.SupplierQualificationMapper;
import com.supplier.sourcing.query.SupplierQualificationQuery;
import com.supplier.sourcing.service.SupplierQualificationService;
import com.supplier.sourcing.vo.SupplierQualificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupplierQualificationServiceImpl implements SupplierQualificationService {

    private final SupplierQualificationMapper supplierQualificationMapper;

    @Override
    public PageResult<SupplierQualificationVO> page(SupplierQualificationQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<SupplierQualification> wrapper = new LambdaQueryWrapper<SupplierQualification>()
                .eq(supplierId != null, SupplierQualification::getSupplierId, supplierId)
                .eq(query.getStatus() != null, SupplierQualification::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getQualName()), SupplierQualification::getQualName, query.getQualName())
                .orderByDesc(SupplierQualification::getCreateTime);
        Page<SupplierQualification> page = supplierQualificationMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(SupplierQualificationConverter::toVO));
    }

    @Override
    public SupplierQualificationVO getDetail(Long id) {
        SupplierQualification entity = getWithScope(id);
        return SupplierQualificationConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierQualificationCreateDTO dto) {
        SupplierQualification entity = SupplierQualificationConverter.toEntity(dto);
        supplierQualificationMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierQualificationUpdateDTO dto) {
        SupplierQualification entity = getWithScope(id);
        SupplierQualificationConverter.updateEntity(entity, dto);
        supplierQualificationMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SupplierQualification entity = getWithScope(id);
        supplierQualificationMapper.deleteById(entity.getId());
    }

    private SupplierQualification getWithScope(Long id) {
        SupplierQualification entity = supplierQualificationMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !entity.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return entity;
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