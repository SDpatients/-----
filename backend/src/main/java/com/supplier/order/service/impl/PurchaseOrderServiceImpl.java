package com.supplier.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.order.converter.PurchaseOrderConverter;
import com.supplier.order.dto.BuyerConfirmDTO;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.OrderCloseDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.entity.OrderTrack;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.OrderTrackMapper;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.query.PurchaseOrderQuery;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.order.vo.PurchaseOrderVO;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING_CONFIRM = 1;
    private static final int STATUS_CONFIRMED = 2;
    private static final int STATUS_PARTIAL_SHIPPED = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELED = 5;
    private static final int STATUS_REJECTED = 6;

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final OrderTrackMapper orderTrackMapper;
    private final PurchaseOrderDetailMapper detailMapper;
    private final PortalTodoService portalTodoService;
    private final MessageNoticeService messageNoticeService;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResult<PurchaseOrderVO> page(PurchaseOrderQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .eq(query.getOrderStatus() != null, PurchaseOrder::getOrderStatus, query.getOrderStatus())
                .notIn(!CollectionUtils.isEmpty(query.getExcludeStatuses()), PurchaseOrder::getOrderStatus, query.getExcludeStatuses())
                .ge(query.getStartDate() != null, PurchaseOrder::getOrderDate, query.getStartDate())
                .le(query.getEndDate() != null, PurchaseOrder::getOrderDate, query.getEndDate())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(PurchaseOrder::getOrderNo, query.getKeyword())
                        .or()
                        .like(PurchaseOrder::getBuyerName, query.getKeyword()))
                .orderByDesc(PurchaseOrder::getOrderDate)
                .orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> page = purchaseOrderMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 为每个订单填充聚合数量
        List<PurchaseOrderVO> voList = new ArrayList<>();
        for (PurchaseOrder order : page.getRecords()) {
            List<PurchaseOrderDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrderDetail>()
                            .eq(PurchaseOrderDetail::getOrderId, order.getId())
                            .orderByAsc(PurchaseOrderDetail::getLineNo));
            voList.add(PurchaseOrderConverter.toVOWithDetails(order, details));
        }

        Page<PurchaseOrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return PageResult.of(voPage);
    }

    @Override
    public PurchaseOrderVO getDetail(Long id) {
        PurchaseOrder order = getOrderWithDataScope(id);
        List<PurchaseOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>()
                        .eq(PurchaseOrderDetail::getOrderId, id)
                        .orderByAsc(PurchaseOrderDetail::getLineNo));
        return PurchaseOrderConverter.toVOWithDetails(order, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseOrderCreateDTO dto) {
        Long count = purchaseOrderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getOrderNo, dto.getOrderNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "订单号已存在");
        }
        PurchaseOrder order = PurchaseOrderConverter.toEntity(dto);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            order.setBuyerId(loginUser.getUserId());
            order.setBuyerName(loginUser.getRealName());
        }
        purchaseOrderMapper.insert(order);
        writeTrack(order, STATUS_DRAFT, "创建订单");
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "订单下发", businessIdExpr = "#id")
    public void publish(Long id, OrderActionDTO dto) {
        dto = normalizeAction(dto);
        changeStatus(id, STATUS_DRAFT, STATUS_PENDING_CONFIRM, dto, "订单下发");
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order != null) {
            if (order.getSupplierId() != null) {
                createSupplierTodo(order);
                createSupplierMessage(order);
            }
            domainEventPublisher.publish("supplier.order", "order.purchase.published",
                    DomainEvent.builder()
                            .eventType("order.purchase.published")
                            .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                    "supplierId", order.getSupplierId()))
                            .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "供应商接单", businessIdExpr = "#id")
    public void confirm(Long id, OrderActionDTO dto) {
        dto = normalizeAction(dto);
        PurchaseOrder order = getOrderWithDataScope(id);
        if (!Integer.valueOf(STATUS_PENDING_CONFIRM).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待确认订单可以接单");
        }
        order.setOrderStatus(STATUS_CONFIRMED);
        order.setConfirmBy(SecurityUtils.getUserId());
        order.setConfirmTime(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
        writeTrack(order, STATUS_CONFIRMED, StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "供应商确认订单");
        // 订单确认后自动完成关联待办
        portalTodoService.autoFinishByBusiness("purchase_order", order.getId());
        domainEventPublisher.publish("supplier.order", "order.purchase.confirmed",
                DomainEvent.builder()
                        .eventType("order.purchase.confirmed")
                        .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                "supplierId", order.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "供应商拒单", businessIdExpr = "#id")
    public void reject(Long id, OrderActionDTO dto) {
        dto = normalizeAction(dto);
        changeStatus(id, STATUS_PENDING_CONFIRM, STATUS_REJECTED, dto, "供应商拒单");
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        // 订单拒单后也自动完成关联待办
        if (order != null) {
            portalTodoService.autoFinishByBusiness("purchase_order", order.getId());
            domainEventPublisher.publish("supplier.order", "order.purchase.rejected",
                    DomainEvent.builder()
                            .eventType("order.purchase.rejected")
                            .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                    "supplierId", order.getSupplierId()))
                            .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "订单取消", businessIdExpr = "#id")
    public void cancel(Long id, OrderActionDTO dto) {
        dto = normalizeAction(dto);
        PurchaseOrder order = getOrderWithDataScope(id);
        if (!Integer.valueOf(STATUS_DRAFT).equals(order.getOrderStatus()) && !Integer.valueOf(STATUS_PENDING_CONFIRM).equals(order.getOrderStatus()) && !Integer.valueOf(STATUS_CONFIRMED).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前订单状态不允许取消");
        }
        order.setOrderStatus(STATUS_CANCELED);
        order.setCancelReason(dto.getRemark());
        order.setCancelTime(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
        writeTrack(order, STATUS_CANCELED, StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "订单取消");
        domainEventPublisher.publish("supplier.order", "order.purchase.canceled",
                DomainEvent.builder()
                        .eventType("order.purchase.canceled")
                        .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                "supplierId", order.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "采购方确认接单", businessIdExpr = "#id")
    public void confirmByBuyer(Long id, BuyerConfirmDTO dto) {
        PurchaseOrder order = getOrderWithDataScope(id);
        if (!Integer.valueOf(STATUS_CONFIRMED).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有供应商已确认的订单才能进行采购方确认");
        }
        order.setOrderStatus(STATUS_COMPLETED);
        purchaseOrderMapper.updateById(order);
        writeTrack(order, STATUS_COMPLETED,
                StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "采购方确认接单");
        domainEventPublisher.publish("supplier.order", "order.purchase.buyer_confirmed",
                DomainEvent.builder()
                        .eventType("order.purchase.buyer_confirmed")
                        .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                "supplierId", order.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "采购订单", businessType = "purchase_order", action = "订单关闭", businessIdExpr = "#id")
    public void close(Long id, OrderCloseDTO dto) {
        PurchaseOrder order = getOrderWithDataScope(id);
        if (!Integer.valueOf(STATUS_COMPLETED).equals(order.getOrderStatus())
                && !Integer.valueOf(STATUS_PARTIAL_SHIPPED).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "订单未完成或存在未处理异常，不允许关闭");
        }
        // 校验所有明细收货完成
        java.util.List<PurchaseOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>()
                        .eq(PurchaseOrderDetail::getOrderId, id));
        for (PurchaseOrderDetail detail : details) {
            BigDecimal received = detail.getReceivedQty() != null ? detail.getReceivedQty() : BigDecimal.ZERO;
            BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO;
            if (received.compareTo(qty) < 0) {
                throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(),
                        "明细" + detail.getMaterialCode() + " 收货数量不足，不允许关闭订单");
            }
        }
        order.setOrderStatus(STATUS_COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
        writeTrack(order, STATUS_COMPLETED,
                StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "订单关闭");
        domainEventPublisher.publish("supplier.order", "order.purchase.closed",
                DomainEvent.builder()
                        .eventType("order.purchase.closed")
                        .data(Map.of("businessId", order.getId(), "businessNo", order.getOrderNo(),
                                "supplierId", order.getSupplierId()))
                        .build());
    }

    @Override
    public void validateDeliveryQuantity(Long orderDetailId, BigDecimal newDeliveryQty) {
        PurchaseOrderDetail detail = detailMapper.selectById(orderDetailId);
        if (detail == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "订单明细不存在");
        }
        BigDecimal delivered = detail.getDeliveredQty() != null ? detail.getDeliveredQty() : BigDecimal.ZERO;
        BigDecimal total = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO;
        if (delivered.add(newDeliveryQty).compareTo(total) > 0) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(),
                    "收货数量超订单数量: 已发" + delivered + " + 本次" + newDeliveryQty + " > 订单数量" + total);
        }
    }

    private void changeStatus(Long id, int expectedStatus, int targetStatus, OrderActionDTO dto, String defaultRemark) {
        PurchaseOrder order = getOrderWithDataScope(id);
        if (!Integer.valueOf(expectedStatus).equals(order.getOrderStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前订单状态不允许执行该操作");
        }
        order.setOrderStatus(targetStatus);
        purchaseOrderMapper.updateById(order);
        writeTrack(order, targetStatus, StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : defaultRemark);
    }

    private void createSupplierTodo(PurchaseOrder order) {
        PortalTodoCreateDTO todoDto = new PortalTodoCreateDTO();
        todoDto.setSupplierId(order.getSupplierId());
        todoDto.setTodoType("order_confirm");
        todoDto.setBusinessType("purchase_order");
        todoDto.setBusinessId(order.getId());
        todoDto.setBusinessNo(order.getOrderNo());
        todoDto.setTitle("待确认采购订单" + order.getOrderNo());
        todoDto.setDueTime(order.getDeliveryDate() != null ? order.getDeliveryDate().atStartOfDay() : LocalDateTime.now().plusDays(3));
        portalTodoService.create(todoDto);
    }

    private void createSupplierMessage(PurchaseOrder order) {
        MessageNoticeCreateDTO msgDto = new MessageNoticeCreateDTO();
        msgDto.setReceiverSupplierId(order.getSupplierId());
        msgDto.setChannel(1);
        msgDto.setTitle("新采购订单待确认");
        msgDto.setContent("采购订单" + order.getOrderNo() + "已下发，金额￥" + order.getTotalAmount() + "，请及时确认。");
        msgDto.setBusinessType("purchase_order");
        msgDto.setBusinessId(order.getId());
        messageNoticeService.create(msgDto);
    }

    private PurchaseOrder getOrderWithDataScope(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !order.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return order;
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

    private void writeTrack(PurchaseOrder order, Integer status, String remark) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        OrderTrack track = new OrderTrack();
        track.setOrderId(order.getId());
        track.setTrackStatus(status);
        track.setTrackTime(LocalDateTime.now());
        track.setTrackRemark(remark);
        track.setOperator(loginUser == null ? null : loginUser.getUserId());
        track.setOperatorName(loginUser == null ? null : loginUser.getRealName());
        orderTrackMapper.insert(track);
    }

    private OrderActionDTO normalizeAction(OrderActionDTO dto) {
        return dto == null ? new OrderActionDTO() : dto;
    }
}