package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.settlement.dto.ReconciliationCreateDTO;
import com.supplier.settlement.dto.ReconciliationDetailCreateDTO;
import com.supplier.settlement.dto.ReconciliationSummaryDTO;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.enums.DeductionStatusEnum;
import com.supplier.settlement.mapper.DeductionMapper;
import com.supplier.settlement.service.ReconciliationDetailService;
import com.supplier.settlement.service.ReconciliationService;
import com.supplier.settlement.service.ReconciliationSummaryService;
import com.supplier.settlement.vo.ReconciliationDetailSummaryVO;
import com.supplier.settlement.vo.ReconciliationSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 对账汇总服务实现（联动6: 对账周期 → 自动汇总收货/退货/扣款）
 * <p>
 * 跨模块联动: logistics + quality → settlement
 * 汇总逻辑: sum(收货金额) - sum(退货金额) - sum(扣款金额) = 应付金额
 */
@Service
@RequiredArgsConstructor
public class ReconciliationSummaryServiceImpl implements ReconciliationSummaryService {

    private final ReceiptRecordMapper receiptRecordMapper;
    private final DeductionMapper deductionMapper;
    private final ReconciliationService reconciliationService;
    private final ReconciliationDetailService reconciliationDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationSummaryVO summarize(ReconciliationSummaryDTO dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        Long supplierId = dto.getSupplierId();

        // 1) 汇总已确认的收货记录（receipt_status = 1）
        List<ReceiptRecord> receipts = queryConfirmedReceipts(supplierId, startDate, endDate);
        BigDecimal totalReceiptAmount = BigDecimal.ZERO;
        int receiptCount = 0;
        for (ReceiptRecord r : receipts) {
            totalReceiptAmount = totalReceiptAmount.add(
                    r.getReceiptQty() != null ? r.getReceiptQty() : BigDecimal.ZERO);
            receiptCount++;
        }

        // 2) 汇总退货（rejectQty > 0 的收货记录）
        BigDecimal totalReturnAmount = BigDecimal.ZERO;
        int returnCount = 0;
        for (ReceiptRecord r : receipts) {
            if (r.getRejectQty() != null && r.getRejectQty().compareTo(BigDecimal.ZERO) > 0) {
                totalReturnAmount = totalReturnAmount.add(r.getRejectQty());
                returnCount++;
            }
        }

        // 3) 汇总已确认的扣款
        List<Deduction> deductions = queryConfirmedDeductions(supplierId, startDate, endDate);
        BigDecimal totalDeductionAmount = BigDecimal.ZERO;
        int deductionCount = 0;
        for (Deduction d : deductions) {
            totalDeductionAmount = totalDeductionAmount.add(
                    d.getDeductionAmount() != null ? d.getDeductionAmount() : BigDecimal.ZERO);
            deductionCount++;
        }

        // 4) 计算应付金额
        BigDecimal payableAmount = totalReceiptAmount.subtract(totalReturnAmount).subtract(totalDeductionAmount);

        // 5) 创建对账单
        String reconNo = generateReconNo(dto.getReconPeriod());
        ReconciliationCreateDTO reconDto = new ReconciliationCreateDTO();
        reconDto.setReconNo(reconNo);
        reconDto.setSupplierId(supplierId);
        reconDto.setReconPeriod(dto.getReconPeriod());
        reconDto.setStartDate(startDate);
        reconDto.setEndDate(endDate);
        reconDto.setTotalAmount(payableAmount);
        reconDto.setRemark(dto.getRemark());
        Long reconId = reconciliationService.create(reconDto);

        // 6) 生成对账明细行
        List<ReconciliationDetailSummaryVO> detailSummaries = new ArrayList<>();

        // 6a) 收货明细行
        for (ReceiptRecord r : receipts) {
            ReconciliationDetailCreateDTO detailDto = new ReconciliationDetailCreateDTO();
            detailDto.setReconId(reconId);
            detailDto.setMaterialCode(r.getMaterialCode());
            detailDto.setMaterialName(r.getMaterialName());
            detailDto.setQuantity(r.getReceiptQty());
            detailDto.setConfirmedAmount(r.getReceiptQty());
            detailDto.setConfirmStatus(0);
            detailDto.setRemark("收货记录 ID:" + r.getId());
            reconciliationDetailService.create(detailDto);

            ReconciliationDetailSummaryVO summaryVO = new ReconciliationDetailSummaryVO();
            summaryVO.setSourceType("receipt");
            summaryVO.setMaterialCode(r.getMaterialCode());
            summaryVO.setMaterialName(r.getMaterialName());
            summaryVO.setQuantity(r.getReceiptQty());
            summaryVO.setAmount(r.getReceiptQty());
            summaryVO.setRemark("收货记录");
            detailSummaries.add(summaryVO);
        }

        // 6b) 退货明细行
        for (ReceiptRecord r : receipts) {
            if (r.getRejectQty() != null && r.getRejectQty().compareTo(BigDecimal.ZERO) > 0) {
                ReconciliationDetailCreateDTO detailDto = new ReconciliationDetailCreateDTO();
                detailDto.setReconId(reconId);
                detailDto.setMaterialCode(r.getMaterialCode());
                detailDto.setMaterialName(r.getMaterialName());
                detailDto.setQuantity(r.getRejectQty().negate());
                detailDto.setConfirmedAmount(r.getRejectQty().negate());
                detailDto.setDiffReason(r.getRejectReason());
                detailDto.setConfirmStatus(0);
                detailDto.setRemark("退货记录 ID:" + r.getId());
                reconciliationDetailService.create(detailDto);

                ReconciliationDetailSummaryVO summaryVO = new ReconciliationDetailSummaryVO();
                summaryVO.setSourceType("return");
                summaryVO.setMaterialCode(r.getMaterialCode());
                summaryVO.setMaterialName(r.getMaterialName());
                summaryVO.setQuantity(r.getRejectQty());
                summaryVO.setAmount(r.getRejectQty().negate());
                summaryVO.setDiffReason(r.getRejectReason());
                summaryVO.setRemark("退货记录");
                detailSummaries.add(summaryVO);
            }
        }

        // 6c) 扣款明细行
        for (Deduction d : deductions) {
            ReconciliationDetailCreateDTO detailDto = new ReconciliationDetailCreateDTO();
            detailDto.setReconId(reconId);
            detailDto.setMaterialName("扣款-" + d.getDeductionNo());
            detailDto.setQuantity(BigDecimal.ONE);
            detailDto.setOrderAmount(d.getDeductionAmount());
            detailDto.setConfirmedAmount(d.getDeductionAmount().negate());
            detailDto.setDiffReason(d.getDeductionReason());
            detailDto.setConfirmStatus(0);
            detailDto.setRemark("扣款记录 ID:" + d.getId() + ", 单号:" + d.getDeductionNo());
            reconciliationDetailService.create(detailDto);

            ReconciliationDetailSummaryVO summaryVO = new ReconciliationDetailSummaryVO();
            summaryVO.setSourceType("deduction");
            summaryVO.setSourceNo(d.getDeductionNo());
            summaryVO.setMaterialName("扣款");
            summaryVO.setQuantity(BigDecimal.ONE);
            summaryVO.setAmount(d.getDeductionAmount().negate());
            summaryVO.setDiffReason(d.getDeductionReason());
            summaryVO.setRemark(d.getDeductionReason());
            detailSummaries.add(summaryVO);
        }

        // 7) 构建返回结果
        ReconciliationSummaryVO result = new ReconciliationSummaryVO();
        result.setReconId(reconId);
        result.setReconNo(reconNo);
        result.setSupplierId(supplierId);
        result.setReconPeriod(dto.getReconPeriod());
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTotalReceiptAmount(totalReceiptAmount);
        result.setTotalReturnAmount(totalReturnAmount);
        result.setTotalDeductionAmount(totalDeductionAmount);
        result.setPayableAmount(payableAmount);
        result.setReceiptCount(receiptCount);
        result.setReturnCount(returnCount);
        result.setDeductionCount(deductionCount);
        result.setDetails(detailSummaries);
        return result;
    }

    /**
     * 查询指定供应商在日期范围内已确认的收货记录
     */
    private List<ReceiptRecord> queryConfirmedReceipts(Long supplierId, LocalDate startDate, LocalDate endDate) {
        return receiptRecordMapper.selectList(
                new LambdaQueryWrapper<ReceiptRecord>()
                        .eq(ReceiptRecord::getSupplierId, supplierId)
                        .eq(ReceiptRecord::getReceiptStatus, 1) // 已确认
                        .ge(ReceiptRecord::getReceiptTime, startDate.atStartOfDay())
                        .le(ReceiptRecord::getReceiptTime, endDate.plusDays(1).atStartOfDay())
        );
    }

    /**
     * 查询指定供应商在日期范围内已确认/已入账的扣款记录
     */
    private List<Deduction> queryConfirmedDeductions(Long supplierId, LocalDate startDate, LocalDate endDate) {
        return deductionMapper.selectList(
                new LambdaQueryWrapper<Deduction>()
                        .eq(Deduction::getSupplierId, supplierId)
                        .in(Deduction::getDeductionStatus,
                                DeductionStatusEnum.CONFIRMED.getCode(),
                                DeductionStatusEnum.BOOKED.getCode())
                        .ge(Deduction::getCreateTime, startDate.atStartOfDay())
                        .le(Deduction::getCreateTime, endDate.plusDays(1).atStartOfDay())
        );
    }

    private String generateReconNo(String reconPeriod) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "REC" + datePart + String.format("%04d", System.currentTimeMillis() % 10000);
    }

    private void ensureSupplierExists(Long supplierId) {
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "供应商ID不能为空");
        }
    }
}