package com.supplier.order.converter;

import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.vo.PurchaseOrderDetailVO;
import com.supplier.order.vo.PurchaseOrderVO;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseOrderConverter {

    public static PurchaseOrder toEntity(PurchaseOrderCreateDTO dto) {
        PurchaseOrder entity = new PurchaseOrder();
        entity.setOrderNo(dto.getOrderNo());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setOrderDate(dto.getOrderDate());
        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setCurrency(dto.getCurrency());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setDiscountAmount(dto.getDiscountAmount());
        entity.setPayAmount(dto.getPayAmount());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setPaymentTerms(dto.getPaymentTerms());
        entity.setRemark(dto.getRemark());
        entity.setOrderStatus(0);
        return entity;
    }

    public static PurchaseOrderVO toVO(PurchaseOrder entity) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(entity.getId());
        vo.setOrderNo(entity.getOrderNo());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setOrderDate(entity.getOrderDate());
        vo.setDeliveryDate(entity.getDeliveryDate());
        vo.setCurrency(entity.getCurrency());
        vo.setTotalAmount(entity.getTotalAmount());
        vo.setPayAmount(entity.getPayAmount());
        vo.setOrderStatus(entity.getOrderStatus());
        vo.setConfirmTime(entity.getConfirmTime());
        vo.setBuyerId(entity.getBuyerId());
        vo.setBuyerName(entity.getBuyerName());
        vo.setDeliveryAddress(entity.getDeliveryAddress());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 将订单实体转为VO，并填充聚合数量和明细行
     */
    public static PurchaseOrderVO toVOWithDetails(PurchaseOrder entity, List<PurchaseOrderDetail> details) {
        PurchaseOrderVO vo = toVO(entity);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal shippedQty = BigDecimal.ZERO;
        BigDecimal receivedQty = BigDecimal.ZERO;

        if (details != null) {
            for (PurchaseOrderDetail d : details) {
                totalQty = totalQty.add(d.getQuantity() != null ? d.getQuantity() : BigDecimal.ZERO);
                shippedQty = shippedQty.add(d.getDeliveredQty() != null ? d.getDeliveredQty() : BigDecimal.ZERO);
                receivedQty = receivedQty.add(d.getReceivedQty() != null ? d.getReceivedQty() : BigDecimal.ZERO);
            }
        }

        vo.setTotalQty(totalQty);
        vo.setShippedQty(shippedQty);
        vo.setReceivedQty(receivedQty);
        vo.setInTransitQty(shippedQty.subtract(receivedQty).max(BigDecimal.ZERO));

        if (details != null) {
            vo.setDetails(details.stream().map(PurchaseOrderConverter::toDetailVO).toList());
        }

        return vo;
    }

    public static PurchaseOrderDetailVO toDetailVO(PurchaseOrderDetail e) {
        PurchaseOrderDetailVO vo = new PurchaseOrderDetailVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setLineNo(e.getLineNo());
        vo.setMaterialCode(e.getMaterialCode());
        vo.setMaterialName(e.getMaterialName());
        vo.setMaterialSpec(e.getMaterialSpec());
        vo.setMaterialModel(e.getMaterialModel());
        vo.setUnit(e.getUnit());
        vo.setQuantity(e.getQuantity());
        vo.setUnitPrice(e.getUnitPrice());
        vo.setTaxRate(e.getTaxRate());
        vo.setTaxAmount(e.getTaxAmount());
        vo.setAmount(e.getAmount());
        vo.setDeliveredQty(e.getDeliveredQty());
        vo.setReceivedQty(e.getReceivedQty());
        vo.setQualifiedQty(e.getQualifiedQty());
        vo.setDeliveryDate(e.getDeliveryDate());
        vo.setRemark(e.getRemark());
        return vo;
    }
}
