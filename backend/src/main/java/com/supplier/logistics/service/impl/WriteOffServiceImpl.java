package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.dto.WriteOffCreateDTO;
import com.supplier.logistics.entity.WriteOff;
import com.supplier.logistics.mapper.WriteOffMapper;
import com.supplier.logistics.query.WriteOffQuery;
import com.supplier.logistics.service.WriteOffService;
import com.supplier.logistics.vo.WriteOffVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class WriteOffServiceImpl implements WriteOffService {
    private final WriteOffMapper writeOffMapper;
    private static final AtomicInteger sequence = new AtomicInteger(0);

    @Override
    public PageResult<WriteOffVO> page(WriteOffQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<WriteOff> wrapper = new LambdaQueryWrapper<WriteOff>()
                .eq(supplierId != null, WriteOff::getSupplierId, supplierId)
                .eq(StringUtils.hasText(query.getWriteOffType()), WriteOff::getWriteOffType, query.getWriteOffType())
                .eq(query.getStatus() != null, WriteOff::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(WriteOff::getWriteOffNo, query.getKeyword())
                        .or().like(WriteOff::getAsnNo, query.getKeyword())
                        .or().like(WriteOff::getOrderNo, query.getKeyword()))
                .orderByDesc(WriteOff::getCreateTime);
        Page<WriteOff> page = writeOffMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WriteOffCreateDTO dto) {
        WriteOff entity = new WriteOff();
        entity.setWriteOffNo(generateWriteOffNo());
        entity.setNoticeId(dto.getNoticeId());
        entity.setAsnNo(dto.getAsnNo());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderNo(dto.getOrderNo());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setWriteOffType(dto.getWriteOffType());
        entity.setAmount(dto.getAmount());
        entity.setReason(dto.getReason());
        entity.setStatus(0);
        entity.setWriteOffTime(LocalDateTime.now());
        entity.setRemark(dto.getRemark());
        writeOffMapper.insert(entity);
        return entity.getId();
    }

    private String generateWriteOffNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = sequence.incrementAndGet() % 10000;
        return "WO" + datePart + String.format("%04d", seq);
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

    private WriteOffVO toVO(WriteOff e) {
        WriteOffVO vo = new WriteOffVO();
        vo.setId(e.getId());
        vo.setWriteOffNo(e.getWriteOffNo());
        vo.setNoticeId(e.getNoticeId());
        vo.setAsnNo(e.getAsnNo());
        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(e.getOrderNo());
        vo.setSupplierId(e.getSupplierId());
        vo.setSupplierName(e.getSupplierName());
        vo.setWriteOffType(e.getWriteOffType());
        vo.setAmount(e.getAmount());
        vo.setReason(e.getReason());
        vo.setStatus(e.getStatus());
        vo.setWriteOffTime(e.getWriteOffTime());
        vo.setRemark(e.getRemark());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}
