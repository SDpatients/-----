package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.ReconciliationConfirmDTO;
import com.supplier.logistics.dto.ReconciliationStatusUpdateDTO;
import com.supplier.logistics.entity.DeliveryDetail;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryDetailMapper;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.logistics.mapper.WriteOffMapper;
import com.supplier.logistics.entity.WriteOff;
import com.supplier.logistics.query.FinancialReconciliationQuery;
import com.supplier.logistics.service.FinancialReconciliationService;
import com.supplier.logistics.vo.FinancialReconciliationChartDataVO;
import com.supplier.logistics.vo.FinancialReconciliationOverviewVO;
import com.supplier.logistics.vo.FinancialReconciliationRecordVO;
import com.supplier.logistics.vo.ReconciliationDetailVO;
import com.supplier.logistics.vo.ReconciliationLineVO;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReconciliationServiceImpl implements FinancialReconciliationService {

    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final DeliveryDetailMapper deliveryDetailMapper;
    private final WriteOffMapper writeOffMapper;
    private final PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    private static final Map<Integer, String> RECON_STATUS_MAP = Map.of(
            0, "待对账", 1, "对账中", 2, "已对账", 3, "有差异"
    );
    private static final Map<Integer, String> PAYMENT_STATUS_MAP = Map.of(
            0, "未付款", 1, "部分付款", 2, "已付款"
    );

    @Override
    public FinancialReconciliationOverviewVO getOverview(FinancialReconciliationQuery query) {
        List<DeliveryNotice> notices = listNotices(query);

        FinancialReconciliationOverviewVO vo = new FinancialReconciliationOverviewVO();
        vo.setTotalAmount(sumAmount(notices, null));
        vo.setReconciledAmount(sumAmount(notices, 2));
        vo.setPendingAmount(sumAmount(notices, 0).add(sumAmount(notices, 1)));
        vo.setDiffAmount(sumDiffAmount(notices));
        vo.setTotalCount(notices.size());
        vo.setReconciledCount(countByStatus(notices, 2));
        vo.setPendingCount(countByStatus(notices, 0) + countByStatus(notices, 1));
        vo.setDiffCount(countByStatus(notices, 3));
        return vo;
    }

    @Override
    public FinancialReconciliationChartDataVO getChartData(FinancialReconciliationQuery query) {
        List<DeliveryNotice> notices = listNotices(query);

        FinancialReconciliationChartDataVO vo = new FinancialReconciliationChartDataVO();
        vo.setTrends(buildTrends(notices));
        vo.setSupplierDistribution(buildSupplierDistribution(notices));
        vo.setPaymentStatus(buildPaymentStatusDistribution(notices));
        vo.setReconciliationStatus(buildReconStatusDistribution(notices));
        return vo;
    }

    @Override
    public PageResult<FinancialReconciliationRecordVO> page(FinancialReconciliationQuery query) {
        LambdaQueryWrapper<DeliveryNotice> wrapper = buildWrapper(query);
        wrapper.orderByDesc(DeliveryNotice::getCreateTime);

        Page<DeliveryNotice> page = deliveryNoticeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<DeliveryNotice> notices = page.getRecords();
        // 批量查询明细和采购订单明细，避免 N+1
        Map<Long, List<DeliveryDetail>> detailsByNotice = Map.of();
        Map<Long, PurchaseOrderDetail> podMap = Map.of();
        if (!notices.isEmpty()) {
            List<Long> noticeIds = notices.stream().map(DeliveryNotice::getId).toList();
            List<DeliveryDetail> allDetails = deliveryDetailMapper.selectList(
                    new LambdaQueryWrapper<DeliveryDetail>().in(DeliveryDetail::getNoticeId, noticeIds));
            detailsByNotice = allDetails.stream()
                    .filter(d -> d.getNoticeId() != null)
                    .collect(Collectors.groupingBy(DeliveryDetail::getNoticeId));

            List<Long> orderDetailIds = allDetails.stream()
                    .map(DeliveryDetail::getOrderDetailId)
                    .filter(id -> id != null)
                    .distinct().toList();
            podMap = orderDetailIds.isEmpty() ? Map.of()
                    : purchaseOrderDetailMapper.selectBatchIds(orderDetailIds).stream()
                            .collect(Collectors.toMap(PurchaseOrderDetail::getId, p -> p));
        }

        Map<Long, List<DeliveryDetail>> finalDetailsByNotice = detailsByNotice;
        Map<Long, PurchaseOrderDetail> finalPodMap = podMap;
        List<FinancialReconciliationRecordVO> voList = notices.stream()
                .map(n -> toRecordVO(n, finalDetailsByNotice, finalPodMap))
                .toList();

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReconciliationStatus(Long id, ReconciliationStatusUpdateDTO dto) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(id);
        if (notice == null) {
            throw BusinessException.of(404, "发货通知单不存在");
        }
        notice.setReconciliationStatus(dto.getReconciliationStatus());
        // 如果标记为有差异，记录差异金额
        if (dto.getReconciliationStatus() == 3 && dto.getDiffAmount() != null) {
            notice.setNetAmount(notice.getTotalAmount() != null
                    ? notice.getTotalAmount().subtract(dto.getDiffAmount())
                    : dto.getDiffAmount().negate());
        }
        deliveryNoticeMapper.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long id, Integer paymentStatus) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(id);
        if (notice == null) {
            throw BusinessException.of(404, "发货通知单不存在");
        }
        notice.setPaymentStatus(paymentStatus);
        deliveryNoticeMapper.updateById(notice);
    }

    @Override
    public ReconciliationDetailVO getDetail(Long id) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(id);
        if (notice == null) {
            throw BusinessException.of(404, "发货通知单不存在");
        }

        ReconciliationDetailVO vo = new ReconciliationDetailVO();
        vo.setId(notice.getId());
        vo.setAsnNo(notice.getNoticeNo());
        vo.setOrderNo(notice.getOrderNo());
        vo.setSupplierName(notice.getSupplierName());
        vo.setTotalAmount(notice.getTotalAmount());
        vo.setCurrency(notice.getCurrency());
        vo.setReconciliationStatus(notice.getReconciliationStatus());
        vo.setPaymentStatus(notice.getPaymentStatus());
        vo.setPeriod(notice.getActualDeliveryDate() != null
                ? notice.getActualDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null);

        // 查询发货明细行
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, id));

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        BigDecimal totalReconcileAmount = BigDecimal.ZERO;
        BigDecimal totalDiffAmount = BigDecimal.ZERO;
        List<ReconciliationLineVO> lines = new ArrayList<>();

        for (DeliveryDetail dd : details) {
            ReconciliationLineVO line = new ReconciliationLineVO();
            line.setDetailId(dd.getId());
            line.setOrderDetailId(dd.getOrderDetailId());
            line.setMaterialCode(dd.getMaterialCode());
            line.setMaterialName(dd.getMaterialName());
            line.setMaterialSpec(dd.getMaterialSpec());
            line.setUnit(dd.getUnit());
            line.setDeliveryQty(dd.getActualQty());
            line.setReceivedQty(dd.getReceivedQty());

            // 从采购订单明细取单价和金额
            if (dd.getOrderDetailId() != null) {
                PurchaseOrderDetail pod = purchaseOrderDetailMapper.selectById(dd.getOrderDetailId());
                if (pod != null) {
                    line.setOrderQty(pod.getQuantity());
                    line.setUnitPrice(pod.getUnitPrice());
                    BigDecimal orderAmount = pod.getQuantity() != null && pod.getUnitPrice() != null
                            ? pod.getQuantity().multiply(pod.getUnitPrice()) : BigDecimal.ZERO;
                    line.setOrderAmount(orderAmount);
                }
            }

            // 默认对账数量=实收数量
            BigDecimal reconcileQty = dd.getReceivedQty() != null ? dd.getReceivedQty() : BigDecimal.ZERO;
            line.setReconcileQty(reconcileQty);

            // 计算对账金额
            BigDecimal reconcileAmount = line.getUnitPrice() != null
                    ? reconcileQty.multiply(line.getUnitPrice()) : BigDecimal.ZERO;
            line.setReconcileAmount(reconcileAmount);

            // 计算差异（基于发货数量/金额，而非订单数量/金额）
            BigDecimal deliveryQty = dd.getActualQty() != null ? dd.getActualQty() : BigDecimal.ZERO;
            BigDecimal deliveryAmount = line.getUnitPrice() != null
                    ? deliveryQty.multiply(line.getUnitPrice()) : BigDecimal.ZERO;
            BigDecimal diffAmount = reconcileAmount.subtract(deliveryAmount);
            BigDecimal diffQty = reconcileQty.subtract(deliveryQty);
            line.setDiffQty(diffQty);
            line.setDiffAmount(diffAmount);

            totalOrderAmount = totalOrderAmount.add(deliveryAmount);
            totalReconcileAmount = totalReconcileAmount.add(reconcileAmount);
            totalDiffAmount = totalDiffAmount.add(diffAmount);

            lines.add(line);
        }

        vo.setLines(lines);
        vo.setTotalOrderAmount(totalOrderAmount);
        vo.setTotalReconcileAmount(totalReconcileAmount);
        vo.setTotalDiffAmount(totalDiffAmount);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReconciliation(Long id, ReconciliationConfirmDTO dto) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(id);
        if (notice == null) {
            throw BusinessException.of(404, "发货通知单不存在");
        }

        // 计算差异金额
        BigDecimal totalDiffAmount = BigDecimal.ZERO;
        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            for (ReconciliationConfirmDTO.LineReconcileItem item : dto.getLines()) {
                DeliveryDetail dd = deliveryDetailMapper.selectById(item.getDetailId());
                if (dd == null || !dd.getNoticeId().equals(id)) {
                    continue;
                }
                // 更新对账数量到 receivedQty（对账确认后的最终数量）
                dd.setReceivedQty(item.getReconcileQty());
                deliveryDetailMapper.updateById(dd);

                // 计算该行差异金额（基于发货数量，而非订单数量）
                if (dd.getOrderDetailId() != null) {
                    PurchaseOrderDetail pod = purchaseOrderDetailMapper.selectById(dd.getOrderDetailId());
                    if (pod != null && pod.getUnitPrice() != null) {
                        BigDecimal reconcileAmount = item.getReconcileQty().multiply(pod.getUnitPrice());
                        BigDecimal deliveryQty = dd.getActualQty() != null ? dd.getActualQty() : BigDecimal.ZERO;
                        BigDecimal deliveryAmount = deliveryQty.multiply(pod.getUnitPrice());
                        totalDiffAmount = totalDiffAmount.add(reconcileAmount.subtract(deliveryAmount));
                    }
                }
            }
        }

        notice.setReconciliationStatus(dto.getReconciliationStatus());

        // 自动计算差异金额并更新
        if (totalDiffAmount.compareTo(BigDecimal.ZERO) != 0) {
            notice.setNetAmount(notice.getTotalAmount() != null
                    ? notice.getTotalAmount().subtract(totalDiffAmount.abs())
                    : totalDiffAmount.abs().negate());
            // 如果有差异但状态未标记为"有差异"，自动修正
            if (dto.getReconciliationStatus() != 3) {
                notice.setReconciliationStatus(3);
            }
        }

        deliveryNoticeMapper.updateById(notice);
    }

    // ---------- 私有方法 ----------

    private BigDecimal calculateDiffAmount(DeliveryNotice notice) {
        // 从冲销记录汇总差异金额
        List<WriteOff> writeOffs = writeOffMapper.selectList(
                new LambdaQueryWrapper<WriteOff>().eq(WriteOff::getNoticeId, notice.getId()));
        if (writeOffs.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return writeOffs.stream()
                .map(w -> w.getAmount() != null ? w.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LambdaQueryWrapper<DeliveryNotice> buildWrapper(FinancialReconciliationQuery query) {
        LambdaQueryWrapper<DeliveryNotice> wrapper = new LambdaQueryWrapper<DeliveryNotice>()
                .ge(DeliveryNotice::getDeliveryStatus, 4) // 只展示已入库的物流单
                .isNotNull(DeliveryNotice::getTotalAmount)
                .ge(query.getStartDate() != null, DeliveryNotice::getActualDeliveryDate, query.getStartDate())
                .le(query.getEndDate() != null, DeliveryNotice::getActualDeliveryDate, query.getEndDate())
                .eq(query.getReconciliationStatus() != null, DeliveryNotice::getReconciliationStatus, query.getReconciliationStatus())
                .eq(query.getPaymentStatus() != null, DeliveryNotice::getPaymentStatus, query.getPaymentStatus());

        if (StringUtils.hasText(query.getSupplierName())) {
            wrapper.like(DeliveryNotice::getSupplierName, query.getSupplierName());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DeliveryNotice::getNoticeNo, query.getKeyword())
                    .or().like(DeliveryNotice::getOrderNo, query.getKeyword()));
        }
        return wrapper;
    }

    private List<DeliveryNotice> listNotices(FinancialReconciliationQuery query) {
        LambdaQueryWrapper<DeliveryNotice> wrapper = buildWrapper(query);
        return deliveryNoticeMapper.selectList(wrapper);
    }

    private BigDecimal sumAmount(List<DeliveryNotice> notices, Integer status) {
        return notices.stream()
                .filter(n -> status == null || status.equals(n.getReconciliationStatus()))
                .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算所有订单的实际差异金额之和（对账金额 - 发货金额）。
     * 对于已完成对账的订单，使用 totalAmount - netAmount；
     * 否则从明细行计算差异（基于发货数量，而非订单数量）。
     */
    private BigDecimal sumDiffAmount(List<DeliveryNotice> notices) {
        if (notices.isEmpty()) return BigDecimal.ZERO;

        // 批量查询所有相关明细，避免 N+1
        List<Long> noticeIds = notices.stream().map(DeliveryNotice::getId).toList();
        List<DeliveryDetail> allDetails = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().in(DeliveryDetail::getNoticeId, noticeIds));
        Map<Long, List<DeliveryDetail>> detailsByNotice = allDetails.stream()
                .filter(d -> d.getNoticeId() != null)
                .collect(Collectors.groupingBy(DeliveryDetail::getNoticeId));

        // 批量查询关联的采购订单明细（取单价）
        List<Long> orderDetailIds = allDetails.stream()
                .map(DeliveryDetail::getOrderDetailId)
                .filter(id -> id != null)
                .distinct().toList();
        Map<Long, PurchaseOrderDetail> podMap = orderDetailIds.isEmpty() ? Map.of()
                : purchaseOrderDetailMapper.selectBatchIds(orderDetailIds).stream()
                        .collect(Collectors.toMap(PurchaseOrderDetail::getId, p -> p));

        BigDecimal totalDiff = BigDecimal.ZERO;
        for (DeliveryNotice notice : notices) {
            // 优先使用 netAmount（对账确认后已写入）
            if (notice.getNetAmount() != null && notice.getTotalAmount() != null) {
                totalDiff = totalDiff.add(notice.getTotalAmount().subtract(notice.getNetAmount()));
                continue;
            }

            // 否则从明细行计算差异（基于发货数量）
            List<DeliveryDetail> details = detailsByNotice.getOrDefault(notice.getId(), List.of());
            for (DeliveryDetail dd : details) {
                if (dd.getOrderDetailId() == null) continue;
                PurchaseOrderDetail pod = podMap.get(dd.getOrderDetailId());
                if (pod == null || pod.getUnitPrice() == null) continue;

                BigDecimal deliveryQty = dd.getActualQty() != null ? dd.getActualQty() : BigDecimal.ZERO;
                BigDecimal deliveryAmount = deliveryQty.multiply(pod.getUnitPrice());
                BigDecimal reconcileAmount = dd.getReceivedQty() != null
                        ? dd.getReceivedQty().multiply(pod.getUnitPrice()) : BigDecimal.ZERO;
                totalDiff = totalDiff.add(reconcileAmount.subtract(deliveryAmount));
            }
        }
        return totalDiff;
    }

    private int countByStatus(List<DeliveryNotice> notices, Integer status) {
        return (int) notices.stream().filter(n -> status.equals(n.getReconciliationStatus())).count();
    }

    private List<FinancialReconciliationChartDataVO.TrendItem> buildTrends(List<DeliveryNotice> notices) {
        Map<String, List<DeliveryNotice>> grouped = notices.stream()
                .filter(n -> n.getActualDeliveryDate() != null)
                .collect(Collectors.groupingBy(n ->
                        YearMonth.from(n.getActualDeliveryDate()).format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        List<FinancialReconciliationChartDataVO.TrendItem> trends = new ArrayList<>();
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    FinancialReconciliationChartDataVO.TrendItem item = new FinancialReconciliationChartDataVO.TrendItem();
                    item.setPeriod(entry.getKey());
                    List<DeliveryNotice> group = entry.getValue();
                    item.setTotalAmount(group.stream()
                            .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    item.setReconciledAmount(group.stream()
                            .filter(n -> Integer.valueOf(2).equals(n.getReconciliationStatus()))
                            .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    item.setDiffAmount(sumDiffAmount(group));
                    item.setCount(group.size());
                    trends.add(item);
                });
        return trends;
    }

    private List<FinancialReconciliationChartDataVO.SupplierDistributionItem> buildSupplierDistribution(List<DeliveryNotice> notices) {
        Map<String, List<DeliveryNotice>> grouped = notices.stream()
                .collect(Collectors.groupingBy(DeliveryNotice::getSupplierName, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    FinancialReconciliationChartDataVO.SupplierDistributionItem item = new FinancialReconciliationChartDataVO.SupplierDistributionItem();
                    item.setSupplierName(entry.getKey());
                    item.setAmount(entry.getValue().stream()
                            .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    item.setCount(entry.getValue().size());
                    return item;
                })
                .sorted(Comparator.comparing(FinancialReconciliationChartDataVO.SupplierDistributionItem::getAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<FinancialReconciliationChartDataVO.StatusDistributionItem> buildPaymentStatusDistribution(List<DeliveryNotice> notices) {
        Map<Integer, List<DeliveryNotice>> grouped = notices.stream()
                .filter(n -> n.getPaymentStatus() != null)
                .collect(Collectors.groupingBy(DeliveryNotice::getPaymentStatus));

        List<FinancialReconciliationChartDataVO.StatusDistributionItem> result = new ArrayList<>();
        for (Map.Entry<Integer, List<DeliveryNotice>> entry : grouped.entrySet()) {
            FinancialReconciliationChartDataVO.StatusDistributionItem item = new FinancialReconciliationChartDataVO.StatusDistributionItem();
            item.setStatus(String.valueOf(entry.getKey()));
            item.setLabel(PAYMENT_STATUS_MAP.getOrDefault(entry.getKey(), "未知"));
            item.setCount(entry.getValue().size());
            item.setAmount(entry.getValue().stream()
                    .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(item);
        }
        return result;
    }

    private List<FinancialReconciliationChartDataVO.StatusDistributionItem> buildReconStatusDistribution(List<DeliveryNotice> notices) {
        Map<Integer, List<DeliveryNotice>> grouped = notices.stream()
                .filter(n -> n.getReconciliationStatus() != null)
                .collect(Collectors.groupingBy(DeliveryNotice::getReconciliationStatus));

        List<FinancialReconciliationChartDataVO.StatusDistributionItem> result = new ArrayList<>();
        for (Map.Entry<Integer, List<DeliveryNotice>> entry : grouped.entrySet()) {
            FinancialReconciliationChartDataVO.StatusDistributionItem item = new FinancialReconciliationChartDataVO.StatusDistributionItem();
            item.setStatus(String.valueOf(entry.getKey()));
            item.setLabel(RECON_STATUS_MAP.getOrDefault(entry.getKey(), "未知"));
            item.setCount(entry.getValue().size());
            item.setAmount(entry.getValue().stream()
                    .map(n -> n.getTotalAmount() != null ? n.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(item);
        }
        return result;
    }

    private FinancialReconciliationRecordVO toRecordVO(DeliveryNotice notice,
            Map<Long, List<DeliveryDetail>> detailsByNotice,
            Map<Long, PurchaseOrderDetail> podMap) {
        FinancialReconciliationRecordVO vo = new FinancialReconciliationRecordVO();
        vo.setId(notice.getId());
        vo.setAsnNo(notice.getNoticeNo());
        vo.setOrderNo(notice.getOrderNo());
        vo.setSupplierName(notice.getSupplierName());
        vo.setTotalAmount(notice.getTotalAmount());
        vo.setCurrency(notice.getCurrency());
        vo.setReconciliationStatus(notice.getReconciliationStatus());
        vo.setReconciliationStatusLabel(RECON_STATUS_MAP.getOrDefault(notice.getReconciliationStatus(), "未知"));
        vo.setPaymentStatus(notice.getPaymentStatus());
        vo.setPaymentStatusLabel(PAYMENT_STATUS_MAP.getOrDefault(notice.getPaymentStatus(), "未知"));

        // 计算差异金额：优先使用 netAmount，否则从明细行计算
        BigDecimal diffAmount = BigDecimal.ZERO;
        if (notice.getNetAmount() != null && notice.getTotalAmount() != null) {
            diffAmount = notice.getTotalAmount().subtract(notice.getNetAmount());
        } else {
            List<DeliveryDetail> details = detailsByNotice.getOrDefault(notice.getId(), List.of());
            for (DeliveryDetail dd : details) {
                if (dd.getOrderDetailId() == null) continue;
                PurchaseOrderDetail pod = podMap.get(dd.getOrderDetailId());
                if (pod == null || pod.getUnitPrice() == null) continue;
                BigDecimal deliveryQty = dd.getActualQty() != null ? dd.getActualQty() : BigDecimal.ZERO;
                BigDecimal deliveryAmount = deliveryQty.multiply(pod.getUnitPrice());
                BigDecimal reconcileAmount = dd.getReceivedQty() != null
                        ? dd.getReceivedQty().multiply(pod.getUnitPrice()) : BigDecimal.ZERO;
                diffAmount = diffAmount.add(reconcileAmount.subtract(deliveryAmount));
            }
        }
        vo.setDiffAmount(diffAmount);

        // 对账时间：显示年月日
        vo.setPeriod(notice.getActualDeliveryDate() != null
                ? notice.getActualDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null);
        vo.setCreateDate(notice.getActualDeliveryDate());

        // 计算总发货数量
        List<DeliveryDetail> details = detailsByNotice.getOrDefault(notice.getId(), List.of());
        int totalQty = details.stream()
                .mapToInt(d -> d.getPlanQty() != null ? d.getPlanQty().intValue() : 0)
                .sum();
        vo.setQuantity(totalQty);

        return vo;
    }
}
