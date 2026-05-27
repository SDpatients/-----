package com.supplier.quality.converter;

import com.supplier.quality.dto.QualityAppealCreateDTO;
import com.supplier.quality.entity.QualityAppeal;
import com.supplier.quality.vo.QualityAppealVO;

public class QualityAppealConverter {

    public static QualityAppeal toEntity(QualityAppealCreateDTO dto) {
        QualityAppeal entity = new QualityAppeal();
        entity.setNcrId(dto.getNcrId());
        entity.setInspectionId(dto.getInspectionId());
        entity.setSupplierId(dto.getSupplierId());
        entity.setAppealReason(dto.getAppealReason());
        entity.setAppealStatus(0);
        return entity;
    }

    public static QualityAppealVO toVO(QualityAppeal entity) {
        QualityAppealVO vo = new QualityAppealVO();
        vo.setId(entity.getId());
        vo.setAppealNo(entity.getAppealNo());
        vo.setNcrId(entity.getNcrId());
        vo.setInspectionId(entity.getInspectionId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setAppealReason(entity.getAppealReason());
        vo.setAppealStatus(entity.getAppealStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setAuditBy(entity.getAuditBy());
        vo.setAuditTime(entity.getAuditTime());
        vo.setAuditRemark(entity.getAuditRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}