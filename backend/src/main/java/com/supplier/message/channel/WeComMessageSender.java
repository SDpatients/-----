package com.supplier.message.channel;

/**
 * 企业微信消息发送器
 * 通过企业微信应用/群机器人发送消息
 */
public interface WeComMessageSender {

    /**
     * 发送文本消息到企业微信用户
     * @param userId 企业微信用户ID
     * @param content 文本内容
     * @return 是否发送成功
     */
    boolean sendTextToUser(String userId, String content);

    /**
     * 发送Markdown消息到企业微信群机器人
     * @param webhookUrl 群机器人webhook地址
     * @param content Markdown内容
     * @return 是否发送成功
     */
    boolean sendMarkdownToGroup(String webhookUrl, String content);

    /**
     * 发送卡片消息到企业微信用户
     * @param userId 企业微信用户ID
     * @param title 卡片标题
     * @param description 卡片描述
     * @param url 跳转链接
     * @return 是否发送成功
     */
    boolean sendCardToUser(String userId, String title, String description, String url);
}