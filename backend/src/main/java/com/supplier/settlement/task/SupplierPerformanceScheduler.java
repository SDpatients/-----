package com.supplier.settlement.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.quality.entity.NonconformanceReport;
import com.supplier.quality.enums.NcrStatusEnum;
import com.supplier.quality.mapper.NonconformanceReportMapper;
import com.supplier.settlement.entity.SupplierPerformance;
import com.supplier.settlement.mapper.SupplierPerformanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 供应商绩效定时计算
 * 每月初计算上月供应商绩效评分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierPerformanceScheduler {

    private final SupplierPerformanceMapper performanceMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final ReceiptRecordMapper receiptRecordMapper;
    private final NonconformanceReportMapper ncrMapper;

    private static final BigDecimal FULL_SCORE = new BigDecimal("100");

    /**
     * 每月1号凌晨 01:00 执行供应商绩效计算
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void calculateMonthlyPerformance() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = now.minusMonths(1);
        endDate = endDate.withDayOfMonth(endDate.lengthOfMonth());

        String period = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("[绩效计算] 开始计算 {} 月供应商绩效", period);

        // 统计有订单的供应商
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .ge(PurchaseOrder::getOrderDate, startDate)
                        .le(PurchaseOrder::getOrderDate, endDate)
        );

        Set<Long> allSupplierIds = new HashSet<>();
        for (PurchaseOrder order : orders) {
            if (order.getSupplierId() != null) {
                allSupplierIds.add(order.getSupplierId());
            }
        }

        // 也纳入有NCR的供应商
        List<NonconformanceReport> ncrs = ncrMapper.selectList(
                new LambdaQueryWrapper<NonconformanceReport>()
                        .ge(NonconformanceReport::getCreateTime, startDate.atStartOfDay())
                        .le(NonconformanceReport::getCreateTime, endDate.plusDays(1).atStartOfDay())
        );
        for (NonconformanceReport ncr : ncrs) {
            if (ncr.getSupplierId() != null) {
                allSupplierIds.add(ncr.getSupplierId());
            }
        }

        if (allSupplierIds.isEmpty()) {
            log.info("[绩效计算] 当月无活跃供应商，跳过");
            return;
        }

        int count = 0;
        for (Long supplierId : allSupplierIds) {
            try {
                SupplierPerformance perf = calculateSupplier(supplierId, startDate, endDate, period);
                if (perf != null) {
                    performanceMapper.insert(perf);
                    count++;
                }
            } catch (Exception e) {
                log.error("[绩效计算] 供应商 {} 绩效计算失败", supplierId, e);
            }
        }
        log.info("[绩效计算] {} 月绩效计算完成，共生成 {} 条绩效记录", period, count);
    }

    private SupplierPerformance calculateSupplier(Long supplierId, LocalDate start, LocalDate end, String period) {
        // 1. 质量评分：基于 NCR 数量
        Long ncrCount = ncrMapper.selectCount(
                new LambdaQueryWrapper<NonconformanceReport>()
                        .eq(NonconformanceReport::getSupplierId, supplierId)
                        .ge(NonconformanceReport::getCreateTime, start.atStartOfDay())
                        .le(NonconformanceReport::getCreateTime, end.plusDays(1).atStartOfDay())
        );
        BigDecimal qualityScore = FULL_SCORE.subtract(new BigDecimal("5").multiply(new BigDecimal(ncrCount)));
        if (qualityScore.compareTo(BigDecimal.ZERO) < 0) qualityScore = BigDecimal.ZERO;

        // 2. 交付评分：基于收货准时率
        List<ReceiptRecord> receipts = receiptRecordMapper.selectList(
                new LambdaQueryWrapper<ReceiptRecord>()
                        .eq(ReceiptRecord::getSupplierId, supplierId)
                        .ge(ReceiptRecord::getReceiptTime, start.atStartOfDay())
                        .le(ReceiptRecord::getReceiptTime, end.plusDays(1).atStartOfDay())
        );
        BigDecimal ontimeRate;
        if (receipts.isEmpty()) {
            ontimeRate = new BigDecimal("100");
        } else {
            long ontimeCount = receipts.stream()
                    .filter(r -> r.getReceiptTime() != null &&
                            r.getReceiptTime().toLocalDate().compareTo(end) <= 0)
                    .count();
            ontimeRate = new BigDecimal(ontimeCount)
                    .multiply(FULL_SCORE)
                    .divide(new BigDecimal(receipts.size()), 2, RoundingMode.HALF_UP);
        }
        BigDecimal deliveryScore = ontimeRate;

        // 3. 服务评分：默认80分，有NCR扣分
        BigDecimal serviceScore = new BigDecimal("80");
        if (ncrCount > 0) {
            serviceScore = serviceScore.subtract(new BigDecimal("10").multiply(new BigDecimal(ncrCount)));
            if (serviceScore.compareTo(BigDecimal.ZERO) < 0) serviceScore = BigDecimal.ZERO;
        }

        // 4. 价格评分：默认满分，可后续扩展
        BigDecimal priceScore = FULL_SCORE;

        // 5. 综合评分 = (质量*0.3 + 交付*0.3 + 服务*0.2 + 价格*0.2)
        BigDecimal qualifiedRate = qualityScore;
        BigDecimal totalScore = qualityScore.multiply(new BigDecimal("0.3"))
                .add(deliveryScore.multiply(new BigDecimal("0.3")))
                .add(serviceScore.multiply(new BigDecimal("0.2")))
                .add(priceScore.multiply(new BigDecimal("0.2")))
                .setScale(2, RoundingMode.HALF_UP);

        SupplierPerformance perf = new SupplierPerformance();
        perf.setSupplierId(supplierId);
        perf.setEvaluatePeriod(period);
        perf.setQualityScore(qualityScore);
        perf.setDeliveryScore(deliveryScore);
        perf.setServiceScore(serviceScore);
        perf.setPriceScore(priceScore);
        perf.setTotalScore(totalScore);
        perf.setQualifiedRate(qualifiedRate);
        perf.setOntimeRate(ontimeRate);
        perf.setEvaluateTime(LocalDateTime.now());
        perf.setRemark(String.format("系统自动计算 %s 月绩效", period));

        return perf;
    }
}