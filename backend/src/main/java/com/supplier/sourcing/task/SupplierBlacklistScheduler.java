package com.supplier.sourcing.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.sourcing.entity.SupplierBlacklist;
import com.supplier.sourcing.enums.SupplierBlacklistStatusEnum;
import com.supplier.sourcing.mapper.SupplierBlacklistMapper;
import com.supplier.sourcing.service.SupplierBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 黑名单到期自动解除定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierBlacklistScheduler {

    private final SupplierBlacklistMapper blacklistMapper;
    private final SupplierBlacklistService supplierBlacklistService;

    /**
     * 每天凌晨3点执行：扫描到期黑名单 → 解除黑名单 → 恢复供应商状态
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void releaseExpiredBlacklist() {
        log.info("开始扫描到期黑名单...");
        var now = LocalDateTime.now();
        var wrapper = new LambdaQueryWrapper<SupplierBlacklist>()
                .eq(SupplierBlacklist::getStatus, SupplierBlacklistStatusEnum.ACTIVE.getCode())
                .isNotNull(SupplierBlacklist::getEndTime)
                .lt(SupplierBlacklist::getEndTime, now);
        List<SupplierBlacklist> list = blacklistMapper.selectList(wrapper);
        for (SupplierBlacklist bl : list) {
            bl.setStatus(SupplierBlacklistStatusEnum.RESOLVED.getCode());
            blacklistMapper.updateById(bl);
            log.info("黑名单 {} 到期自动解除，供应商ID: {}", bl.getId(), bl.getSupplierId());

            supplierBlacklistService.restoreSupplierStatusIfNeeded(bl.getSupplierId());
            log.info("供应商 {} 状态已恢复", bl.getSupplierId());
        }
        log.info("到期黑名单扫描完成，释放数量: {}", list.size());
    }
}