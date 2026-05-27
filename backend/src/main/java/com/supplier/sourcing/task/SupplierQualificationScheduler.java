package com.supplier.sourcing.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.sourcing.entity.SupplierQualification;
import com.supplier.sourcing.enums.QualificationStatusEnum;
import com.supplier.sourcing.mapper.SupplierQualificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商资质有效期定时扫描任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierQualificationScheduler {

    private final SupplierQualificationMapper qualificationMapper;

    /**
     * 每天凌晨2点执行：扫描到期资质并标记失效
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireQualifications() {
        log.info("开始扫描已到期资质...");
        var now = LocalDateTime.now();
        var wrapper = new LambdaQueryWrapper<SupplierQualification>()
                .eq(SupplierQualification::getStatus, QualificationStatusEnum.VALID.getCode())
                .lt(SupplierQualification::getValidEnd, now);
        List<SupplierQualification> expiredList = qualificationMapper.selectList(wrapper);
        for (SupplierQualification qual : expiredList) {
            qual.setStatus(QualificationStatusEnum.EXPIRED.getCode());
            qualificationMapper.updateById(qual);
        }
        log.info("到期资质扫描完成，标记失效数量: {}", expiredList.size());
    }

    /**
     * 每天早上9点执行：扫描即将到期的资质（提醒天数内）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanExpiringQualifications() {
        log.info("开始扫描即将到期资质...");
        var now = LocalDateTime.now();
        // 查询所有有效资质
        var wrapper = new LambdaQueryWrapper<SupplierQualification>()
                .eq(SupplierQualification::getStatus, QualificationStatusEnum.VALID.getCode())
                .gt(SupplierQualification::getRemindDays, 0);
        List<SupplierQualification> list = qualificationMapper.selectList(wrapper);
        int count = 0;
        for (SupplierQualification qual : list) {
            var expireDate = qual.getValidEnd();
            if (expireDate != null) {
                var daysUntilExpiry = java.time.Duration.between(now, expireDate).toDays();
                if (daysUntilExpiry >= 0 && daysUntilExpiry <= qual.getRemindDays()) {
                    qual.setStatus(QualificationStatusEnum.EXPIRING.getCode());
                    qualificationMapper.updateById(qual);
                    count++;
                    // 通知供应商更新资质（可扩展：发送站内信/邮件）
                    log.info("供应商 {} 的资质 {} 将在 {} 天后过期，已标记为即将过期",
                            qual.getSupplierId(), qual.getQualName(), daysUntilExpiry);
                }
            }
        }
        log.info("即将到期资质扫描完成，标记数量: {}", count);
    }
}