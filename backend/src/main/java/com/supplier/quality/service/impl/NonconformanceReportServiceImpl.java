package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.quality.converter.NonconformanceReportConverter;
import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.dto.NcrActionDTO;
import com.supplier.quality.dto.NonconformanceReportCreateDTO;
import com.supplier.quality.entity.NonconformanceReport;
import com.supplier.quality.enums.HandleMethodEnum;
import com.supplier.quality.enums.NcrStatusEnum;
import com.supplier.quality.enums.SeverityEnum;
import com.supplier.quality.mapper.NonconformanceReportMapper;
import com.supplier.quality.query.NonconformanceReportQuery;
import com.supplier.quality.service.EightDReportService;
import com.supplier.quality.service.NonconformanceReportService;
import com.supplier.quality.vo.NonconformanceReportVO;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.service.DeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NonconformanceReportServiceImpl implements NonconformanceReportService {

    private final NonconformanceReportMapper nonconformanceReportMapper;
    private final EightDReportService eightDReportService;
    private final DeductionService deductionService;
    private final MessageNoticeService messageNoticeService;

    @Override
    public PageResult<NonconformanceReportVO> page(NonconformanceReportQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<NonconformanceReport> wrapper = new LambdaQueryWrapper<NonconformanceReport>()
                .eq(supplierId != null, NonconformanceReport::getSupplierId, supplierId)
                .eq(query.getNcrStatus() != null, NonconformanceReport::getNcrStatus, query.getNcrStatus())
                .ge(query.getStartDate() != null, NonconformanceReport::getCreateTime, query.getStartDate() != null ? query.getStartDate().atStartOfDay() : null)
                .le(query.getEndDate() != null, NonconformanceReport::getCreateTime, query.getEndDate() != null ? query.getEndDate().plusDays(1).atStartOfDay() : null)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(NonconformanceReport::getNcrNo, query.getKeyword())
                        .or()
                        .like(NonconformanceReport::getMaterialName, query.getKeyword())
                        .or()
                        .like(NonconformanceReport::getMaterialCode, query.getKeyword()))
                .orderByDesc(NonconformanceReport::getCreateTime);
        Page<NonconformanceReport> page = nonconformanceReportMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(NonconformanceReportConverter::toVO));
    }

    @Override
    public NonconformanceReportVO getDetail(Long id) {
        NonconformanceReport report = getWithScope(id);
        return NonconformanceReportConverter.toVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(NonconformanceReportCreateDTO dto) {
        NonconformanceReport entity = NonconformanceReportConverter.toEntity(dto);
        entity.setNcrNo(generateNcrNo());
        nonconformanceReportMapper.insert(entity);
        // 6.8: NCR 创建时通知供应商质量员
        notifySupplierNcrCreated(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "NCR", businessType = "nonconformance_report", action = "提交NCR", businessIdExpr = "#id")
    public void submit(Long id, NcrActionDTO dto) {
        dto = normalizeAction(dto);
        NonconformanceReport report = getWithScope(id);
        if (!Integer.valueOf(NcrStatusEnum.DRAFT.getCode()).equals(report.getNcrStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态的NCR可以提交");
        }
        report.setNcrStatus(NcrStatusEnum.PUBLISHED.getCode());
        report.setSubmitTime(LocalDateTime.now());
        nonconformanceReportMapper.updateById(report);
        // 6.8: NCR 状态变更通知
        notifyNcrStatusChanged(report, "已提交");
        // 6.5: 严重等级 >= 严重(2) 时自动创建 8D 报告
        if (report.getSeverity() != null && report.getSeverity() >= SeverityEnum.SERIOUS.getCode()) {
            autoCreate8D(report);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "NCR", businessType = "nonconformance_report", action = "处理NCR", businessIdExpr = "#id")
    public void handle(Long id, NcrActionDTO dto) {
        dto = normalizeAction(dto);
        NonconformanceReport report = getWithScope(id);
        if (!Integer.valueOf(NcrStatusEnum.PUBLISHED.getCode()).equals(report.getNcrStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已发布状态的NCR可以进行处理");
        }
        report.setNcrStatus(NcrStatusEnum.PROCESSING.getCode());
        report.setHandleMethod(dto.getHandleMethod());
        report.setHandleDetail(dto.getHandleDetail());
        report.setHandleRemark(dto.getHandleRemark());
        nonconformanceReportMapper.updateById(report);
        // 6.8: 状态变更通知
        String handleDesc = HandleMethodEnum.getDesc(dto.getHandleMethod());
        notifyNcrStatusChanged(report, "处理中（" + handleDesc + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "NCR", businessType = "nonconformance_report", action = "验证NCR", businessIdExpr = "#id")
    public void verify(Long id, NcrActionDTO dto) {
        dto = normalizeAction(dto);
        NonconformanceReport report = getWithScope(id);
        if (!Integer.valueOf(NcrStatusEnum.PROCESSING.getCode()).equals(report.getNcrStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有处理中状态的NCR可以进行验证");
        }
        report.setNcrStatus(NcrStatusEnum.PENDING_VERIFY.getCode());
        nonconformanceReportMapper.updateById(report);
        notifyNcrStatusChanged(report, "待验证");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "NCR", businessType = "nonconformance_report", action = "关闭NCR", businessIdExpr = "#id")
    public void close(Long id, NcrActionDTO dto) {
        dto = normalizeAction(dto);
        NonconformanceReport report = getWithScope(id);
        if (!Integer.valueOf(NcrStatusEnum.PENDING_VERIFY.getCode()).equals(report.getNcrStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待验证状态的NCR可以进行关闭");
        }
        report.setNcrStatus(NcrStatusEnum.CLOSED.getCode());
        report.setCloseTime(LocalDateTime.now());
        report.setCloseRemark(dto.getRemark());
        nonconformanceReportMapper.updateById(report);
        // 6.8: 状态变更通知
        notifyNcrStatusChanged(report, "已关闭");
        // 6.7: 处理方式为扣款时自动生成扣款单
        if (report.getHandleMethod() != null && report.getHandleMethod().equals(HandleMethodEnum.DEDUCTION.getCode())) {
            autoCreateDeduction(report);
        }
    }

    // ---- 6.5: 自动创建 8D 报告 ----
    private void autoCreate8D(NonconformanceReport report) {
        EightDReportCreateDTO dto = new EightDReportCreateDTO();
        dto.setNcrId(report.getId());
        dto.setSupplierId(report.getSupplierId());
        dto.setDueDate(LocalDate.now().plusDays(15));
        eightDReportService.create(dto);
    }

    // ---- 6.7: 自动创建扣款单 ----
    private void autoCreateDeduction(NonconformanceReport report) {
        DeductionCreateDTO dto = new DeductionCreateDTO();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        dto.setDeductionNo("DK" + datePart + String.format("%04d", System.currentTimeMillis() % 10000));
        dto.setSupplierId(report.getSupplierId());
        dto.setSourceType("NCR");
        dto.setSourceId(report.getId());
        dto.setDeductionType(1);
        dto.setDeductionAmount(java.math.BigDecimal.ZERO);
        dto.setDeductionReason("NCR质量异常扣款，" + report.getNcrNo() + "，" + (report.getProblemDesc() != null ? report.getProblemDesc() : ""));
        deductionService.create(dto);
    }

    // ---- 6.8: NCR 消息通知 ----
    private void notifySupplierNcrCreated(NonconformanceReport report) {
        if (report.getSupplierId() != null) {
            MessageNoticeCreateDTO msg = new MessageNoticeCreateDTO();
            msg.setReceiverSupplierId(report.getSupplierId());
            msg.setChannel(1);
            msg.setTitle("NCR质量异常通知");
            msg.setContent("NCR单号 " + report.getNcrNo() + " 已创建，物料 "
                    + report.getMaterialName() + "(" + report.getMaterialCode() + ")"
                    + "，不合格数量 " + report.getUnqualifiedQty() + "，请关注处理。");
            msg.setBusinessType("nonconformance_report");
            msg.setBusinessId(report.getId());
            messageNoticeService.create(msg);
        }
    }

    private void notifyNcrStatusChanged(NonconformanceReport report, String statusDesc) {
        if (report.getSupplierId() != null) {
            MessageNoticeCreateDTO msg = new MessageNoticeCreateDTO();
            msg.setReceiverSupplierId(report.getSupplierId());
            msg.setChannel(1);
            msg.setTitle("NCR状态变更通知");
            msg.setContent("NCR单号 " + report.getNcrNo() + " 状态变更为「" + statusDesc + "」，物料 "
                    + report.getMaterialName() + "(" + report.getMaterialCode() + ")。");
            msg.setBusinessType("nonconformance_report");
            msg.setBusinessId(report.getId());
            messageNoticeService.create(msg);
        }
    }

    private String generateNcrNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "NCR" + datePart;
        Long count = nonconformanceReportMapper.selectCount(
                new LambdaQueryWrapper<NonconformanceReport>()
                        .likeRight(NonconformanceReport::getNcrNo, prefix));
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    private NonconformanceReport getWithScope(Long id) {
        NonconformanceReport report = nonconformanceReportMapper.selectById(id);
        if (report == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && (report.getSupplierId() == null || !report.getSupplierId().equals(SecurityUtils.getSupplierId()))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return report;
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

    private NcrActionDTO normalizeAction(NcrActionDTO dto) {
        return dto == null ? new NcrActionDTO() : dto;
    }
}