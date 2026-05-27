package com.supplier.quality.converter;

import com.supplier.quality.dto.NonconformanceReportCreateDTO;
import com.supplier.quality.entity.NonconformanceReport;
import com.supplier.quality.vo.NonconformanceReportVO;

public class NonconformanceReportConverter {

    public static NonconformanceReport toEntity(NonconformanceReportCreateDTO dto) {
        NonconformanceReport entity = new NonconformanceReport();
        entity.setSupplierId(dto.getSupplierId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setUnqualifiedQty(dto.getUnqualifiedQty());
        entity.setProblemDesc(dto.getProblemDesc());
        entity.setSeverity(dto.getSeverity());
        entity.setInspectionId(dto.getInspectionId());
        entity.setNcrStatus(0);
        return entity;
    }

    public static NonconformanceReportVO toVO(NonconformanceReport entity) {
        NonconformanceReportVO vo = new NonconformanceReportVO();
        vo.setId(entity.getId());
        vo.setNcrNo(entity.getNcrNo());
        vo.setInspectionId(entity.getInspectionId());
        vo.setReceiptId(entity.getReceiptId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setMaterialName(entity.getMaterialName());
        vo.setUnqualifiedQty(entity.getUnqualifiedQty());
        vo.setProblemDesc(entity.getProblemDesc());
        vo.setSeverity(entity.getSeverity());
        vo.setHandleMethod(entity.getHandleMethod());
        vo.setHandleDetail(entity.getHandleDetail());
        vo.setHandleRemark(entity.getHandleRemark());
        vo.setNcrStatus(entity.getNcrStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setCloseTime(entity.getCloseTime());
        vo.setCloseRemark(entity.getCloseRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}