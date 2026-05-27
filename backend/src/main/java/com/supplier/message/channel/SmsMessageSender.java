package com.supplier.message.channel;

/**
 * 短信消息发送器
 * 通过短信网关/API 发送短信通知
 */
public interface SmsMessageSender {

    /**
     * 发送短信
     * @param phone 手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String content);

    /**
     * 发送模板短信
     * @param phone 手机号
     * @param templateId 模板ID
     * @param params 模板参数
     * @return 是否发送成功
     */
    boolean sendTemplateSms(String phone, String templateId, java.util.Map<String, String> params);
}