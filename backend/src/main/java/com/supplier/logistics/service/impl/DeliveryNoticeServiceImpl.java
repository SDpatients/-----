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
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryDetailItemDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
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
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryNoticeServiceImpl implements DeliveryNoticeService {
    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final DeliveryDetailMapper deliveryDetailMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final BizStatusTrackService bizStatusTrackService;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    private static final int STATUS_CONFIRMED = 2;
    private static final int STATUS_PARTIAL_SHIPPED = 3;

    @Override
    public PageResult<DeliveryNoticeVO> page(DeliveryNoticeQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<DeliveryNotice> wrapper = new LambdaQueryWrapper<DeliveryNotice>()
                .eq(supplierId != null, DeliveryNotice::getSupplierId, supplierId)
                .eq(query.getDeliveryStatus() != null, DeliveryNotice::getDeliveryStatus, query.getDeliveryStatus())
                .ge(query.getStartDate() != null, DeliveryNotice::getPlanDeliveryDate, query.getStartDate())
                .le(query.getEndDate() != null, DeliveryNotice::getPlanDeliveryDate, query.getEndDate())
                .and(StringUtils.hasText(query.getKeyword()), w -> w.like(DeliveryNotice::getNoticeNo, query.getKeyword()).or().like(DeliveryNotice::getOrderNo, query.getKeyword()))
                .orderByDesc(DeliveryNotice::getCreateTime);
        Page<DeliveryNotice> page = deliveryNoticeMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
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
            DeliveryDetailVO dvo = new DeliveryDetailVO();
            dvo.setId(d.getId()); dvo.setNoticeId(d.getNoticeId()); dvo.setOrderDetailId(d.getOrderDetailId());
            dvo.setMaterialCode(d.getMaterialCode()); dvo.setMaterialName(d.getMaterialName());
            dvo.setMaterialSpec(d.getMaterialSpec()); dvo.setUnit(d.getUnit());
            dvo.setPlanQty(d.getPlanQty()); dvo.setActualQty(d.getActualQty());
            dvo.setReceivedQty(d.getReceivedQty()); dvo.setQualifiedQty(d.getQualifiedQty());
            dvo.setBatchNo(d.getBatchNo()); dvo.setProductionDate(d.getProductionDate());
            dvo.setExpiryDate(d.getExpiryDate()); dvo.setRemark(d.getRemark());
            detailVOs.add(dvo);
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
                detail.setRemark(item.getRemark());
                deliveryDetailMapper.insert(detail);

                // 更新订单明细已发数量
                orderDetail.setDeliveredQty(deliveredQty.add(item.getActualQty()));
                purchaseOrderDetailMapper.updateById(orderDetail);
            }
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
        vo.setId(e.getId()); vo.setNoticeNo(e.getNoticeNo()); vo.setOrderId(e.getOrderId()); vo.setOrderNo(e.getOrderNo()); vo.setSupplierId(e.getSupplierId()); vo.setSupplierName(e.getSupplierName()); vo.setPlanDeliveryDate(e.getPlanDeliveryDate()); vo.setActualDeliveryDate(e.getActualDeliveryDate()); vo.setDeliveryStatus(e.getDeliveryStatus()); vo.setDeliveryMethod(e.getDeliveryMethod()); vo.setDeliveryCompany(e.getDeliveryCompany()); vo.setDeliveryNo(e.getDeliveryNo()); vo.setDeliveryAddress(e.getDeliveryAddress()); vo.setReceiver(e.getReceiver()); vo.setSendTime(e.getSendTime()); vo.setArriveTime(e.getArriveTime()); vo.setBatchNo(e.getBatchNo()); vo.setProductionDate(e.getProductionDate()); vo.setExpiryDate(e.getExpiryDate()); vo.setRemark(e.getRemark());
        return vo;
    }
}
