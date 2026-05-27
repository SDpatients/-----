package com.supplier.quality.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.quality.entity.EightDReport;
import com.supplier.quality.enums.EightDStepEnum;
import com.supplier.quality.enums.ReportStatusEnum;
import com.supplier.quality.mapper.EightDReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 8D 阶段逾期预警定时任务
 * 扫描 8D 报告中各阶段(d1-d8)已逾期的记录并生成通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EightDOverdueScheduler {

    private final EightDReportMapper eightDReportMapper;
    private final MessageNoticeService messageNoticeService;

    /**
     * 每日 08:30 扫描 8D 阶段逾期
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void scanEightDOverdue() {
        log.info("[8D逾期预警] 开始扫描8D阶段逾期");
        int noticeCount = 0;

        // 扫描未关闭的报告
        List<EightDReport> reports = eightDReportMapper.selectList(
                new LambdaQueryWrapper<EightDReport>()
                        .ne(EightDReport::getReportStatus, ReportStatusEnum.CLOSED.getCode())
                        .isNotNull(EightDReport::getStepDueDate)
                        .select(EightDReport::getId, EightDReport::getReportNo,
                                EightDReport::getSupplierId, EightDReport::getCurrentStep,
                                EightDReport::getStepDueDate, EightDReport::getReportStatus)
        );

        LocalDate today = LocalDate.now();
        for (EightDReport report : reports) {
            if (report.getStepDueDate() == null) {
                continue;
            }
            // 阶段截止日期已过期
            if (report.getStepDueDate().isBefore(today)) {
                String stepName = EightDStepEnum.getDesc(report.getCurrentStep());
                String statusName = ReportStatusEnum.CLOSED.getCode().equals(report.getReportStatus()) ? "已关闭" : "进行中";
                sendNotice(report.getSupplierId(),
                        "8D阶段逾期预警",
                        String.format("8D报告 %s 的 %s 阶段已逾期（截止 %s），当前状态: %s",
                                report.getReportNo(), stepName,
                                report.getStepDueDate(), statusName),
                        "8d_overdue", report.getId());
                noticeCount++;
            }
        }
        log.info("[8D逾期预警] 扫描完成，共生成 {} 条预警通知", noticeCount);
    }

    private void sendNotice(Long supplierId, String title, String content, String businessType, Long businessId) {
        try {
            if (supplierId == null) {
                log.warn("[8D逾期预警] 跳过无供应商的通知: title={}", title);
                return;
            }
            MessageNoticeCreateDTO dto = new MessageNoticeCreateDTO();
            dto.setReceiverSupplierId(supplierId);
            dto.setTitle(title);
            dto.setContent(content);
            dto.setBusinessType(businessType);
            dto.setBusinessId(businessId);
            dto.setChannel(1);
            messageNoticeService.create(dto);
        } catch (Exception e) {
            log.error("[8D逾期预警] 发送通知失败: supplierId={}, title={}", supplierId, title, e);
        }
    }
}