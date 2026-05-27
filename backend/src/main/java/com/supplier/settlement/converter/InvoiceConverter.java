package com.supplier.settlement.converter;

import com.supplier.settlement.dto.InvoiceCreateDTO;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.vo.InvoiceVO;

public class InvoiceConverter {

    public static Invoice toEntity(InvoiceCreateDTO dto) {
        Invoice entity = new Invoice();
        entity.setInvoiceNo(dto.getInvoiceNo());
        entity.setInvoiceCode(dto.getInvoiceCode());
        entity.setInvoiceType(dto.getInvoiceType());
        entity.setReconId(dto.getReconId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setTaxNumber(dto.getTaxNumber());
        entity.setInvoiceAmount(dto.getInvoiceAmount());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setTaxRate(dto.getTaxRate());
        entity.setInvoiceDate(dto.getInvoiceDate());
        entity.setRemark(dto.getRemark());
        entity.setInvoiceStatus(0);
        entity.setOcrStatus(0);
        return entity;
    }

    public static InvoiceVO toVO(Invoice entity) {
        InvoiceVO vo = new InvoiceVO();
        vo.setId(entity.getId());
        vo.setInvoiceNo(entity.getInvoiceNo());
        vo.setInvoiceCode(entity.getInvoiceCode());
        vo.setInvoiceType(entity.getInvoiceType());
        vo.setReconId(entity.getReconId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setTaxNumber(entity.getTaxNumber());
        vo.setInvoiceAmount(entity.getInvoiceAmount());
        vo.setTaxAmount(entity.getTaxAmount());
        vo.setTaxRate(entity.getTaxRate());
        vo.setInvoiceDate(entity.getInvoiceDate());
        vo.setInvoiceStatus(entity.getInvoiceStatus());
        vo.setReceiveTime(entity.getReceiveTime());
        vo.setCertifyTime(entity.getCertifyTime());
        vo.setVoidTime(entity.getVoidTime());
        vo.setVoidReason(entity.getVoidReason());
        vo.setFileId(entity.getFileId());
        vo.setOcrStatus(entity.getOcrStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}