package com.supplier.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.portal.vo.DashboardMetricVO;
import com.supplier.portal.vo.DashboardRiskVO;
import com.supplier.portal.vo.DashboardTrendVO;
import com.supplier.portal.vo.SupplierPerformanceVO;
import com.supplier.quality.entity.QualityInspection;
import com.supplier.quality.mapper.QualityInspectionMapper;
import com.supplier.portal.service.DashboardService;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.entity.SupplierPerformance;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.mapper.SupplierPerformanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final QualityInspectionMapper qualityInspectionMapper;
    private final ReconciliationMapper reconciliationMapper;
    private final SupplierPerformanceMapper supplierPerformanceMapper;

    @Override
    public List<DashboardMetricVO> metrics() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;
        return List.of(
                new DashboardMetricVO("订单总数", purchaseOrderMapper.selectCount(orderScope(supplierId)), "单"),
                new DashboardMetricVO("待确认订单", purchaseOrderMapper.selectCount(orderScope(supplierId).eq(PurchaseOrder::getOrderStatus, 1)), "单"),
                new DashboardMetricVO("送货通知", deliveryNoticeMapper.selectCount(deliveryScope(supplierId)), "单"),
                new DashboardMetricVO("对账单", reconciliationMapper.selectCount(reconScope(supplierId)), "单")
        );
    }

    @Override
    public List<DashboardTrendVO> trends() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;
        List<DashboardTrendVO> list = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = now.minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            Long orderCount = purchaseOrderMapper.selectCount(orderScope(supplierId).ge(PurchaseOrder::getOrderDate, start).le(PurchaseOrder::getOrderDate, end));
            Long deliveryCount = deliveryNoticeMapper.selectCount(deliveryScope(supplierId).ge(DeliveryNotice::getPlanDeliveryDate, start).le(DeliveryNotice::getPlanDeliveryDate, end));
            Long qualityCount = qualityInspectionMapper.selectCount(qualityScope(supplierId).eq(QualityInspection::getInspectResult, 2));
            Long reconCount = reconciliationMapper.selectCount(reconScope(supplierId).eq(Reconciliation::getReconPeriod, month.toString()));
            list.add(new DashboardTrendVO(month.toString(), orderCount, deliveryCount, qualityCount, reconCount));
        }
        return list;
    }

    @Override
    public List<DashboardRiskVO> risks() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;
        return List.of(
                new DashboardRiskVO("order_pending", "待确认订单积压", purchaseOrderMapper.selectCount(orderScope(supplierId).eq(PurchaseOrder::getOrderStatus, 1)), "warning"),
                new DashboardRiskVO("delivery_delay", "计划送货已逾期", deliveryNoticeMapper.selectCount(
                        deliveryScope(supplierId)
                                .lt(DeliveryNotice::getPlanDeliveryDate, LocalDate.now())
                                .lt(DeliveryNotice::getDeliveryStatus, 4)
                                .isNull(DeliveryNotice::getActualDeliveryDate)), "danger"),
                new DashboardRiskVO("quality_issue", "不合格质检单", qualityInspectionMapper.selectCount(
                        qualityScope(supplierId).in(QualityInspection::getInspectResult, 2, 3)), "danger"),
                new DashboardRiskVO("recon_dispute", "有异议对账单", reconciliationMapper.selectCount(reconScope(supplierId).eq(Reconciliation::getReconStatus, 3)), "warning")
        );
    }

    @Override
    public List<SupplierPerformanceVO> supplierPerformance() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;
        YearMonth currentPeriod = YearMonth.now();
        LambdaQueryWrapper<SupplierPerformance> perfWrapper =
                new LambdaQueryWrapper<SupplierPerformance>()
                        .eq(supplierId != null, SupplierPerformance::getSupplierId, supplierId)
                        .eq(SupplierPerformance::getEvaluatePeriod, currentPeriod.toString())
                        .orderByDesc(SupplierPerformance::getTotalScore);
        return supplierPerformanceMapper.selectList(perfWrapper).stream()
                .map(p -> new SupplierPerformanceVO(
                        p.getSupplierId(), null,
                        p.getOntimeRate(), p.getQualifiedRate(),
                        p.getDeliveryScore(), p.getTotalScore().intValue()))
                .toList();
    }

    private LambdaQueryWrapper<PurchaseOrder> orderScope(Long supplierId) {
        return new LambdaQueryWrapper<PurchaseOrder>().eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId);
    }

    private LambdaQueryWrapper<DeliveryNotice> deliveryScope(Long supplierId) {
        return new LambdaQueryWrapper<DeliveryNotice>().eq(supplierId != null, DeliveryNotice::getSupplierId, supplierId);
    }

    private LambdaQueryWrapper<Reconciliation> reconScope(Long supplierId) {
        return new LambdaQueryWrapper<Reconciliation>().eq(supplierId != null, Reconciliation::getSupplierId, supplierId);
    }

    private LambdaQueryWrapper<QualityInspection> qualityScope(Long supplierId) {
        return new LambdaQueryWrapper<QualityInspection>().eq(supplierId != null, QualityInspection::getSupplierId, supplierId);
    }
}
