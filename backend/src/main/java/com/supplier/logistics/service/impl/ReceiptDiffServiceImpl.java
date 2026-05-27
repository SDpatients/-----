package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.dto.ReceiptDiffApproveDTO;
import com.supplier.logistics.entity.ReceiptDiff;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.ReceiptDiffMapper;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.logistics.query.ReceiptDiffQuery;
import com.supplier.logistics.service.ReceiptDiffService;
import com.supplier.logistics.vo.ReceiptDiffVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceiptDiffServiceImpl implements ReceiptDiffService {

    private final ReceiptDiffMapper receiptDiffMapper;
    private final ReceiptRecordMapper receiptRecordMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResult<ReceiptDiffVO> page(ReceiptDiffQuery query) {
        LambdaQueryWrapper<ReceiptDiff> wrapper = new LambdaQueryWrapper<ReceiptDiff>()
                .eq(query.getRecordId() != null, ReceiptDiff::getRecordId, query.getRecordId())
                .eq(query.getNoticeId() != null, ReceiptDiff::getNoticeId, query.getNoticeId())
                .eq(query.getStatus() != null, ReceiptDiff::getStatus, query.getStatus())
                .orderByDesc(ReceiptDiff::getCreateTime);
        Page<ReceiptDiff> page = receiptDiffMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public ReceiptDiffVO getDetail(Long id) {
        ReceiptDiff diff = receiptDiffMapper.selectById(id);
        if (diff == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(diff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "收货差异", businessType = "receipt_diff", action = "收货调整", businessIdExpr = "#recordId")
    public Long adjust(Long recordId, ReceiptAdjustDTO dto) {
        ReceiptRecord record = receiptRecordMapper.selectById(recordId);
        if (record == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "收货记录不存在");
        }
        if (!Integer.valueOf(1).equals(record.getReceiptStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "仅已确认的收货记录可调整");
        }

        ReceiptDiff diff = new ReceiptDiff();
        diff.setRecordId(recordId);
        diff.setNoticeId(record.getNoticeId());
        diff.setMaterialCode(record.getMaterialCode());
        diff.setMaterialName(record.getMaterialName());
        diff.setPlanQty(record.getPlanQty());
        diff.setReceiptQty(record.getReceiptQty());
        diff.setDiffQty(dto.getDiffQty());
        diff.setDiffReason(dto.getDiffReason());
        diff.setHandleMethod(dto.getHandleMethod());
        diff.setHandleRemark(dto.getHandleRemark());
        diff.setStatus(0);
        diff.setRemark(dto.getRemark());
        receiptDiffMapper.insert(diff);

        domainEventPublisher.publish("supplier.delivery", "delivery.receipt.adjusted",
                DomainEvent.builder()
                        .eventType("delivery.receipt.adjusted")
                        .data(Map.of("businessId", diff.getId(), "recordId", recordId,
                                "diffQty", dto.getDiffQty(), "handleMethod", dto.getHandleMethod()))
                        .build());
        return diff.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, ReceiptDiffApproveDTO dto) {
        ReceiptDiff diff = receiptDiffMapper.selectById(id);
        if (diff == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "收货差异记录不存在");
        }
        if (!Integer.valueOf(0).equals(diff.getStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前差异记录不允许审批");
        }
        diff.setStatus(1); // 已审批
        diff.setHandleRemark(dto.getApproveRemark());
        receiptDiffMapper.updateById(diff);

        // 审批通过后更新收货记录
        ReceiptRecord record = receiptRecordMapper.selectById(diff.getRecordId());
        if (record != null) {
            record.setReceiptQty(diff.getReceiptQty());
            record.setRejectQty(diff.getDiffQty());
            record.setRemark("差异调整已审批: " + (dto.getApproveRemark() != null ? dto.getApproveRemark() : ""));
            receiptRecordMapper.updateById(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, ReceiptDiffApproveDTO dto) {
        ReceiptDiff diff = receiptDiffMapper.selectById(id);
        if (diff == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "收货差异记录不存在");
        }
        if (!Integer.valueOf(0).equals(diff.getStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前差异记录不允许审批");
        }
        diff.setStatus(2); // 已驳回
        diff.setHandleRemark(dto.getApproveRemark());
        receiptDiffMapper.updateById(diff);
    }

    private ReceiptDiffVO toVO(ReceiptDiff e) {
        ReceiptDiffVO vo = new ReceiptDiffVO();
        vo.setId(e.getId()); vo.setRecordId(e.getRecordId()); vo.setNoticeId(e.getNoticeId());
        vo.setMaterialCode(e.getMaterialCode()); vo.setMaterialName(e.getMaterialName());
        vo.setPlanQty(e.getPlanQty()); vo.setReceiptQty(e.getReceiptQty()); vo.setDiffQty(e.getDiffQty());
        vo.setDiffReason(e.getDiffReason()); vo.setHandleMethod(e.getHandleMethod());
        vo.setHandleRemark(e.getHandleRemark()); vo.setStatus(e.getStatus()); vo.setRemark(e.getRemark());
        return vo;
    }
}