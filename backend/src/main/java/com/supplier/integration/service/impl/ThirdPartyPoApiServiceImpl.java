package com.supplier.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.ThirdPartyPoApiConfig;
import com.supplier.integration.mapper.ThirdPartyPoApiConfigMapper;
import com.supplier.integration.service.ThirdPartyPoApiService;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.order.vo.PurchaseOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyPoApiServiceImpl implements ThirdPartyPoApiService {

    private final ThirdPartyPoApiConfigMapper configMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderService purchaseOrderService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pushOrderToThirdParty(Long configId, Long orderId) {
        ThirdPartyPoApiConfig config = getEnabledConfig(configId, "CREATE");
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "采购订单不存在");
        }
        // 构建请求体：优先使用配置的模板，否则使用默认的订单数据映射
        String bodyJson = buildRequestBody(config, order);
        String response = executeRequest(config, bodyJson);
        log.info("推送采购订单 {} 到第三方成功，响应: {}", order.getOrderNo(), response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String fetchLatestOrderFromThirdParty(Long configId, Map<String, Object> params) {
        ThirdPartyPoApiConfig config = getEnabledConfig(configId, "GET_LATEST");
        // 构建查询URL（支持简单参数拼接）
        String url = config.getBaseUrl();
        if (params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            if (!url.contains("?")) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.substring(0, sb.length() - 1);
        }
        String response = executeGetRequest(config, url);
        // 解析第三方返回的订单数据并保存到本地
        int savedCount = parseAndSaveOrders(response);
        log.info("从第三方获取最新采购订单成功，同步 {} 条订单", savedCount);
        return "成功同步 " + savedCount + " 条采购订单";
    }

    // ---- 内部方法 ----

    private ThirdPartyPoApiConfig getEnabledConfig(Long configId, String expectedApiType) {
        ThirdPartyPoApiConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "第三方接口配置不存在");
        }
        if (!Integer.valueOf(1).equals(config.getEnabled())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "第三方接口配置未启用");
        }
        if (!expectedApiType.equals(config.getApiType())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "接口配置类型不匹配，期望: " + expectedApiType);
        }
        return config;
    }

    private String buildRequestBody(ThirdPartyPoApiConfig config, PurchaseOrder order) {
        // 如果配置了请求体模板，使用模板并替换占位符
        if (StringUtils.hasText(config.getRequestBodyTemplate())) {
            String template = config.getRequestBodyTemplate();
            template = template.replace("${orderNo}", nvl(order.getOrderNo()))
                    .replace("${supplierId}", String.valueOf(nvl(order.getSupplierId())))
                    .replace("${orderDate}", nvl(order.getOrderDate()))
                    .replace("${deliveryDate}", nvl(order.getDeliveryDate()))
                    .replace("${currency}", nvl(order.getCurrency()))
                    .replace("${totalAmount}", nvl(order.getTotalAmount()))
                    .replace("${payAmount}", nvl(order.getPayAmount()))
                    .replace("${deliveryAddress}", nvl(order.getDeliveryAddress()))
                    .replace("${paymentTerms}", nvl(order.getPaymentTerms()))
                    .replace("${remark}", nvl(order.getRemark()));
            return template;
        }
        // 默认：将订单数据以JSON方式映射到请求体
        try {
            PurchaseOrderVO vo = new PurchaseOrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setSupplierId(order.getSupplierId());
            vo.setOrderDate(order.getOrderDate());
            vo.setDeliveryDate(order.getDeliveryDate());
            vo.setCurrency(order.getCurrency());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setPayAmount(order.getPayAmount());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setConfirmTime(order.getConfirmTime());
            vo.setBuyerId(order.getBuyerId());
            vo.setBuyerName(order.getBuyerName());
            vo.setDeliveryAddress(order.getDeliveryAddress());
            vo.setRemark(order.getRemark());
            return objectMapper.writeValueAsString(vo);
        } catch (Exception e) {
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "构建请求体失败: " + e.getMessage());
        }
    }

    private String executeRequest(ThirdPartyPoApiConfig config, String bodyJson) {
        return doExecute(config, bodyJson);
    }

    private String executeGetRequest(ThirdPartyPoApiConfig config, String url) {
        ThirdPartyPoApiConfig tempConfig = new ThirdPartyPoApiConfig();
        // 将原始配置中的 baseUrl 替换为带参数的 URL
        org.springframework.beans.BeanUtils.copyProperties(config, tempConfig);
        tempConfig.setBaseUrl(url);
        return doExecute(tempConfig, null);
    }

    private String doExecute(ThirdPartyPoApiConfig config, String bodyJson) {
        RestTemplate rt = buildRestTemplate(config.getTimeoutSeconds());
        HttpHeaders headers = buildHeaders(config);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        HttpMethod method = HttpMethod.valueOf(config.getHttpMethod().toUpperCase());

        int maxRetries = Math.max(0, config.getRetryCount() != null ? config.getRetryCount() : 0);
        Exception lastException = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                ResponseEntity<String> response = rt.exchange(config.getBaseUrl(), method, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                }
                log.warn("第三方接口返回非200状态码: {}，重试 {}/{}", response.getStatusCodeValue(), i, maxRetries);
            } catch (RestClientException e) {
                lastException = e;
                log.warn("调用第三方接口失败，重试 {}/{}: {}", i, maxRetries, e.getMessage());
            }
        }
        throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(),
                "调用第三方接口失败，已重试 " + maxRetries + " 次" + (lastException != null ? ": " + lastException.getMessage() : ""));
    }

    private RestTemplate buildRestTemplate(Integer timeoutSeconds) {
        int timeout = (timeoutSeconds != null && timeoutSeconds > 0) ? timeoutSeconds : 30;
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
        org.springframework.http.client.JdkClientHttpRequestFactory factory = new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(timeout));
        return new RestTemplate(factory);
    }

    private HttpHeaders buildHeaders(ThirdPartyPoApiConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 鉴权处理
        String authType = config.getAuthType();
        String credentials = config.getAuthCredentials();
        if (StringUtils.hasText(authType) && StringUtils.hasText(credentials)) {
            switch (authType.toUpperCase()) {
                case "BEARER":
                    headers.setBearerAuth(credentials);
                    break;
                case "BASIC":
                    String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                    headers.set("Authorization", "Basic " + basic);
                    break;
                case "API_KEY":
                    // API_KEY 格式: {"headerName":"X-Api-Key","value":"xxx"}
                    try {
                        Map<String, String> apiKeyMap = objectMapper.readValue(credentials, new TypeReference<Map<String, String>>() {});
                        String headerName = apiKeyMap.getOrDefault("headerName", "X-Api-Key");
                        String value = apiKeyMap.getOrDefault("value", "");
                        headers.set(headerName, value);
                    } catch (Exception e) {
                        headers.set("X-Api-Key", credentials);
                    }
                    break;
                default:
                    break;
            }
        }

        // 自定义请求头
        if (StringUtils.hasText(config.getRequestHeaders())) {
            try {
                Map<String, String> customHeaders = objectMapper.readValue(config.getRequestHeaders(),
                        new TypeReference<Map<String, String>>() {});
                customHeaders.forEach(headers::set);
            } catch (Exception e) {
                log.warn("解析自定义请求头失败: {}", e.getMessage());
            }
        }

        return headers;
    }

    private int parseAndSaveOrders(String responseBody) {
        try {
            // 尝试解析为订单列表
            List<Map<String, Object>> orders = objectMapper.readValue(responseBody,
                    new TypeReference<List<Map<String, Object>>>() {});
            int count = 0;
            for (Map<String, Object> item : orders) {
                try {
                    PurchaseOrderCreateDTO dto = new PurchaseOrderCreateDTO();
                    dto.setOrderNo(String.valueOf(item.getOrDefault("orderNo", "")));
                    dto.setSupplierId(toLong(item.get("supplierId")));
                    dto.setOrderDate(parseDate(item.get("orderDate")));
                    dto.setDeliveryDate(parseDate(item.get("deliveryDate")));
                    dto.setCurrency(String.valueOf(item.getOrDefault("currency", "CNY")));
                    dto.setTotalAmount(toBigDecimal(item.get("totalAmount")));
                    dto.setTaxAmount(toBigDecimal(item.get("taxAmount")));
                    dto.setDiscountAmount(toBigDecimal(item.get("discountAmount")));
                    dto.setPayAmount(toBigDecimal(item.get("payAmount")));
                    dto.setDeliveryAddress(String.valueOf(item.getOrDefault("deliveryAddress", "")));
                    dto.setPaymentTerms(String.valueOf(item.getOrDefault("paymentTerms", "")));
                    dto.setRemark(String.valueOf(item.getOrDefault("remark", "")));
                    purchaseOrderService.create(dto);
                    count++;
                } catch (Exception e) {
                    log.warn("保存第三方订单失败: {}", e.getMessage());
                }
            }
            return count;
        } catch (Exception e) {
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(),
                    "解析第三方返回订单数据失败: " + e.getMessage());
        }
    }

    // ---- 工具方法 ----

    private String nvl(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.parseLong(String.valueOf(obj)); } catch (NumberFormatException e) { return null; }
    }

    private LocalDate parseDate(Object obj) {
        if (obj == null) return null;
        String s = String.valueOf(obj);
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        try { return new BigDecimal(String.valueOf(obj)); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
}