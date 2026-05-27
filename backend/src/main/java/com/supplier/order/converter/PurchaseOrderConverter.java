package com.supplier.order.converter;

import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.vo.PurchaseOrderVO;

public class PurchaseOrderConverter {

    public static PurchaseOrder toEntity(PurchaseOrderCreateDTO dto) {
        PurchaseOrder entity = new PurchaseOrder();
        entity.setOrderNo(dto.getOrderNo());
        entity.setSupplierId(dto.getSupplierId());
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
}
