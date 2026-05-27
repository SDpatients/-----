package com.supplier.settlement.service.impl;

import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.mapper.PaymentMapper;
import com.supplier.settlement.service.PaymentPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPlanServiceImpl implements PaymentPlanService {

    private final PaymentMapper paymentMapper;

    /**
     * 三单匹配通过后自动生成付款计划
     * 默认排期为对账周期结束日 + 30天（次月底）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAfterThreeWayMatch(Reconciliation recon, Payment payment) {
        if (payment.getScheduleDate() == null) {
            // 默认排期：对账截止日期次月最后一天
            LocalDate defaultSchedule = recon.getEndDate() != null
                    ? recon.getEndDate().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                    : LocalDate.now().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            payment.setScheduleDate(defaultSchedule);
        }
        if (payment.getPaymentTerms() == null) {
            payment.setPaymentTerms("月结30天");
        }
        payment.setPaymentStatus(PaymentStatusEnum.SCHEDULED.getCode());
        paymentMapper.updateById(payment);
        log.info("付款计划已生成: paymentId={}, scheduleDate={}, paymentTerms={}",
                payment.getId(), payment.getScheduleDate(), payment.getPaymentTerms());
    }
}