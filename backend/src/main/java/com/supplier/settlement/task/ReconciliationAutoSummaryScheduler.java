package com.supplier.settlement.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.entity.ReconciliationDetail;
import com.supplier.settlement.enums.DeductionStatusEnum;
import com.supplier.settlement.enums.ReconStatusEnum;
import com.supplier.settlement.mapper.DeductionMapper;
import com.supplier.settlement.mapper.ReconciliationDetailMapper;
import com.supplier.settlement.mapper.ReconciliationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对账周期自动汇总定时任务
 * 每月最后一天自动生成对账单，汇总收货/退货/扣款数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationAutoSummaryScheduler {

    private final ReconciliationMapper reconciliationMapper;
    private final ReconciliationDetailMapper detailMapper;
    private final ReceiptRecordMapper receiptRecordMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final DeductionMapper deductionMapper;
    private final DeliveryNoticeMapper deliveryNoticeMapper;

    /**
     * 每月最后一天 23:00 执行对账自动汇总
     */
    @Scheduled(cron = "0 0 23 L * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoSummaryReconciliation() {
        LocalDate now = LocalDate.now();
        // 只在实际月末执行
        if (now.getDayOfMonth() != now.lengthOfMonth()) {
            return;
        }
        log.info("[对账汇总] 开始执行月末对账汇总，当前日期: {}", now);

        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now;

        String reconPeriod = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                + " ~ " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 统计本月有收货的所有供应商
        List<ReceiptRecord> receipts = receiptRecordMapper.selectList(
                new LambdaQueryWrapper<ReceiptRecord>()
                        .ge(ReceiptRecord::getReceiptTime, startDate.atStartOfDay())
                        .le(ReceiptRecord::getReceiptTime, endDate.plusDays(1).atStartOfDay())
                        .isNotNull(ReceiptRecord::getSupplierId)
        );

        if (receipts.isEmpty()) {
            log.info("[对账汇总] 本月无收货记录，跳过汇总");
            return;
        }

        // 按供应商分组
        Map<Long, List<ReceiptRecord>> supplierReceiptMap = receipts.stream()
                .collect(Collectors.groupingBy(ReceiptRecord::getSupplierId));

        int reconCount = 0;
        for (Map.Entry<Long, List<ReceiptRecord>> entry : supplierReceiptMap.entrySet()) {
            Long supplierId = entry.getKey();
            List<ReceiptRecord> supplierReceipts = entry.getValue();

            // 检查本月是否已有对账单
            Long existingCount = reconciliationMapper.selectCount(
                    new LambdaQueryWrapper<Reconciliation>()
                            .eq(Reconciliation::getSupplierId, supplierId)
                            .ge(Reconciliation::getEndDate, startDate)
                            .le(Reconciliation::getEndDate, endDate)
            );
            if (existingCount > 0) {
                log.info("[对账汇总] 供应商 {} 本月已有对账单，跳过", supplierId);
                continue;
            }

            // 创建对账单头
            Reconciliation recon = new Reconciliation();
            recon.setReconNo("RECON" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%04d", reconCount + 1));
            recon.setSupplierId(supplierId);
            recon.setReconPeriod(reconPeriod);
            recon.setStartDate(startDate);
            recon.setEndDate(endDate);
            recon.setTotalAmount(BigDecimal.ZERO);
            recon.setConfirmedAmount(BigDecimal.ZERO);
            recon.setDiffAmount(BigDecimal.ZERO);
            recon.setReconStatus(ReconStatusEnum.DRAFT.getCode());
            recon.setRemark(String.format("系统自动生成的 %s 对账单", reconPeriod));
            reconciliationMapper.insert(recon);

            // 生成对账明细
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ReceiptRecord receipt : supplierReceipts) {
                ReconciliationDetail detail = new ReconciliationDetail();
                detail.setReconId(recon.getId());
                detail.setDeliveryId(receipt.getDeliveryId());
                detail.setMaterialCode(receipt.getMaterialCode());
                detail.setMaterialName(receipt.getMaterialName());
                detail.setQuantity(receipt.getReceiptQty());
                detail.setConfirmedAmount(receipt.getReceiptQty() != null && receipt.getReceiptQty().compareTo(BigDecimal.ZERO) > 0
                        ? receipt.getReceiptQty() : BigDecimal.ZERO);
                detail.setDiffAmount(BigDecimal.ZERO);
                detail.setConfirmStatus(0);

                // 关联订单信息
                if (receipt.getNoticeId() != null) {
                    DeliveryNotice notice = deliveryNoticeMapper.selectById(receipt.getNoticeId());
                    if (notice != null && notice.getOrderId() != null) {
                        PurchaseOrder order = purchaseOrderMapper.selectById(notice.getOrderId());
                        if (order != null) {
                            detail.setOrderId(order.getId());
                            detail.setOrderNo(order.getOrderNo());
                            detail.setUnitPrice(order.getTotalAmount());
                        }
                    }
                    detail.setDeliveryNo(notice != null ? notice.getNoticeNo() : null);
                }
                detailMapper.insert(detail);

                totalAmount = totalAmount.add(receipt.getReceiptQty() != null ? receipt.getReceiptQty() : BigDecimal.ZERO);
            }

            // 汇总扣款
            List<Deduction> deductions = deductionMapper.selectList(
                    new LambdaQueryWrapper<Deduction>()
                            .eq(Deduction::getSupplierId, supplierId)
                            .eq(Deduction::getDeductionStatus, DeductionStatusEnum.CONFIRMED.getCode())
                            .ge(Deduction::getCreateTime, startDate.atStartOfDay())
                            .le(Deduction::getCreateTime, endDate.plusDays(1).atStartOfDay())
            );

            BigDecimal deductionTotal = deductions.stream()
                    .map(d -> d.getDeductionAmount() != null ? d.getDeductionAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 更新对账单总金额
            recon.setTotalAmount(totalAmount.subtract(deductionTotal));
            recon.setDiffAmount(BigDecimal.ZERO);
            reconciliationMapper.updateById(recon);

            log.info("[对账汇总] 供应商 {} 对账单 {} 创建完成，总金额: {}，含 {} 条收货记录，{} 条扣款",
                    supplierId, recon.getReconNo(), recon.getTotalAmount(),
                    supplierReceipts.size(), deductions.size());
            reconCount++;
        }

        log.info("[对账汇总] 月末对账汇总完成，共生成 {} 张对账单", reconCount);
    }
}