package com.supplier.integration.service;

/**
 * 资金系统集成服务
 * 负责付款指令下发和付款状态回传
 */
public interface FundIntegrationService {

    /**
     * 向资金系统下发付款指令
     * @param paymentId 付款单ID
     * @return 资金系统返回的付款指令编号
     */
    String pushPaymentInstruction(Long paymentId);

    /**
     * 从资金系统同步付款状态
     * @param paymentNo 付款单号
     * @return 资金系统返回的付款状态 JSON
     */
    String syncPaymentStatus(String paymentNo);

    /**
     * 处理资金系统的付款状态回调
     * @param paymentNo 付款单号
     * @param fundStatus 资金系统状态
     * @param fundMessage 资金系统消息
     */
    void handlePaymentCallback(String paymentNo, String fundStatus, String fundMessage);
}