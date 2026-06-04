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
import com.supplier.quality.dto.QualityInspectionCreateDTO;
import com.supplier.quality.entity.InspectionStandard;
import com.supplier.quality.mapper.InspectionStandardMapper;
import com.supplier.quality.service.QualityInspectionService;
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
    private final QualityInspectionService qualityInspectionService;
    private final InspectionStandardMapper inspectionStandardMapper;

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
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 4, "ASN确认入库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "ASN", businessType = "delivery_notice", action = "触发质检", businessIdExpr = "#id")
    public void triggerQuality(Long id) {
        DeliveryNotice notice = getWithScope(id);
        // 已送达(3)或已收货(4)状态可触发质检
        if (!Integer.valueOf(3).equals(notice.getDeliveryStatus()) && !Integer.valueOf(4).equals(notice.getDeliveryStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已送达或已收货的通知单可触发质检");
        }
        // 获取ASN明细行，为每个物料创建质检单
        List<DeliveryDetail> details = deliveryDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, id));
        if (CollectionUtils.isEmpty(details)) {
            throw BusinessException.of(ResultCode.PARAM_ERROR.getCode(), "该通知单无明细行，无法触发质检");
        }
        for (DeliveryDetail detail : details) {
            // 查找检验标准
            InspectionStandard standard = inspectionStandardMapper.selectOne(
                    new LambdaQueryWrapper<InspectionStandard>().eq(InspectionStandard::getMaterialCode, detail.getMaterialCode()));
            int strategy = (standard != null && standard.getInspectionStrategy() != null)
                    ? standard.getInspectionStrategy() : 1;
            // 免检策略(0)则跳过
            if (strategy == 0) {
                continue;
            }
            QualityInspectionCreateDTO qiDto = new QualityInspectionCreateDTO();
            qiDto.setDeliveryId(id);
            qiDto.setMaterialCode(detail.getMaterialCode());
            qiDto.setMaterialName(detail.getMaterialName());
            qiDto.setInspectQty(detail.getActualQty() != null ? detail.getActualQty() : BigDecimal.ZERO);
            qiDto.setInspectType(strategy);
            qualityInspectionService.create(qiDto);
        }
        // 更新状态为质检中(6)
        int beforeStatus = notice.getDeliveryStatus();
        notice.setDeliveryStatus(6);
        deliveryNoticeMapper.updateById(notice);
        bizStatusTrackService.writeTrack("delivery_notice", notice.getId(), beforeStatus, 6, "触发质检");
        domainEventPublisher.publish("supplier.delivery", "delivery.asn.quality.triggered",
                DomainEvent.builder()
                        .eventType("delivery.asn.quality.triggered")
                        .data(Map.of("businessId", notice.getId(), "businessNo", notice.getNoticeNo(),
                                "supplierId", notice.getSupplierId()))
                        .build());
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
}
