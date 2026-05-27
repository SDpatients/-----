package com.supplier.message.channel.impl;

import com.supplier.message.channel.SmsMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信消息发送器实现
 * 对接第三方短信网关API
 */
@Slf4j
@Service
public class SmsMessageSenderImpl implements SmsMessageSender {

    private final RestTemplate restTemplate;

    @Value("${sms.api.url:}")
    private String smsApiUrl;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${sms.sign.name:供应商协同平台}")
    private String signName;

    public SmsMessageSenderImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean sendSms(String phone, String content) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("phone", phone);
            body.put("content", content);
            body.put("signName", signName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", smsApiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(smsApiUrl + "/sms/send",
                    HttpMethod.POST, entity, String.class);
            log.info("[短信] 发送成功: phone={}, content={}, response={}", phone, content, response.getBody());
            return true;
        } catch (Exception e) {
            log.error("[短信] 发送失败: phone={}, content={}, error={}", phone, content, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendTemplateSms(String phone, String templateId, Map<String, String> params) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("phone", phone);
            body.put("templateId", templateId);
            body.put("params", params);
            body.put("signName", signName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", smsApiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(smsApiUrl + "/sms/send-template",
                    HttpMethod.POST, entity, String.class);
            log.info("[短信模板] 发送成功: phone={}, templateId={}, response={}", phone, templateId, response.getBody());
            return true;
        } catch (Exception e) {
            log.error("[短信模板] 发送失败: phone={}, templateId={}, error={}", phone, templateId, e.getMessage());
            return false;
        }
    }
}