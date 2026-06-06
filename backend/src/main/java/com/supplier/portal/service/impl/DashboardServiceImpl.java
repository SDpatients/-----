package com.supplier.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.portal.vo.DashboardMetricVO;
import com.supplier.portal.vo.DashboardRiskVO;
import com.supplier.portal.vo.DashboardTrendVO;
import com.supplier.portal.service.DashboardService;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.RfqMapper;
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
    private final RfqMapper rfqMapper;
    private final QuoteMapper quoteMapper;

    @Override
    public List<DashboardMetricVO> metrics() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;

        if (supplierId != null) {
            // 供应商视角：待确认订单、未送达的物流、可参与的报价量
            return List.of(
                    new DashboardMetricVO("待确认订单",
                            purchaseOrderMapper.selectCount(orderScope(supplierId).eq(PurchaseOrder::getOrderStatus, 1)), "单", "实时刷新", "/supplier/orders"),
                    new DashboardMetricVO("未送达的物流",
                            deliveryNoticeMapper.selectCount(deliveryScope(supplierId).in(DeliveryNotice::getDeliveryStatus, 0, 1, 2)), "单", "实时刷新", "/supplier/deliveries"),
                    new DashboardMetricVO("可参与的报价量",
                            quoteMapper.selectCount(quoteScope(supplierId).eq(Quote::getQuoteStatus, 0)), "单", "来自业务聚合数据", "/supplier/rfq")
            );
        }

        // 采购方视角：进行中的询价单、收到的未处理报价、未送达的物流
        return List.of(
                new DashboardMetricVO("进行中的询价单",
                        rfqMapper.selectCount(rfqScope().in(Rfq::getRfqStatus, 1, 2)), "单", "来自业务聚合数据", "/purchasing/rfq"),
                new DashboardMetricVO("收到的未处理报价",
                        quoteMapper.selectCount(quoteScope(null).eq(Quote::getQuoteStatus, 0)), "单", "实时刷新", "/purchasing/quotes"),
                new DashboardMetricVO("未送达的物流",
                        deliveryNoticeMapper.selectCount(deliveryScope(null).in(DeliveryNotice::getDeliveryStatus, 0, 1, 2)), "单", "实时刷新", "/purchasing/asn")
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
            list.add(new DashboardTrendVO(month.toString(), orderCount, deliveryCount));
        }
        return list;
    }

    @Override
    public List<DashboardRiskVO> risks() {
        Long supplierId = SecurityUtils.isSupplierUser() ? SecurityUtils.getSupplierId() : null;
        LocalDate today = LocalDate.now();
        // 即将到期窗口：今天 ~ 未来2天
        LocalDate approachingStart = today.plusDays(1);
        LocalDate approachingEnd = today.plusDays(2);
        return List.of(
                new DashboardRiskVO("order_pending", "未确认订单量",
                        purchaseOrderMapper.selectCount(orderScope(supplierId).eq(PurchaseOrder::getOrderStatus, 1)),
                        "warning"),
                new DashboardRiskVO("delivery_delay", "计划送货已逾期",
                        deliveryNoticeMapper.selectCount(
                                deliveryScope(supplierId)
                                        .lt(DeliveryNotice::getPlanDeliveryDate, today)
                                        .lt(DeliveryNotice::getDeliveryStatus, 4)
                                        .isNull(DeliveryNotice::getActualDeliveryDate)),
                        "danger"),
                new DashboardRiskVO("delivery_approaching", "即将到期待发货",
                        deliveryNoticeMapper.selectCount(
                                deliveryScope(supplierId)
                                        .ge(DeliveryNotice::getPlanDeliveryDate, approachingStart)
                                        .le(DeliveryNotice::getPlanDeliveryDate, approachingEnd)
                                        .isNull(DeliveryNotice::getActualDeliveryDate)
                                        .lt(DeliveryNotice::getDeliveryStatus, 4)),
                        "warning"),
                new DashboardRiskVO("order_overdue", "订单交期已逾期",
                        purchaseOrderMapper.selectCount(
                                orderScope(supplierId)
                                        .lt(PurchaseOrder::getDeliveryDate, today)
                                        .in(PurchaseOrder::getOrderStatus, 0, 1, 2, 3)),
                        "danger")
        );
    }

    private LambdaQueryWrapper<PurchaseOrder> orderScope(Long supplierId) {
        return new LambdaQueryWrapper<PurchaseOrder>().eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId);
    }

    private LambdaQueryWrapper<DeliveryNotice> deliveryScope(Long supplierId) {
        return new LambdaQueryWrapper<DeliveryNotice>().eq(supplierId != null, DeliveryNotice::getSupplierId, supplierId);
    }

    private LambdaQueryWrapper<Rfq> rfqScope() {
        return new LambdaQueryWrapper<>();
    }

    private LambdaQueryWrapper<Quote> quoteScope(Long supplierId) {
        return new LambdaQueryWrapper<Quote>().eq(supplierId != null, Quote::getSupplierId, supplierId);
    }
}
