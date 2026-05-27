package com.supplier.sourcing.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.RfqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价截止自动处理定时任务
 * 扫描 quoteDeadline 已过期的询价单，自动将状态改为"已截止"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteDeadlineSchedulerService {

    private final RfqMapper rfqMapper;

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredRfqs() {
        List<Rfq> rfqs = rfqMapper.selectList(
                new LambdaQueryWrapper<Rfq>()
                        .eq(Rfq::getRfqStatus, RfqStatusEnum.QUOTING.getCode())
                        .lt(Rfq::getQuoteDeadline, LocalDateTime.now())
        );
        if (rfqs.isEmpty()) {
            return;
        }
        for (Rfq rfq : rfqs) {
            rfq.setRfqStatus(RfqStatusEnum.CLOSED.getCode());
            rfq.setCloseTime(LocalDateTime.now());
            rfqMapper.updateById(rfq);
            log.info("[报价截止] 询价单 {} 报价已截止", rfq.getRfqNo());
        }
        log.info("[报价截止] 本次共处理 {} 条过期询价单", rfqs.size());
    }
}