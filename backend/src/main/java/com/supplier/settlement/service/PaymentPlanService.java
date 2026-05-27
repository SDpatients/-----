package com.supplier.settlement.service;

import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.entity.Reconciliation;

public interface PaymentPlanService {

    /**
     * 三单匹配通过后自动生成付款计划
     * @param recon  对账单
     * @param payment 生成的付款单（需排期）
     */
    void generateAfterThreeWayMatch(Reconciliation recon, Payment payment);
}