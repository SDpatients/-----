package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.RfqSupplierConverter;
import com.supplier.sourcing.dto.RfqSupplierCreateDTO;
import com.supplier.sourcing.entity.RfqSupplier;
import com.supplier.sourcing.entity.SupplierInfo;
import com.supplier.sourcing.mapper.RfqSupplierMapper;
import com.supplier.sourcing.mapper.SupplierInfoMapper;
import com.supplier.sourcing.query.RfqSupplierQuery;
import com.supplier.sourcing.service.RfqSupplierService;
import com.supplier.sourcing.vo.RfqSupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqSupplierServiceImpl implements RfqSupplierService {

    private final RfqSupplierMapper rfqSupplierMapper;
    private final SupplierInfoMapper supplierInfoMapper;

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
    @AuditLog(module = "询价单", businessType = "rfq_supplier", action = "邀请供应商", businessIdExpr = "#rfqId")
    public void inviteSuppliers(Long rfqId, List<Long> supplierIds) {
        List<Long> existingIds = rfqSupplierMapper.selectList(
                new LambdaQueryWrapper<RfqSupplier>()
                        .eq(RfqSupplier::getRfqId, rfqId)
                        .in(RfqSupplier::getSupplierId, supplierIds))
                .stream()
                .map(RfqSupplier::getSupplierId)
                .collect(Collectors.toList());

        List<SupplierInfo> suppliers = supplierInfoMapper.selectList(
                new LambdaQueryWrapper<SupplierInfo>()
                        .in(SupplierInfo::getId, supplierIds));
        Map<Long, String> nameMap = suppliers.stream()
                .collect(Collectors.toMap(SupplierInfo::getId, SupplierInfo::getSupplierName));

        List<RfqSupplier> list = supplierIds.stream()
                .filter(sid -> !existingIds.contains(sid))
                .map(sid -> {
                    RfqSupplier entity = new RfqSupplier();
                    entity.setRfqId(rfqId);
                    entity.setSupplierId(sid);
                    entity.setSupplierName(nameMap.getOrDefault(sid, null));
                    entity.setInviteStatus(0);
                    entity.setInviteTime(LocalDateTime.now());
                    return entity;
                })
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            list.forEach(rfqSupplierMapper::insert);
        }
    }

    @Override
    public List<RfqSupplierVO> getInvitedByRfqId(Long rfqId) {
        List<RfqSupplier> list = rfqSupplierMapper.selectList(
                new LambdaQueryWrapper<RfqSupplier>()
                        .eq(RfqSupplier::getRfqId, rfqId));
        return list.stream().map(RfqSupplierConverter::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "询价单", businessType = "rfq_supplier", action = "移除供应商", businessIdExpr = "#id")
    public void delete(Long id) {
        RfqSupplier entity = rfqSupplierMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        rfqSupplierMapper.deleteById(entity.getId());
    }
}