package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.converter.SupplierPerformanceConverter;
import com.supplier.settlement.dto.SupplierPerformanceCreateDTO;
import com.supplier.settlement.dto.SupplierPerformanceUpdateDTO;
import com.supplier.settlement.entity.SupplierPerformance;
import com.supplier.settlement.mapper.SupplierPerformanceMapper;
import com.supplier.settlement.query.SupplierPerformanceQuery;
import com.supplier.settlement.service.SupplierPerformanceService;
import com.supplier.settlement.vo.SupplierPerformanceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupplierPerformanceServiceImpl implements SupplierPerformanceService {

    private final SupplierPerformanceMapper supplierPerformanceMapper;

    @Override
    public PageResult<SupplierPerformanceVO> page(SupplierPerformanceQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<SupplierPerformance> wrapper = new LambdaQueryWrapper<SupplierPerformance>()
                .eq(supplierId != null, SupplierPerformance::getSupplierId, supplierId)
                .eq(StringUtils.hasText(query.getEvaluatePeriod()), SupplierPerformance::getEvaluatePeriod, query.getEvaluatePeriod())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SupplierPerformance::getEvaluatePeriod, query.getKeyword())
                        .or()
                        .like(SupplierPerformance::getRemark, query.getKeyword()))
                .orderByDesc(SupplierPerformance::getEvaluatePeriod)
                .orderByDesc(SupplierPerformance::getCreateTime);
        Page<SupplierPerformance> page = supplierPerformanceMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(SupplierPerformanceConverter::toVO));
    }

    @Override
    public SupplierPerformanceVO getDetail(Long id) {
        SupplierPerformance entity = getWithDataScope(id);
        return SupplierPerformanceConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierPerformanceCreateDTO dto) {
        SupplierPerformance entity = SupplierPerformanceConverter.toEntity(dto);
        supplierPerformanceMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierPerformanceUpdateDTO dto) {
        SupplierPerformance entity = getWithDataScope(id);
        SupplierPerformanceConverter.updateEntity(entity, dto);
        supplierPerformanceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SupplierPerformance entity = getWithDataScope(id);
        supplierPerformanceMapper.deleteById(entity.getId());
    }

    private SupplierPerformance getWithDataScope(Long id) {
        SupplierPerformance entity = supplierPerformanceMapper.selectById(id);
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
        if (supplierId == null || (querySupplierId != null && !supplierId.equals(querySupplierId))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }
}