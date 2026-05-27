package com.supplier.message.channel;

/**
 * 邮件消息发送器
 * 通过 SMTP 发送邮件通知
 */
public interface EmailMessageSender {

    /**
     * 发送简单文本邮件
     * @param to 收件人邮箱
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    boolean sendHtmlMail(String to, String subject, String htmlContent);

    /**
     * 发送带附件的邮件
     * @param to 收件人邮箱
     * @param subject 主题
     * @param content 内容
     * @param attachmentPath 附件路径
     * @return 是否发送成功
     */
    boolean sendMailWithAttachment(String to, String subject, String content, String attachmentPath);
}