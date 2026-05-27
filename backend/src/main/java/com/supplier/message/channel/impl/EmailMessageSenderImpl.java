package com.supplier.message.channel.impl;

import com.supplier.message.channel.EmailMessageSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@RequiredArgsConstructor
public class EmailMessageSenderImpl implements EmailMessageSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public boolean sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("[邮件] 发送成功: to={}, subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("[邮件] 发送失败: to={}, subject={}, error={}", to, subject, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("[邮件HTML] 发送成功: to={}, subject={}", to, subject);
            return true;
        } catch (MessagingException e) {
            log.error("[邮件HTML] 发送失败: to={}, subject={}, error={}", to, subject, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMailWithAttachment(String to, String subject, String content, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            mailSender.send(message);
            log.info("[邮件附件] 发送成功: to={}, subject={}", to, subject);
            return true;
        } catch (MessagingException e) {
            log.error("[邮件附件] 发送失败: to={}, subject={}, error={}", to, subject, e.getMessage());
            return false;
        }
    }
}