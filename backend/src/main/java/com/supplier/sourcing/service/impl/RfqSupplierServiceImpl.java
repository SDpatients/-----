package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.RfqSupplierConverter;
import com.supplier.sourcing.dto.RfqSupplierCreateDTO;
import com.supplier.sourcing.entity.RfqSupplier;
import com.supplier.sourcing.mapper.RfqSupplierMapper;
import com.supplier.sourcing.query.RfqSupplierQuery;
import com.supplier.sourcing.service.RfqSupplierService;
import com.supplier.sourcing.vo.RfqSupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RfqSupplierServiceImpl implements RfqSupplierService {

    private final RfqSupplierMapper rfqSupplierMapper;

    @Override
    public PageResult<RfqSupplierVO> page(RfqSupplierQuery query) {
        LambdaQueryWrapper<RfqSupplier> wrapper = new LambdaQueryWrapper<RfqSupplier>()
                .eq(query.getRfqId() != null, RfqSupplier::getRfqId, query.getRfqId())
                .eq(query.getSupplierId() != null, RfqSupplier::getSupplierId, query.getSupplierId())
                .eq(query.getInviteStatus() != null, RfqSupplier::getInviteStatus, query.getInviteStatus())
                .orderByDesc(RfqSupplier::getCreateTime);
        Page<RfqSupplier> page = rfqSupplierMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(RfqSupplierConverter::toVO));
    }

    @Override
    public RfqSupplierVO getDetail(Long id) {
        RfqSupplier entity = rfqSupplierMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return RfqSupplierConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RfqSupplierCreateDTO dto) {
        RfqSupplier entity = RfqSupplierConverter.toEntity(dto);
        rfqSupplierMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RfqSupplier entity = rfqSupplierMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        rfqSupplierMapper.deleteById(entity.getId());
    }
}