package com.supplier.message.channel.impl;

import com.supplier.message.channel.WeComMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信消息发送器实现
 * 对接企业微信 API
 */
@Slf4j
@Service
public class WeComMessageSenderImpl implements WeComMessageSender {

    private final RestTemplate restTemplate;

    @Value("${wecom.corp.id:}")
    private String corpId;

    @Value("${wecom.agent.id:}")
    private String agentId;

    @Value("${wecom.agent.secret:}")
    private String agentSecret;

    private String cachedAccessToken;
    private long tokenExpireTime;

    public WeComMessageSenderImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean sendTextToUser(String userId, String content) {
        try {
            String accessToken = getAccessToken();
            String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;

            Map<String, Object> body = new HashMap<>();
            body.put("touser", userId);
            body.put("msgtype", "text");
            body.put("agentid", Integer.parseInt(agentId));

            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("[企微] 文本消息发送成功: userId={}, response={}", userId, response.getBody());
            return true;
        } catch (Exception e) {
            log.error("[企微] 文本消息发送失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMarkdownToGroup(String webhookUrl, String content) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "markdown");

            Map<String, String> markdown = new HashMap<>();
            markdown.put("content", content);
            body.put("markdown", markdown);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
            log.info("[企微群] Markdown消息发送成功: response={}", response.getBody());
            return true;
        } catch (Exception e) {
            log.error("[企微群] Markdown消息发送失败: error={}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendCardToUser(String userId, String title, String description, String url) {
        try {
            String accessToken = getAccessToken();
            String apiUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;

            Map<String, Object> body = new HashMap<>();
            body.put("touser", userId);
            body.put("msgtype", "textcard");
            body.put("agentid", Integer.parseInt(agentId));

            Map<String, String> textcard = new HashMap<>();
            textcard.put("title", title);
            textcard.put("description", description);
            textcard.put("url", url);
            body.put("textcard", textcard);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            log.info("[企微] 卡片消息发送成功: userId={}, title={}", userId, title);
            return true;
        } catch (Exception e) {
            log.error("[企微] 卡片消息发送失败: userId={}, title={}, error={}", userId, title, e.getMessage());
            return false;
        }
    }

    private synchronized String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }
        try {
            String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                    corpId, agentSecret);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    response.getBody(), Map.class);
            cachedAccessToken = String.valueOf(result.get("access_token"));
            Integer expiresIn = (Integer) result.get("expires_in");
            tokenExpireTime = System.currentTimeMillis() + (expiresIn != null ? (expiresIn - 300) * 1000L : 7200000L);
            return cachedAccessToken;
        } catch (Exception e) {
            log.error("[企微] 获取access_token失败", e);
            return "";
        }
    }
}