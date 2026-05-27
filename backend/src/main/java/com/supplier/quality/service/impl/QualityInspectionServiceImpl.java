package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.service.BizStatusTrackService;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.quality.converter.InspectionStandardConverter;
import com.supplier.quality.dto.NonconformanceReportCreateDTO;
import com.supplier.quality.dto.QualityHandleDTO;
import com.supplier.quality.dto.QualityInspectionCreateDTO;
import com.supplier.quality.dto.QualitySubmitDTO;
import com.supplier.quality.entity.InspectionStandard;
import com.supplier.quality.entity.QualityInspection;
import com.supplier.quality.mapper.InspectionStandardMapper;
import com.supplier.quality.mapper.QualityInspectionMapper;
import com.supplier.quality.query.QualityInspectionQuery;
import com.supplier.quality.service.NonconformanceReportService;
import com.supplier.quality.service.QualityInspectionService;
import com.supplier.quality.vo.InspectionStandardVO;
import com.supplier.quality.vo.QualityInspectionVO;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QualityInspectionServiceImpl implements QualityInspectionService {
    private final QualityInspectionMapper qualityInspectionMapper;
    private final InspectionStandardMapper inspectionStandardMapper;
    private final MessageNoticeService messageNoticeService;
    private final DomainEventPublisher domainEventPublisher;
    private final BizStatusTrackService bizStatusTrackService;
    private final NonconformanceReportService nonconformanceReportService;

    @Override
    public PageResult<QualityInspectionVO> page(QualityInspectionQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<QualityInspection> wrapper = new LambdaQueryWrapper<QualityInspection>()
                .eq(supplierId != null, QualityInspection::getSupplierId, supplierId)
                .eq(query.getReceiptId() != null, QualityInspection::getReceiptId, query.getReceiptId())
                .eq(query.getDeliveryId() != null, QualityInspection::getDeliveryId, query.getDeliveryId())
                .eq(query.getInspectResult() != null, QualityInspection::getInspectResult, query.getInspectResult())
                .eq(query.getInspectType() != null, QualityInspection::getInspectType, query.getInspectType())
                .eq(StringUtils.hasText(query.getMaterialCode()), QualityInspection::getMaterialCode, query.getMaterialCode())
                .ge(query.getStartTime() != null, QualityInspection::getInspectTime, query.getStartTime())
                .le(query.getEndTime() != null, QualityInspection::getInspectTime, query.getEndTime())
                .orderByDesc(QualityInspection::getCreateTime);
        Page<QualityInspection> page = qualityInspectionMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public QualityInspectionVO getDetail(Long id) {
        QualityInspection inspection = getWithScope(id);
        QualityInspectionVO vo = toVO(inspection);
        if (inspection.getStandardId() != null) {
            InspectionStandard standard = inspectionStandardMapper.selectById(inspection.getStandardId());
            if (standard != null) {
                vo.setStandard(InspectionStandardConverter.toVO(standard));
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(QualityInspectionCreateDTO dto) {
        QualityInspection entity = new QualityInspection();
        entity.setInspectionNo(generateInspectionNo());
        entity.setReceiptId(dto.getReceiptId());
        entity.setDeliveryId(dto.getDeliveryId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setInspectQty(dto.getInspectQty());
        entity.setInspectType(dto.getInspectType());
        entity.setInspectResult(0);
        entity.setInspectRemark(dto.getInspectRemark());
        if (dto.getSupplierId() != null) {
            entity.setSupplierId(dto.getSupplierId());
        } else if (SecurityUtils.isSupplierUser()) {
            entity.setSupplierId(SecurityUtils.getSupplierId());
        }
        // 自动匹配检验标准
        Long standardId = dto.getStandardId();
        if (standardId == null) {
            InspectionStandard standard = inspectionStandardMapper.selectOne(
                    new LambdaQueryWrapper<InspectionStandard>()
                            .eq(InspectionStandard::getMaterialCode, dto.getMaterialCode())
                            .eq(InspectionStandard::getStatus, 1)
                            .last("LIMIT 1"));
            if (standard != null) {
                standardId = standard.getId();
            }
        }
        entity.setStandardId(standardId);
        qualityInspectionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "质检", businessType = "quality_inspection", action = "提交检验", businessIdExpr = "#id")
    public void submit(Long id, QualitySubmitDTO dto) {
        QualityInspection inspection = getWithScope(id);
        if (!Integer.valueOf(0).equals(inspection.getInspectResult())) {
            String statusName = inspectResultName(inspection.getInspectResult());
            String submitInfo = String.format("质检单 %s 当前状态为「%s」",
                    inspection.getInspectionNo(), statusName);
            if (inspection.getInspectTime() != null && inspection.getInspectorName() != null) {
                submitInfo += String.format("，已于 %s 由 %s 提交",
                        inspection.getInspectTime().toLocalDate(), inspection.getInspectorName());
            }
            submitInfo += "，不允许重复提交。如需修改检验结果，请联系管理员";
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), submitInfo);
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        inspection.setInspectQty(dto.getInspectQty());
        inspection.setQualifiedQty(dto.getQualifiedQty());
        inspection.setUnqualifiedQty(dto.getUnqualifiedQty());
        inspection.setInspectResult(dto.getInspectResult());
        inspection.setInspectType(dto.getInspectType());
        inspection.setInspectTime(LocalDateTime.now());
        inspection.setInspector(loginUser == null ? null : loginUser.getUserId());
        inspection.setInspectorName(loginUser == null ? null : loginUser.getRealName());
        inspection.setInspectRemark(dto.getInspectRemark());
        qualityInspectionMapper.updateById(inspection);
        bizStatusTrackService.writeTrack("quality_inspection", inspection.getId(), 0, inspection.getInspectResult(), "提交检验结果");
        // 不合格时通知供应商
        if (dto.getInspectResult() != null && (dto.getInspectResult() == 2 || dto.getInspectResult() == 3)) {
            notifySupplierUnqualified(inspection);
        }
        // 6.3: IQC 不合格时自动创建 NCR
        if (inspection.getUnqualifiedQty() != null && inspection.getUnqualifiedQty().compareTo(java.math.BigDecimal.ZERO) > 0) {
            autoCreateNcr(inspection);
        }
        domainEventPublisher.publish("supplier.quality", "quality.inspection.submitted",
                DomainEvent.builder()
                        .eventType("quality.inspection.submitted")
                        .data(Map.of("businessId", inspection.getId(), "inspectResult", inspection.getInspectResult(),
                                "materialCode", inspection.getMaterialCode(),
                                "supplierId", inspection.getSupplierId()))
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "质检", businessType = "quality_inspection", action = "质检处理", businessIdExpr = "#id")
    public void handle(Long id, QualityHandleDTO dto) {
        QualityInspection inspection = getWithScope(id);
        if (Integer.valueOf(0).equals(inspection.getInspectResult())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(),
                    String.format("质检单 %s 当前状态为「待检验」，请先提交检验结果后再处理", inspection.getInspectionNo()));
        }
        inspection.setHandleMethod(dto.getHandleMethod());
        inspection.setHandleRemark(dto.getHandleRemark());
        qualityInspectionMapper.updateById(inspection);
        bizStatusTrackService.writeTrack("quality_inspection", inspection.getId(), inspection.getInspectResult(), null, "处理检验结果: " + dto.getHandleMethod());
    }

    // ---- 6.3: 自动创建 NCR ----
    private void autoCreateNcr(QualityInspection inspection) {
        NonconformanceReportCreateDTO ncrDto = new NonconformanceReportCreateDTO();
        ncrDto.setInspectionId(inspection.getId());
        ncrDto.setSupplierId(inspection.getSupplierId());
        ncrDto.setMaterialCode(inspection.getMaterialCode());
        ncrDto.setMaterialName(inspection.getMaterialName());
        ncrDto.setUnqualifiedQty(inspection.getUnqualifiedQty());
        ncrDto.setProblemDesc("IQC检验不合格，物料" + inspection.getMaterialName()
                + "(" + inspection.getMaterialCode() + ")，检验数量" + inspection.getInspectQty()
                + "，不合格数量" + inspection.getUnqualifiedQty());
        ncrDto.setSeverity(inspection.getUnqualifiedQty().compareTo(inspection.getInspectQty()) >= 0 ? 2 : 1);
        nonconformanceReportService.create(ncrDto);
    }

    private String generateInspectionNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "QCI" + datePart;
        Long count = qualityInspectionMapper.selectCount(
                new LambdaQueryWrapper<QualityInspection>()
                        .likeRight(QualityInspection::getInspectionNo, prefix));
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    private void notifySupplierUnqualified(QualityInspection inspection) {
        if (inspection.getSupplierId() != null) {
            MessageNoticeCreateDTO msgDto = new MessageNoticeCreateDTO();
            msgDto.setReceiverSupplierId(inspection.getSupplierId());
            msgDto.setChannel(1);
            msgDto.setTitle("质量检验不合格通知");
            msgDto.setContent("物料" + inspection.getMaterialName() + "(" + inspection.getMaterialCode() + ")检验不合格，不合格数量" + inspection.getUnqualifiedQty() + "，请关注后续处理。");
            msgDto.setBusinessType("quality_inspection");
            msgDto.setBusinessId(inspection.getId());
            messageNoticeService.create(msgDto);
        }
    }

    private QualityInspection getWithScope(Long id) {
        QualityInspection inspection = qualityInspectionMapper.selectById(id);
        if (inspection == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && (inspection.getSupplierId() == null || !inspection.getSupplierId().equals(SecurityUtils.getSupplierId()))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return inspection;
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

    private String inspectResultName(Integer code) {
        if (code == null) return "未知";
        return switch (code) {
            case 0 -> "待检验";
            case 1 -> "合格";
            case 2 -> "不合格";
            case 3 -> "部分合格";
            default -> "未知(" + code + ")";
        };
    }

    private QualityInspectionVO toVO(QualityInspection e) {
        QualityInspectionVO vo = new QualityInspectionVO();
        vo.setId(e.getId());
        vo.setInspectionNo(e.getInspectionNo());
        vo.setReceiptId(e.getReceiptId());
        vo.setDeliveryId(e.getDeliveryId());
        vo.setSupplierId(e.getSupplierId());
        vo.setStandardId(e.getStandardId());
        vo.setMaterialCode(e.getMaterialCode());
        vo.setMaterialName(e.getMaterialName());
        vo.setInspectQty(e.getInspectQty());
        vo.setQualifiedQty(e.getQualifiedQty());
        vo.setUnqualifiedQty(e.getUnqualifiedQty());
        vo.setInspectResult(e.getInspectResult());
        vo.setInspectType(e.getInspectType());
        vo.setInspectTime(e.getInspectTime());
        vo.setInspectorName(e.getInspectorName());
        vo.setInspectRemark(e.getInspectRemark());
        vo.setHandleMethod(e.getHandleMethod());
        vo.setHandleRemark(e.getHandleRemark());
        return vo;
    }
}