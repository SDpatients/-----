package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.quality.converter.EightDReportConverter;
import com.supplier.quality.dto.EightDActionDTO;
import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.dto.EightDReportUpdateDTO;
import com.supplier.quality.entity.EightDReport;
import com.supplier.quality.enums.EightDStepEnum;
import com.supplier.quality.mapper.EightDReportMapper;
import com.supplier.quality.query.EightDReportQuery;
import com.supplier.quality.service.EightDReportService;
import com.supplier.quality.vo.EightDReportVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EightDReportServiceImpl implements EightDReportService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_SUBMITTED = 1;
    private static final int STATUS_AUDITING = 2;
    private static final int STATUS_REJECTED = 3;
    private static final int STATUS_CLOSED = 4;

    private final EightDReportMapper eightDReportMapper;
    private final MessageNoticeService messageNoticeService;

    @Override
    public PageResult<EightDReportVO> page(EightDReportQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<EightDReport> wrapper = new LambdaQueryWrapper<EightDReport>()
                .eq(supplierId != null, EightDReport::getSupplierId, supplierId)
                .eq(query.getNcrId() != null, EightDReport::getNcrId, query.getNcrId())
                .eq(query.getReportStatus() != null, EightDReport::getReportStatus, query.getReportStatus())
                .ge(query.getStartDate() != null, EightDReport::getCreateTime, query.getStartDate() != null ? query.getStartDate().atStartOfDay() : null)
                .le(query.getEndDate() != null, EightDReport::getCreateTime, query.getEndDate() != null ? query.getEndDate().plusDays(1).atStartOfDay() : null)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(EightDReport::getReportNo, query.getKeyword()))
                .orderByDesc(EightDReport::getCreateTime);
        Page<EightDReport> page = eightDReportMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(EightDReportConverter::toVO));
    }

    @Override
    public EightDReportVO getDetail(Long id) {
        EightDReport report = getWithScope(id);
        return EightDReportConverter.toVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(EightDReportCreateDTO dto) {
        EightDReport entity = EightDReportConverter.toEntity(dto);
        entity.setReportNo(generateReportNo());
        entity.setCurrentStep(dto.getCurrentStep() != null ? dto.getCurrentStep() : 1);
        if (dto.getDueDate() == null) {
            entity.setDueDate(LocalDate.now().plusDays(15));
        }
        eightDReportMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, EightDReportUpdateDTO dto) {
        EightDReport report = getWithScope(id);
        if (dto.getD1Team() != null) report.setD1Team(dto.getD1Team());
        if (dto.getD2Problem() != null) report.setD2Problem(dto.getD2Problem());
        if (dto.getD3Containment() != null) report.setD3Containment(dto.getD3Containment());
        if (dto.getD4RootCause() != null) report.setD4RootCause(dto.getD4RootCause());
        if (dto.getD5CorrectiveAction() != null) report.setD5CorrectiveAction(dto.getD5CorrectiveAction());
        if (dto.getD6ValidateAction() != null) report.setD6ValidateAction(dto.getD6ValidateAction());
        if (dto.getD7PreventAction() != null) report.setD7PreventAction(dto.getD7PreventAction());
        if (dto.getD8CloseSummary() != null) report.setD8CloseSummary(dto.getD8CloseSummary());
        if (dto.getDueDate() != null) report.setDueDate(dto.getDueDate());
        if (dto.getCurrentStep() != null) report.setCurrentStep(dto.getCurrentStep());
        if (dto.getStepDueDate() != null) report.setStepDueDate(dto.getStepDueDate());
        eightDReportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "提交8D报告", businessIdExpr = "#id")
    public void submit(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_DRAFT).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态的8D报告可以提交");
        }
        report.setReportStatus(STATUS_SUBMITTED);
        report.setSubmitTime(LocalDateTime.now());
        eightDReportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "审核8D报告", businessIdExpr = "#id")
    public void audit(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_SUBMITTED).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态的8D报告可以审核");
        }
        report.setReportStatus(STATUS_AUDITING);
        report.setAuditTime(LocalDateTime.now());
        eightDReportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "退回8D报告", businessIdExpr = "#id")
    public void reject(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_AUDITING).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有审核中状态的8D报告可以退回");
        }
        report.setReportStatus(STATUS_REJECTED);
        eightDReportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "关闭8D报告", businessIdExpr = "#id")
    public void close(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_AUDITING).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有审核中状态的8D报告可以关闭");
        }
        report.setReportStatus(STATUS_CLOSED);
        report.setCloseTime(LocalDateTime.now());
        eightDReportMapper.updateById(report);
    }

    // ---- 6.6: 8D 阶段节点追踪 ----

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "提交阶段", businessIdExpr = "#id")
    public void stepSubmit(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_AUDITING).equals(report.getReportStatus()) && !Integer.valueOf(STATUS_REJECTED).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有审核中或退回状态的8D报告可以提交阶段");
        }
        Integer step = dto.getCurrentStep() != null ? dto.getCurrentStep() : report.getCurrentStep();
        if (step == null) {
            step = 1;
        }
        // 更新阶段内容
        setStepContent(report, step, dto.getStepContent());
        report.setCurrentStep(step);
        if (dto.getStepDueDate() != null) {
            report.setStepDueDate(dto.getStepDueDate());
        } else {
            EightDStepEnum stepEnum = null;
            for (EightDStepEnum e : EightDStepEnum.values()) {
                if (e.getCode().equals(step)) { stepEnum = e; break; }
            }
            if (stepEnum != null) {
                report.setStepDueDate(LocalDate.now().plusDays(stepEnum.getDefaultDays()));
            }
        }
        // 退回后重新提交，状态改回审核中
        if (Integer.valueOf(STATUS_REJECTED).equals(report.getReportStatus())) {
            report.setReportStatus(STATUS_AUDITING);
        }
        // D8 提交后自动结案
        if (step == 8) {
            report.setCurrentStep(8);
            report.setReportStatus(STATUS_CLOSED);
            report.setCloseTime(LocalDateTime.now());
        }
        eightDReportMapper.updateById(report);
        // 6.8: 逾期预警检查
        checkOverdueWarning(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "8D报告", businessType = "eight_d_report", action = "审核阶段", businessIdExpr = "#id")
    public void stepApprove(Long id, EightDActionDTO dto) {
        dto = normalizeAction(dto);
        EightDReport report = getWithScope(id);
        if (!Integer.valueOf(STATUS_AUDITING).equals(report.getReportStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有审核中状态的8D报告可以审核阶段");
        }
        Integer currentStep = report.getCurrentStep();
        if (currentStep == null) currentStep = 1;
        // 推进到下一阶段
        EightDStepEnum next = EightDStepEnum.next(currentStep);
        if (next != null) {
            report.setCurrentStep(next.getCode());
            report.setStepDueDate(LocalDate.now().plusDays(next.getDefaultDays()));
        }
        eightDReportMapper.updateById(report);
    }

    private void setStepContent(EightDReport report, Integer step, String content) {
        if (content == null) return;
        switch (step) {
            case 1 -> report.setD1Team(content);
            case 2 -> report.setD2Problem(content);
            case 3 -> report.setD3Containment(content);
            case 4 -> report.setD4RootCause(content);
            case 5 -> report.setD5CorrectiveAction(content);
            case 6 -> report.setD6ValidateAction(content);
            case 7 -> report.setD7PreventAction(content);
            case 8 -> report.setD8CloseSummary(content);
        }
    }

    // ---- 6.8: 8D 逾期预警 ----
    private void checkOverdueWarning(EightDReport report) {
        if (report.getStepDueDate() != null && report.getStepDueDate().isBefore(LocalDate.now().plusDays(1))
                && report.getSupplierId() != null) {
            MessageNoticeCreateDTO msg = new MessageNoticeCreateDTO();
            msg.setReceiverSupplierId(report.getSupplierId());
            msg.setChannel(1);
            msg.setTitle("8D报告逾期预警");
            msg.setContent("8D报告 " + report.getReportNo() + " 当前阶段 "
                    + EightDStepEnum.getDesc(report.getCurrentStep())
                    + " 截止日期为 " + report.getStepDueDate()
                    + "，已逾期，请尽快处理。");
            msg.setBusinessType("eight_d_report");
            msg.setBusinessId(report.getId());
            messageNoticeService.create(msg);
        }
    }

    private String generateReportNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "8D" + datePart;
        Long count = eightDReportMapper.selectCount(
                new LambdaQueryWrapper<EightDReport>()
                        .likeRight(EightDReport::getReportNo, prefix));
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    private EightDReport getWithScope(Long id) {
        EightDReport report = eightDReportMapper.selectById(id);
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

    private EightDActionDTO normalizeAction(EightDActionDTO dto) {
        return dto == null ? new EightDActionDTO() : dto;
    }
}