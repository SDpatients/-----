package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.converter.InvoiceConverter;
import com.supplier.settlement.dto.InvoiceActionDTO;
import com.supplier.settlement.dto.InvoiceCreateDTO;
import com.supplier.settlement.dto.InvoiceUploadDTO;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.enums.InvoiceStatusEnum;
import com.supplier.settlement.mapper.InvoiceMapper;
import com.supplier.settlement.query.InvoiceQuery;
import com.supplier.settlement.service.InvoiceService;
import com.supplier.settlement.vo.InvoiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceMapper invoiceMapper;

    @Override
    public PageResult<InvoiceVO> page(InvoiceQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<Invoice>()
                .eq(supplierId != null, Invoice::getSupplierId, supplierId)
                .eq(query.getInvoiceStatus() != null, Invoice::getInvoiceStatus, query.getInvoiceStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Invoice::getInvoiceNo, query.getKeyword())
                        .or()
                        .like(Invoice::getSupplierName, query.getKeyword()))
                .orderByDesc(Invoice::getCreateTime);
        Page<Invoice> page = invoiceMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(InvoiceConverter::toVO));
    }

    @Override
    public InvoiceVO getDetail(Long id) {
        Invoice entity = getWithDataScope(id);
        return InvoiceConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InvoiceCreateDTO dto) {
        Long count = invoiceMapper.selectCount(new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getInvoiceNo, dto.getInvoiceNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "发票号码已存在");
        }
        Invoice entity = InvoiceConverter.toEntity(dto);
        invoiceMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upload(Long id, InvoiceUploadDTO dto) {
        Invoice entity = getWithDataScope(id);
        if (!Integer.valueOf(InvoiceStatusEnum.PENDING.getCode()).equals(entity.getInvoiceStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前发票状态不允许上传");
        }
        entity.setInvoiceStatus(InvoiceStatusEnum.UPLOADED.getCode());
        entity.setFileId(dto.getFileId());
        entity.setReceiveTime(LocalDateTime.now());
        invoiceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verify(Long id, InvoiceActionDTO dto) {
        dto = normalizeAction(dto);
        Invoice entity = getWithDataScope(id);
        if (!Integer.valueOf(InvoiceStatusEnum.UPLOADED.getCode()).equals(entity.getInvoiceStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前发票状态不允许验真");
        }
        entity.setInvoiceStatus(InvoiceStatusEnum.VERIFIED.getCode());
        invoiceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void certify(Long id, InvoiceActionDTO dto) {
        dto = normalizeAction(dto);
        Invoice entity = getWithDataScope(id);
        if (!Integer.valueOf(InvoiceStatusEnum.VERIFIED.getCode()).equals(entity.getInvoiceStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前发票状态不允许认证");
        }
        entity.setInvoiceStatus(InvoiceStatusEnum.CERTIFIED.getCode());
        entity.setCertifyTime(LocalDateTime.now());
        invoiceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidInvoice(Long id, InvoiceActionDTO dto) {
        Invoice entity = getWithDataScope(id);
        if (Integer.valueOf(InvoiceStatusEnum.VOIDED.getCode()).equals(entity.getInvoiceStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "发票已作废");
        }
        entity.setInvoiceStatus(InvoiceStatusEnum.VOIDED.getCode());
        entity.setVoidTime(LocalDateTime.now());
        entity.setVoidReason(dto != null ? dto.getRemark() : null);
        invoiceMapper.updateById(entity);
    }

    private Invoice getWithDataScope(Long id) {
        Invoice entity = invoiceMapper.selectById(id);
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

    private InvoiceActionDTO normalizeAction(InvoiceActionDTO dto) {
        return dto == null ? new InvoiceActionDTO() : dto;
    }
}