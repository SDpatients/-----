package com.supplier.quality.converter;

import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.entity.EightDReport;
import com.supplier.quality.vo.EightDReportVO;

public class EightDReportConverter {

    public static EightDReport toEntity(EightDReportCreateDTO dto) {
        EightDReport entity = new EightDReport();
        entity.setNcrId(dto.getNcrId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setD1Team(dto.getD1Team());
        entity.setD2Problem(dto.getD2Problem());
        entity.setD3Containment(dto.getD3Containment());
        entity.setD4RootCause(dto.getD4RootCause());
        entity.setD5CorrectiveAction(dto.getD5CorrectiveAction());
        entity.setD6ValidateAction(dto.getD6ValidateAction());
        entity.setD7PreventAction(dto.getD7PreventAction());
        entity.setD8CloseSummary(dto.getD8CloseSummary());
        entity.setDueDate(dto.getDueDate());
        entity.setCurrentStep(dto.getCurrentStep() != null ? dto.getCurrentStep() : 1);
        entity.setReportStatus(0);
        return entity;
    }

    public static EightDReportVO toVO(EightDReport entity) {
        EightDReportVO vo = new EightDReportVO();
        vo.setId(entity.getId());
        vo.setReportNo(entity.getReportNo());
        vo.setNcrId(entity.getNcrId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setD1Team(entity.getD1Team());
        vo.setD2Problem(entity.getD2Problem());
        vo.setD3Containment(entity.getD3Containment());
        vo.setD4RootCause(entity.getD4RootCause());
        vo.setD5CorrectiveAction(entity.getD5CorrectiveAction());
        vo.setD6ValidateAction(entity.getD6ValidateAction());
        vo.setD7PreventAction(entity.getD7PreventAction());
        vo.setD8CloseSummary(entity.getD8CloseSummary());
        vo.setDueDate(entity.getDueDate());
        vo.setCurrentStep(entity.getCurrentStep());
        vo.setStepDueDate(entity.getStepDueDate());
        vo.setReportStatus(entity.getReportStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setAuditTime(entity.getAuditTime());
        vo.setCloseTime(entity.getCloseTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}