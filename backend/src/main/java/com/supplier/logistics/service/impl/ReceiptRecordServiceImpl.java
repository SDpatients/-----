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
import com.supplier.logistics.dto.ReceiptConfirmDTO;
import com.supplier.logistics.dto.ReceiptCreateDTO;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.entity.DeliveryBarcode;
import com.supplier.logistics.entity.DeliveryDetail;
import com.supplier.logistics.entity.DeliveryPackage;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.DeliveryBarcodeMapper;
import com.supplier.logistics.mapper.DeliveryDetailMapper;
import com.supplier.logistics.mapper.DeliveryPackageMapper;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.logistics.query.ReceiptRecordQuery;
import com.supplier.logistics.service.ReceiptDiffService;
import com.supplier.logistics.service.ReceiptRecordService;
import com.supplier.logistics.vo.ReceiptRecordVO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceiptRecordServiceImpl implements ReceiptRecordService {
    private final ReceiptRecordMapper receiptRecordMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final BizStatusTrackService bizStatusTrackService;
    private final DeliveryBarcodeMapper barcodeMapper;
    private final DeliveryPackageMapper packageMapper;
    private final ReceiptDiffService receiptDiffService;
    private final DeliveryDetailMapper deliveryDetailMapper;
    private final PurchaseOrderDetailMapper purchaseOrderDetailMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public PageResult<ReceiptRecordVO> page(ReceiptRecordQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<ReceiptRecord> wrapper = new LambdaQueryWrapper<ReceiptRecord>()
                .eq(supplierId != null, ReceiptRecord::getSupplierId, supplierId)
                .eq(query.getNoticeId() != null, ReceiptRecord::getNoticeId, query.getNoticeId())
                .eq(query.getDeliveryId() != null, ReceiptRecord::getDeliveryId, query.getDeliveryId())
                .eq(query.getReceiptStatus() != null, ReceiptRecord::getReceiptStatus, query.getReceiptStatus())
                .eq(StringUtils.hasText(query.getMaterialCode()), ReceiptRecord::getMaterialCode, query.getMaterialCode())
                .ge(query.getStartTime() != null, ReceiptRecord::getReceiptTime, query.getStartTime())
                .le(query.getEndTime() != null, ReceiptRecord::getReceiptTime, query.getEndTime())
                .orderByDesc(ReceiptRecord::getCreateTime);
        Page<ReceiptRecord> page = receiptRecordMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public ReceiptRecordVO getDetail(Long id) {
        return toVO(getWithScope(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReceiptCreateDTO dto) {
        ReceiptRecord entity = new ReceiptRecord();
        entity.setDeliveryId(dto.getDeliveryId());
        entity.setNoticeId(dto.getNoticeId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setPlanQty(dto.getPlanQty());
        entity.setReceiptQty(dto.getReceiptQty());
        entity.setRejectQty(dto.getRejectQty());
        entity.setReceiptTime(dto.getReceiptTime());
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setLocation(dto.getLocation());
        entity.setReceiptStatus(0);
        entity.setRemark(dto.getRemark());
        // 供应商用户操作时记录 supplier_id
        if (SecurityUtils.isSupplierUser()) {
            entity.setSupplierId(SecurityUtils.getSupplierId());
        }
        receiptRecordMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "收货", businessType = "receipt_record", action = "确认收货", businessIdExpr = "#id")
    public void confirm(Long id, ReceiptConfirmDTO dto) {
        ReceiptRecord record = getWithScope(id);
        if (!Integer.valueOf(0).equals(record.getReceiptStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前收货记录不允许确认");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        record.setReceiptStatus(1);
        record.setReceiptQty(dto.getReceiptQty());
        record.setRejectQty(dto.getRejectQty());
        record.setReceiptTime(LocalDateTime.now());
        record.setReceiver(loginUser == null ? null : loginUser.getUserId());
        record.setReceiverName(loginUser == null ? null : loginUser.getRealName());
        record.setWarehouseId(dto.getWarehouseId());
        record.setWarehouseName(dto.getWarehouseName());
        record.setLocation(dto.getLocation());
        record.setRejectReason(dto.getRejectReason());
        record.setRemark(dto.getRemark());
        receiptRecordMapper.updateById(record);
        bizStatusTrackService.writeTrack("receipt_record", record.getId(), 0, 1, "确认收货");

        // 回写 delivery_detail.receivedQty 和 purchase_order_detail.receivedQty
        writeBackReceivedQty(record);

        // 检查订单是否所有明细已全部收货，自动完成订单
        tryAutoCompleteOrderFromReceipt(record);

        domainEventPublisher.publish("supplier.delivery", "delivery.receipt.confirmed",
                DomainEvent.builder()
                        .eventType("delivery.receipt.confirmed")
                        .data(Map.of("businessId", record.getId(), "deliveryId", record.getDeliveryId(),
                                "receiptQty", record.getReceiptQty(), "supplierId", record.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "收货", businessType = "receipt_record", action = "收货拒收", businessIdExpr = "#id")
    public void reject(Long id, ReceiptConfirmDTO dto) {
        ReceiptRecord record = getWithScope(id);
        if (!Integer.valueOf(0).equals(record.getReceiptStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前收货记录不允许拒收");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        record.setReceiptStatus(2);
        record.setRejectQty(dto.getRejectQty());
        record.setReceiptTime(LocalDateTime.now());
        record.setReceiver(loginUser == null ? null : loginUser.getUserId());
        record.setReceiverName(loginUser == null ? null : loginUser.getRealName());
        record.setRejectReason(dto.getRejectReason());
        record.setRemark(dto.getRemark());
        receiptRecordMapper.updateById(record);
        bizStatusTrackService.writeTrack("receipt_record", record.getId(), 0, 2, "拒收: " + (dto.getRejectReason() != null ? dto.getRejectReason() : "未知原因"));
        domainEventPublisher.publish("supplier.delivery", "delivery.receipt.confirmed",
                DomainEvent.builder()
                        .eventType("delivery.receipt.confirmed")
                        .data(Map.of("businessId", record.getId(), "deliveryId", record.getDeliveryId(),
                                "receiptStatus", 2, "supplierId", record.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReceiptRecordVO scan(String barcode) {
        DeliveryBarcode bc = barcodeMapper.selectOne(
                new LambdaQueryWrapper<DeliveryBarcode>().eq(DeliveryBarcode::getBarcode, barcode));
        if (bc == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "条码不存在: " + barcode);
        }
        DeliveryPackage pack = packageMapper.selectById(bc.getPackageId());
        if (pack == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "箱信息不存在");
        }
        // 根据扫码信息返回预填充的收货信息，由前端确认后调用 create + confirm
        ReceiptRecordVO vo = new ReceiptRecordVO();
        vo.setNoticeId(bc.getNoticeId());
        vo.setDeliveryId(bc.getPackageId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long adjust(Long id, ReceiptAdjustDTO dto) {
        return receiptDiffService.adjust(id, dto);
    }

    private ReceiptRecord getWithScope(Long id) {
        ReceiptRecord record = receiptRecordMapper.selectById(id);
        if (record == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && (record.getSupplierId() == null || !record.getSupplierId().equals(SecurityUtils.getSupplierId()))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return record;
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

    private ReceiptRecordVO toVO(ReceiptRecord e) {
        ReceiptRecordVO vo = new ReceiptRecordVO();
        vo.setId(e.getId()); vo.setDeliveryId(e.getDeliveryId()); vo.setNoticeId(e.getNoticeId()); vo.setMaterialCode(e.getMaterialCode()); vo.setMaterialName(e.getMaterialName()); vo.setPlanQty(e.getPlanQty()); vo.setReceiptQty(e.getReceiptQty()); vo.setRejectQty(e.getRejectQty()); vo.setReceiptTime(e.getReceiptTime()); vo.setReceiverName(e.getReceiverName()); vo.setWarehouseName(e.getWarehouseName()); vo.setLocation(e.getLocation()); vo.setReceiptStatus(e.getReceiptStatus()); vo.setRejectReason(e.getRejectReason()); vo.setRemark(e.getRemark());
        return vo;
    }

    /**
     * 收货确认后检查关联订单是否所有明细已全部收货，自动完成订单
     */
    private void tryAutoCompleteOrderFromReceipt(ReceiptRecord record) {
        if (record.getNoticeId() == null) return;
        // 从 delivery_detail 找到 orderDetailId，再找到 orderId
        DeliveryDetail deliveryDetail = deliveryDetailMapper.selectOne(
                new LambdaQueryWrapper<DeliveryDetail>()
                        .eq(DeliveryDetail::getNoticeId, record.getNoticeId())
                        .eq(DeliveryDetail::getMaterialCode, record.getMaterialCode())
                        .last("LIMIT 1"));
        if (deliveryDetail == null || deliveryDetail.getOrderDetailId() == null) return;
        PurchaseOrderDetail orderDetail = purchaseOrderDetailMapper.selectById(deliveryDetail.getOrderDetailId());
        if (orderDetail == null) return;
        Long orderId = orderDetail.getOrderId();

        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !Integer.valueOf(3).equals(order.getOrderStatus())) {
            return; // 仅部分发货状态才自动完成
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
        }
    }

    /**
     * 收货确认后回写 delivery_detail.receivedQty 和 purchase_order_detail.receivedQty
     */
    private void writeBackReceivedQty(ReceiptRecord record) {
        if (record.getNoticeId() == null || !StringUtils.hasText(record.getMaterialCode())) {
            return;
        }
        BigDecimal receiptQty = record.getReceiptQty() != null ? record.getReceiptQty() : BigDecimal.ZERO;

        // 1. 查找对应的 delivery_detail
        DeliveryDetail deliveryDetail = deliveryDetailMapper.selectOne(
                new LambdaQueryWrapper<DeliveryDetail>()
                        .eq(DeliveryDetail::getNoticeId, record.getNoticeId())
                        .eq(DeliveryDetail::getMaterialCode, record.getMaterialCode())
                        .last("LIMIT 1"));
        if (deliveryDetail == null) {
            return;
        }

        // 2. 更新 delivery_detail.receivedQty
        BigDecimal currentReceived = deliveryDetail.getReceivedQty() != null ? deliveryDetail.getReceivedQty() : BigDecimal.ZERO;
        deliveryDetail.setReceivedQty(currentReceived.add(receiptQty));
        deliveryDetailMapper.updateById(deliveryDetail);

        // 3. 回写 purchase_order_detail.receivedQty
        if (deliveryDetail.getOrderDetailId() != null) {
            PurchaseOrderDetail orderDetail = purchaseOrderDetailMapper.selectById(deliveryDetail.getOrderDetailId());
            if (orderDetail != null) {
                BigDecimal orderReceived = orderDetail.getReceivedQty() != null ? orderDetail.getReceivedQty() : BigDecimal.ZERO;
                orderDetail.setReceivedQty(orderReceived.add(receiptQty));
                purchaseOrderDetailMapper.updateById(orderDetail);
            }
        }
    }
}
