package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.service.BizStatusTrackService;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.common.util.ExcelExportUtil;
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryDetailItemDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
import com.supplier.logistics.dto.DeliveryNoticeExportRequest;
import com.supplier.logistics.entity.DeliveryDetail;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryDetailMapper;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.logistics.query.DeliveryNoticeQuery;
import com.supplier.logistics.service.DeliveryNoticeService;
import com.supplier.logistics.vo.DeliveryDetailVO;
import com.supplier.logistics.vo.DeliveryNoticeVO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;

import com.supplier.security.util.SecurityUtils;
import com.supplier.system.entity.SysExportTask;
import com.supplier.system.entity.SysFileAttachment;
import com.supplier.system.mapper.SysExportTaskMapper;
import com.supplier.system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryNoticeServiceImpl implements DeliveryNoticeService {
    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final DeliveryDetailMapper deliveryDetailMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final BizStatusTrackService bizStatusTrackService;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderDetailMapper purchaseOrderDetailMapper;
    private final PortalTodoService portalTodoService;
    private final SysExportTaskMapper sysExportTaskMapper;
    private final FileStorageService fileStorageService;
    private final com.supplier.system.mapper.SysFileAttachmentMapper sysFileAttachmentMapper;

    private static final int STATUS_CONFIRMED = 2;
    private static final int STATUS_PARTIAL_SHIPPED = 3;

    @Override
    public PageResult<DeliveryNoticeVO> page(DeliveryNoticeQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<DeliveryNotice> wrapper = new LambdaQueryWrapper<DeliveryNotice>()
                .eq(supplierId != null, DeliveryNotice::getSupplierId, supplierId)
                .eq(query.getOrderId() != null, DeliveryNotice::getOrderId, query.getOrderId())
                .eq(query.getDeliveryStatus() != null, DeliveryNotice::getDeliveryStatus, query.getDeliveryStatus())
                .ge(query.getStartDate() != null, DeliveryNotice::getPlanDeliveryDate, query.getStartDate())
                .le(query.getEndDate() != null, DeliveryNotice::getPlanDeliveryDate, query.getEndDate())
                .and(StringUtils.hasText(query.getKeyword()), w -> w.like(DeliveryNotice::getNoticeNo, query.getKeyword()).or().like(DeliveryNotice::getOrderNo, query.getKeyword()))
                .orderByDesc(DeliveryNotice::getCreateTime);
        Page<DeliveryNotice> page = deliveryNoticeMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<DeliveryNoticeVO> vos = page.convert(this::toVO).getRecords();
        fillQuantityAggregated(vos);
        return PageResult.of(vos, page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 批量加载明细并按 noticeId 聚合 actualQty，写入 vo.quantity。
     * 一次 IN 查询，避免 N+1。
     */
    private void fillQuantityAggregated(List<DeliveryNoticeVO> vos) {
        if (CollectionUtils.isEmpty(vos)) return;
        List<Long> noticeIds = vos.stream().map(DeliveryNoticeVO::getId).toList();
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().in(DeliveryDetail::getNoticeId, noticeIds));
        // 优先聚合 actualQty（实际发货量）；若明细 actualQty 全为 0/null，则回退到 planQty 聚合，
        // 确保列表 quantity 字段不会因实际发货量未填写而错误地显示为 0。
        Map<Long, BigDecimal> qtyMap = details.stream()
                .filter(d -> d.getNoticeId() != null)
                .collect(Collectors.groupingBy(DeliveryDetail::getNoticeId,
                        Collectors.mapping(DeliveryDetail::getActualQty,
                                Collectors.reducing(BigDecimal.ZERO,
                                        (a, b) -> a.add(b == null ? BigDecimal.ZERO : b)))));
        Map<Long, BigDecimal> planQtyMap = details.stream()
                .filter(d -> d.getNoticeId() != null)
                .collect(Collectors.groupingBy(DeliveryDetail::getNoticeId,
                        Collectors.mapping(DeliveryDetail::getPlanQty,
                                Collectors.reducing(BigDecimal.ZERO,
                                        (a, b) -> a.add(b == null ? BigDecimal.ZERO : b)))));
        for (DeliveryNoticeVO vo : vos) {
            BigDecimal actual = qtyMap.get(vo.getId());
            if (actual == null || actual.compareTo(BigDecimal.ZERO) <= 0) {
                BigDecimal plan = planQtyMap.get(vo.getId());
                vo.setQuantity(plan != null ? plan : BigDecimal.ZERO);
            } else {
                vo.setQuantity(actual);
            }
        }
    }

    @Override
    public DeliveryNoticeVO getDetail(Long id) {
        DeliveryNotice notice = getWithScope(id);
        DeliveryNoticeVO vo = toVO(notice);
        // 加载 ASN 明细
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, id));
        List<DeliveryDetailVO> detailVOs = new ArrayList<>();
        for (DeliveryDetail d : details) {
            detailVOs.add(toDetailVO(d));
        }
        vo.setDetails(detailVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeliveryNoticeCreateDTO dto) {
        if (deliveryNoticeMapper.selectCount(new LambdaQueryWrapper<DeliveryNotice>().eq(DeliveryNotice::getNoticeNo, dto.getNoticeNo())) > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "送货通知单号已存在");
        }

        // 1. 校验关联订单存在且状态允许发货
        PurchaseOrder order = purchaseOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "关联订单不存在");
        }
        if (!Integer.valueOf(STATUS_CONFIRMED).equals(order.getOrderStatus())
                && !Integer.valueOf(STATUS_PARTIAL_SHIPPED).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "订单状态不允许发货，仅已确认或部分发货的订单可创建ASN");
        }

        // 2. 校验供应商与订单供应商一致
        if (!order.getSupplierId().equals(dto.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商与订单不匹配");
        }

        DeliveryNotice entity = new DeliveryNotice();
        entity.setNoticeNo(dto.getNoticeNo());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderNo(dto.getOrderNo());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setPlanDeliveryDate(dto.getPlanDeliveryDate());
        entity.setDeliveryMethod(dto.getDeliveryMethod());
        entity.setDeliveryCompany(dto.getDeliveryCompany());
        entity.setDeliveryNo(dto.getDeliveryNo());
        entity.setDriverName(dto.getDriverName());
        entity.setDriverPhone(dto.getDriverPhone());
        entity.setVehicleNo(dto.getVehicleNo());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setReceiver(dto.getReceiver());
        entity.setReceiverPhone(dto.getReceiverPhone());
        entity.setBatchNo(dto.getBatchNo());
        entity.setProductionDate(dto.getProductionDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setRemark(dto.getRemark());
        entity.setDeliveryStatus(0);
        deliveryNoticeMapper.insert(entity);

        // 3. 创建 ASN 明细行（支持明细级创建 + 可发数量校验）
        if (!CollectionUtils.isEmpty(dto.getDetails())) {
            for (DeliveryDetailItemDTO item : dto.getDetails()) {
                PurchaseOrderDetail orderDetail = purchaseOrderDetailMapper.selectById(item.getOrderDetailId());
                if (orderDetail == null) {
                    throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "订单明细不存在: orderDetailId=" + item.getOrderDetailId());
                }
                if (!orderDetail.getOrderId().equals(dto.getOrderId())) {
                    throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "订单明细不属于当前订单: orderDetailId=" + item.getOrderDetailId());
                }
                // 可发数量校验: 发货数量 <= 订单数量 - 已发数量
                BigDecimal deliveredQty = orderDetail.getDeliveredQty() != null ? orderDetail.getDeliveredQty() : BigDecimal.ZERO;
                BigDecimal availableQty = orderDetail.getQuantity().subtract(deliveredQty);
                if (item.getActualQty().compareTo(BigDecimal.ZERO) <= 0) {
                    throw BusinessException.of(ResultCode.PARAM_ERROR.getCode(), "发货数量必须大于0: materialCode=" + orderDetail.getMaterialCode());
                }
                if (item.getActualQty().compareTo(availableQty) > 0) {
                    throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(),
                            "发货数量超出可发数量: material=" + orderDetail.getMaterialName()
                                    + ", 发货=" + item.getActualQty() + ", 可发=" + availableQty
                                    + "(订单=" + orderDetail.getQuantity() + "-已发=" + deliveredQty + ")");
                }

                DeliveryDetail detail = new DeliveryDetail();
                detail.setNoticeId(entity.getId());
                detail.setOrderDetailId(item.getOrderDetailId());
                detail.setMaterialCode(orderDetail.getMaterialCode());
                detail.setMaterialName(orderDetail.getMaterialName());
                detail.setMaterialSpec(orderDetail.getMaterialSpec());
                detail.setUnit(orderDetail.getUnit());
                detail.setPlanQty(orderDetail.getQuantity());
                detail.setActualQty(item.getActualQty());
                detail.setReceivedQty(BigDecimal.ZERO);
                detail.setQualifiedQty(BigDecimal.ZERO);
                detail.setBatchNo(item.getBatchNo() != null ? item.getBatchNo() : dto.getBatchNo());
                detail.setProductionDate(item.getProductionDate() != null ? item.getProductionDate() : dto.getProductionDate());
                detail.setExpiryDate(item.getExpiryDate() != null ? item.getExpiryDate() : dto.getExpiryDate());
                detail.setCaseNo(item.getCaseNo());
                detail.setQtyPerCase(item.getQtyPerCase());
                detail.setBarcode(item.getBarcode());
                // 计算箱数：如果有箱号则统计，或者根据每箱数量计算
                if (item.getQtyPerCase() != null && item.getQtyPerCase() > 0) {
                    detail.setBoxCount(item.getActualQty().divide(BigDecimal.valueOf(item.getQtyPerCase()), 0, java.math.RoundingMode.CEILING).intValue());
                } else if (item.getCaseNo() != null && !item.getCaseNo().isEmpty()) {
                    detail.setBoxCount(1);
                } else {
                    detail.setBoxCount(0);
                }
                detail.setRemark(item.getRemark());
                deliveryDetailMapper.insert(detail);

                // 更新订单明细已发数量
                orderDetail.setDeliveredQty(deliveredQty.add(item.getActualQty()));
                purchaseOrderDetailMapper.updateById(orderDetail);
            }
        }

        // 4. 更新订单状态为"部分发货"（如果当前为"已确认"状态）
        if (Integer.valueOf(STATUS_CONFIRMED).equals(order.getOrderStatus())) {
            order.setOrderStatus(STATUS_PARTIAL_SHIPPED);
            purchaseOrderMapper.updateById(order);
        }

        domainEventPublisher.publish("supplier.delivery", "delivery.asn.created",
                DomainEvent.builder()
                        .eventType("delivery.asn.created")
                        .data(Map.of("businessId", entity.getId(), "businessNo", entity.getNoticeNo(),
                                "supplierId", entity.getSupplierId()))
                        .build());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "ASN发货", businessIdExpr = "#id")
    public void ship(Long id, DeliveryActionDTO dto) {
        DeliveryNotice notice = getWithScope(id);
        if (!Integer.valueOf(0).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待发货通知单可以发货");
        }
        notice.setDeliveryStatus(1);
        notice.setSendTime(LocalDateTime.now());
        notice.setActualDeliveryDate(LocalDate.now());
        if (dto != null && StringUtils.hasText(dto.getRemark())) {
            notice.setRemark(dto.getRemark());
        }
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), 0, 1, "供应商发货");
        domainEventPublisher.publish("supplier.delivery", "delivery.asn.shipped",
                DomainEvent.builder()
                        .eventType("delivery.asn.shipped")
                        .data(Map.of("businessId", notice.getId(), "businessNo", notice.getNoticeNo(),
                                "supplierId", notice.getSupplierId()))
                        .build());

        // 为采购方创建待办：待收货通知单
        createBuyerDeliveryTodo(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "ASN到达确认", businessIdExpr = "#id")
    public void arrive(Long id, DeliveryActionDTO dto) {
        DeliveryNotice notice = getWithScope(id);
        if (!Integer.valueOf(1).equals(notice.getDeliveryStatus()) && !Integer.valueOf(2).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前通知单状态不允许到达确认");
        }
        int beforeStatus = notice.getDeliveryStatus();
        notice.setDeliveryStatus(3);
        notice.setArriveTime(LocalDateTime.now());
        if (dto != null && StringUtils.hasText(dto.getRemark())) {
            notice.setRemark(dto.getRemark());
        }
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 3, "ASN到达确认");
    }

    @Override
    public List<DeliveryDetailVO> getLines(Long id) {
        getWithScope(id);
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, id));
        return details.stream().map(this::toDetailVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "ASN确认入库", businessIdExpr = "#id")
    public void warehousing(Long id) {
        DeliveryNotice notice = getWithScope(id);
        if (!Integer.valueOf(3).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已送达通知单可确认入库");
        }
        int beforeStatus = notice.getDeliveryStatus();
        notice.setDeliveryStatus(4);
        fillFinancialFieldsOnWarehousing(notice);
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 4, "ASN确认入库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "扫码收货确认", businessIdExpr = "#id")
    public void scanReceive(Long id, DeliveryActionDTO dto) {
        DeliveryNotice notice = getWithScope(id);
        // 已送达(3)状态可扫码收货
        if (!Integer.valueOf(3).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已送达的通知单可进行扫码收货");
        }
        int beforeStatus = notice.getDeliveryStatus();
        // 更新为已收货(4)
        notice.setDeliveryStatus(4);
        fillFinancialFieldsOnWarehousing(notice);
        if (dto != null && StringUtils.hasText(dto.getRemark())) {
            notice.setRemark(dto.getRemark());
        }
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 4, "扫码收货确认");
        domainEventPublisher.publish("supplier.delivery", "delivery.asn.scan-received",
                DomainEvent.builder()
                        .eventType("delivery.asn.scan-received")
                        .data(Map.of("businessId", notice.getId(), "businessNo", notice.getNoticeNo(),
                                "supplierId", notice.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "确认收货", businessIdExpr = "#id")
    public void confirmReceive(Long id, DeliveryActionDTO dto) {
        DeliveryNotice notice = getWithScope(id);
        if (!Integer.valueOf(3).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已送达的通知单可确认收货");
        }
        int beforeStatus = notice.getDeliveryStatus();
        notice.setDeliveryStatus(4);
        fillFinancialFieldsOnWarehousing(notice);
        if (dto != null) {
            if (StringUtils.hasText(dto.getRemark())) {
                notice.setRemark(dto.getRemark());
            }
            if (StringUtils.hasText(dto.getWarehouse())) {
                notice.setWarehouse(dto.getWarehouse());
            }
        }
        deliveryNoticeMapper.updateById(notice);

        // 回写 delivery_detail.receivedQty 和 purchase_order_detail.receivedQty
        writeBackReceivedQtyOnConfirm(notice);

        // 检查订单是否所有明细已全部收货，自动完成订单
        tryAutoCompleteOrder(notice.getOrderId());

        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 4, "确认收货");
        // 确认收货后自动完成关联待办
        portalTodoService.autoFinishByBusiness("delivery_notice", notice.getId());
        domainEventPublisher.publish("supplier.delivery", "delivery.asn.confirmed-receive",
                DomainEvent.builder()
                        .eventType("delivery.asn.confirmed-receive")
                        .data(Map.of("businessId", notice.getId(), "businessNo", notice.getNoticeNo(),
                                "supplierId", notice.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "拒收", businessIdExpr = "#id")
    public void rejectReceive(Long id, DeliveryActionDTO dto) {
        DeliveryNotice notice = getWithScope(id);
        if (!Integer.valueOf(3).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已送达的通知单可拒收");
        }
        int beforeStatus = notice.getDeliveryStatus();
        notice.setDeliveryStatus(5);
        if (dto != null && StringUtils.hasText(dto.getRemark())) {
            notice.setRemark(dto.getRemark());
        }
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 5, "拒收" + (dto != null && StringUtils.hasText(dto.getRemark()) ? ": " + dto.getRemark() : ""));
        domainEventPublisher.publish("supplier.delivery", "delivery.asn.rejected-receive",
                DomainEvent.builder()
                        .eventType("delivery.asn.rejected-receive")
                        .data(Map.of("businessId", notice.getId(), "businessNo", notice.getNoticeNo(),
                                "supplierId", notice.getSupplierId()))
                        .build());
    }

    /**
     * 检查订单是否所有明细已全部收货，如果是则自动将订单状态更新为"已完成"
     */
    private void tryAutoCompleteOrder(Long orderId) {
        if (orderId == null) return;
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !Integer.valueOf(STATUS_PARTIAL_SHIPPED).equals(order.getOrderStatus())) {
            return;
        }
        List<PurchaseOrderDetail> details = purchaseOrderDetailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().eq(PurchaseOrderDetail::getOrderId, orderId));
        boolean allReceived = true;
        for (PurchaseOrderDetail detail : details) {
            BigDecimal received = detail.getReceivedQty() != null ? detail.getReceivedQty() : BigDecimal.ZERO;
            BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO;
            if (received.compareTo(qty) < 0) {
                allReceived = false;
                break;
            }
        }
        if (allReceived) {
            order.setOrderStatus(4); // STATUS_COMPLETED
            order.setCompleteTime(LocalDateTime.now());
            purchaseOrderMapper.updateById(order);
            log.info("订单{}所有明细已全部收货，自动更新为已完成", order.getOrderNo());
            domainEventPublisher.publish("supplier.order", "order.purchase.auto-completed",
                    DomainEvent.builder()
                            .eventType("order.purchase.auto-completed")
                            .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                    "supplierId", order.getSupplierId()))
                            .build());
        }
    }

    /**
     * 确认收货时回写 delivery_detail.receivedQty 和 purchase_order_detail.receivedQty
     */
    private void writeBackReceivedQtyOnConfirm(DeliveryNotice notice) {
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, notice.getId()));
        for (DeliveryDetail detail : details) {
            // 用 actualQty 作为本次收货数量回写
            BigDecimal receivedQty = detail.getActualQty() != null ? detail.getActualQty() : BigDecimal.ZERO;
            if (receivedQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 更新 delivery_detail.receivedQty
            BigDecimal currentReceived = detail.getReceivedQty() != null ? detail.getReceivedQty() : BigDecimal.ZERO;
            detail.setReceivedQty(currentReceived.add(receivedQty));
            deliveryDetailMapper.updateById(detail);

            // 回写 purchase_order_detail.receivedQty
            if (detail.getOrderDetailId() != null) {
                PurchaseOrderDetail orderDetail = purchaseOrderDetailMapper.selectById(detail.getOrderDetailId());
                if (orderDetail != null) {
                    BigDecimal orderReceived = orderDetail.getReceivedQty() != null ? orderDetail.getReceivedQty() : BigDecimal.ZERO;
                    orderDetail.setReceivedQty(orderReceived.add(receivedQty));
                    purchaseOrderDetailMapper.updateById(orderDetail);
                }
            }
        }
    }

    private DeliveryNotice getWithScope(Long id) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(id);
        if (notice == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !notice.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return notice;
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

    private DeliveryNoticeVO toVO(DeliveryNotice e) {
        DeliveryNoticeVO vo = new DeliveryNoticeVO();
        vo.setId(e.getId()); vo.setNoticeNo(e.getNoticeNo()); vo.setOrderId(e.getOrderId()); vo.setOrderNo(e.getOrderNo()); vo.setSupplierId(e.getSupplierId()); vo.setSupplierName(e.getSupplierName()); vo.setPlanDeliveryDate(e.getPlanDeliveryDate()); vo.setActualDeliveryDate(e.getActualDeliveryDate()); vo.setDeliveryStatus(e.getDeliveryStatus()); vo.setDeliveryMethod(e.getDeliveryMethod()); vo.setDeliveryCompany(e.getDeliveryCompany()); vo.setDeliveryNo(e.getDeliveryNo()); vo.setDeliveryAddress(e.getDeliveryAddress()); vo.setReceiver(e.getReceiver()); vo.setSendTime(e.getSendTime()); vo.setArriveTime(e.getArriveTime()); vo.setBatchNo(e.getBatchNo()); vo.setProductionDate(e.getProductionDate()); vo.setExpiryDate(e.getExpiryDate()); vo.setRemark(e.getRemark()); vo.setWarehouse(e.getWarehouse());
        return vo;
    }

    private DeliveryDetailVO toDetailVO(DeliveryDetail d) {
        DeliveryDetailVO vo = new DeliveryDetailVO();
        vo.setId(d.getId()); vo.setNoticeId(d.getNoticeId()); vo.setOrderDetailId(d.getOrderDetailId());
        vo.setMaterialCode(d.getMaterialCode()); vo.setMaterialName(d.getMaterialName());
        vo.setMaterialSpec(d.getMaterialSpec()); vo.setUnit(d.getUnit());
        vo.setPlanQty(d.getPlanQty()); vo.setActualQty(d.getActualQty());
        vo.setReceivedQty(d.getReceivedQty()); vo.setQualifiedQty(d.getQualifiedQty());
        vo.setBatchNo(d.getBatchNo()); vo.setProductionDate(d.getProductionDate());
        vo.setExpiryDate(d.getExpiryDate()); vo.setBoxCount(d.getBoxCount());
        vo.setCaseNo(d.getCaseNo()); vo.setQtyPerCase(d.getQtyPerCase());
        vo.setBarcode(d.getBarcode()); vo.setRemark(d.getRemark());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long export(DeliveryNoticeExportRequest request) {
        // 1. 创建导出任务记录
        SysExportTask task = new SysExportTask();
        task.setTaskNo("EXP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        task.setTaskType("ASN");
        task.setExportParams(com.alibaba.fastjson2.JSON.toJSONString(request));
        task.setProcessedCount(0);
        task.setTaskStatus(0);
        sysExportTaskMapper.insert(task);

        try {
            // 2. 标记处理中
            task.setTaskStatus(1);
            task.setStartTime(LocalDateTime.now());
            sysExportTaskMapper.updateById(task);

            // 3. 查询数据
            List<DeliveryNotice> notices = queryForExport(request);
            List<com.supplier.logistics.dto.DeliveryNoticeExportDTO> exportData = new ArrayList<>();
            for (DeliveryNotice notice : notices) {
                List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                        new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, notice.getId()));
                if (details.isEmpty()) {
                    exportData.add(toExportDTO(notice, null));
                } else {
                    for (DeliveryDetail detail : details) {
                        exportData.add(toExportDTO(notice, detail));
                    }
                }
            }

            // 4. 生成 Excel
            byte[] excelBytes = ExcelExportUtil.export(
                    com.supplier.logistics.dto.DeliveryNoticeExportDTO.class, exportData, "ASN数据");

            // 5. 存储文件
            String fileName = "ASN导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            SysFileAttachment attachment = fileStorageService.storeBytes(
                    excelBytes, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "export", task.getId(), task.getTaskNo());
            sysFileAttachmentMapper.insert(attachment);

            // 6. 标记成功
            task.setTaskStatus(2);
            task.setFileId(attachment.getId());
            task.setTotalCount(exportData.size());
            task.setProcessedCount(exportData.size());
            task.setFinishTime(LocalDateTime.now());
            sysExportTaskMapper.updateById(task);

        } catch (Exception e) {
            log.error("ASN导出失败", e);
            task.setTaskStatus(3);
            task.setErrorMessage(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            sysExportTaskMapper.updateById(task);
        }

        return task.getId();
    }

    private List<DeliveryNotice> queryForExport(DeliveryNoticeExportRequest request) {
        Long supplierId = resolveSupplierId(request.getSupplierId());
        LambdaQueryWrapper<DeliveryNotice> wrapper = new LambdaQueryWrapper<DeliveryNotice>()
                .eq(supplierId != null, DeliveryNotice::getSupplierId, supplierId)
                .eq(request.getDeliveryStatus() != null, DeliveryNotice::getDeliveryStatus, request.getDeliveryStatus())
                .ge(StringUtils.hasText(request.getStartDate()), DeliveryNotice::getPlanDeliveryDate,
                        StringUtils.hasText(request.getStartDate()) ? LocalDate.parse(request.getStartDate()) : null)
                .le(StringUtils.hasText(request.getEndDate()), DeliveryNotice::getPlanDeliveryDate,
                        StringUtils.hasText(request.getEndDate()) ? LocalDate.parse(request.getEndDate()) : null)
                .and(StringUtils.hasText(request.getKeyword()),
                        w -> w.like(DeliveryNotice::getNoticeNo, request.getKeyword())
                                .or().like(DeliveryNotice::getOrderNo, request.getKeyword()));

        if ("selected".equals(request.getScope()) && !CollectionUtils.isEmpty(request.getSelectedIds())) {
            wrapper.in(DeliveryNotice::getId, request.getSelectedIds());
        }

        wrapper.orderByDesc(DeliveryNotice::getCreateTime);
        return deliveryNoticeMapper.selectList(wrapper);
    }

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "待发货", 1, "已发货", 2, "运输中", 3, "已送达", 4, "已收货", 5, "已拒收", 6, "质检中");

    private com.supplier.logistics.dto.DeliveryNoticeExportDTO toExportDTO(DeliveryNotice notice, DeliveryDetail detail) {
        com.supplier.logistics.dto.DeliveryNoticeExportDTO dto = new com.supplier.logistics.dto.DeliveryNoticeExportDTO();
        dto.setNoticeNo(notice.getNoticeNo());
        dto.setOrderNo(notice.getOrderNo());
        dto.setSupplierName(notice.getSupplierName());
        dto.setPlanDeliveryDate(notice.getPlanDeliveryDate() != null ? notice.getPlanDeliveryDate().toString() : "");
        dto.setActualDeliveryDate(notice.getActualDeliveryDate() != null ? notice.getActualDeliveryDate().toString() : "");
        dto.setDeliveryStatusText(STATUS_MAP.getOrDefault(notice.getDeliveryStatus(), "未知"));
        dto.setDeliveryMethod(notice.getDeliveryMethod());
        dto.setDeliveryCompany(notice.getDeliveryCompany());
        dto.setDeliveryNo(notice.getDeliveryNo());
        dto.setDeliveryAddress(notice.getDeliveryAddress());
        dto.setReceiver(notice.getReceiver());
        dto.setRemark(notice.getRemark());
        if (detail != null) {
            dto.setMaterialCode(detail.getMaterialCode());
            dto.setMaterialName(detail.getMaterialName());
            dto.setMaterialSpec(detail.getMaterialSpec());
            dto.setUnit(detail.getUnit());
            dto.setPlanQty(detail.getPlanQty());
            dto.setActualQty(detail.getActualQty());
            dto.setReceivedQty(detail.getReceivedQty());
            dto.setQualifiedQty(detail.getQualifiedQty());
            dto.setBatchNo(detail.getBatchNo());
            dto.setCaseNo(detail.getCaseNo());
        }
        return dto;
    }

    private void createBuyerDeliveryTodo(DeliveryNotice notice) {
        try {
            PortalTodoCreateDTO todoDto = new PortalTodoCreateDTO();
            todoDto.setTodoType("delivery_receive");
            todoDto.setBusinessType("delivery_notice");
            todoDto.setBusinessId(notice.getId());
            todoDto.setBusinessNo(notice.getNoticeNo());
            todoDto.setTitle("待收货通知单 " + notice.getNoticeNo());
            todoDto.setDueTime(notice.getPlanDeliveryDate() != null ? notice.getPlanDeliveryDate().atStartOfDay() : LocalDateTime.now().plusDays(3));
            portalTodoService.create(todoDto);
        } catch (Exception e) {
            log.warn("创建收货待办失败: noticeId={}", notice.getId(), e);
        }
    }

    /**
     * 入库时填充财务字段（从关联采购订单取金额，初始化对账/付款状态）。
     * 仅在字段尚未设置时填充，避免覆盖已有数据。
     */
    private void fillFinancialFieldsOnWarehousing(DeliveryNotice notice) {
        if (notice.getTotalAmount() != null) {
            return; // 已有财务数据，不覆盖
        }
        PurchaseOrder order = purchaseOrderMapper.selectById(notice.getOrderId());
        if (order != null) {
            notice.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            notice.setCurrency(order.getCurrency() != null ? order.getCurrency() : "CNY");
            notice.setTaxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO);
            notice.setNetAmount(notice.getTotalAmount().subtract(notice.getTaxAmount()));
        } else {
            notice.setTotalAmount(BigDecimal.ZERO);
            notice.setCurrency("CNY");
            notice.setTaxAmount(BigDecimal.ZERO);
            notice.setNetAmount(BigDecimal.ZERO);
        }
        if (notice.getReconciliationStatus() == null) {
            notice.setReconciliationStatus(0); // 待对账
        }
        if (notice.getPaymentStatus() == null) {
            notice.setPaymentStatus(0); // 未付款
        }
    }
}
