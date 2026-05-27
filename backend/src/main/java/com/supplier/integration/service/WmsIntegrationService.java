package com.supplier.integration.service;

/**
 * WMS 仓储系统集成服务
 * 负责 ASN（预到货通知）下发和收货结果同步
 */
public interface WmsIntegrationService {

    /**
     * 向 WMS 下发 ASN（预到货通知）
     * @param deliveryNoticeId 送货通知ID
     * @return WMS 返回结果
     */
    String pushAsnToWms(Long deliveryNoticeId);

    /**
     * 从 WMS 同步收货结果
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 同步的记录数
     */
    int syncReceiptFromWms(String startTime, String endTime);

    /**
     * 确认 ASN 已接收（WMS回调）
     * @param asnNo ASN编号
     * @param wmsStatus WMS状态
     * @param wmsMessage WMS消息
     */
    void confirmAsnReceived(String asnNo, String wmsStatus, String wmsMessage);
}