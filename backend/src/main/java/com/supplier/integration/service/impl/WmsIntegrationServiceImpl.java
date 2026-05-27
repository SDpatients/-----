package com.supplier.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.IntegrationEndpoint;
import com.supplier.integration.entity.IntegrationLog;
import com.supplier.integration.mapper.IntegrationEndpointMapper;
import com.supplier.integration.mapper.IntegrationLogMapper;
import com.supplier.integration.service.WmsIntegrationService;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WMS 仓储系统集成实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WmsIntegrationServiceImpl implements WmsIntegrationService {

    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final IntegrationEndpointMapper endpointMapper;
    private final IntegrationLogMapper integrationLogMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String pushAsnToWms(Long deliveryNoticeId) {
        DeliveryNotice notice = deliveryNoticeMapper.selectById(deliveryNoticeId);
        if (notice == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "送货通知不存在");
        }

        IntegrationEndpoint endpoint = getWmsEndpoint("ASN_PUSH");
        String url = endpoint.getBaseUrl() + "/asn/receive";

        try {
            Map<String, Object> body = buildAsnBody(notice);
            String response = executePost(endpoint, url, body);
            log.info("[WMS-ASN] ASN下发成功: noticeNo={}, 响应={}", notice.getNoticeNo(), response);
            recordIntegrationLog("WMS", "ASN_PUSH", "delivery", notice.getId().toString(), true, null);
            return response;
        } catch (Exception e) {
            log.error("[WMS-ASN] ASN下发失败: noticeNo={}", notice.getNoticeNo(), e);
            recordIntegrationLog("WMS", "ASN_PUSH", "delivery", notice.getId().toString(), false, e.getMessage());
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "WMS ASN下发失败: " + e.getMessage());
        }
    }

    @Override
    public int syncReceiptFromWms(String startTime, String endTime) {
        IntegrationEndpoint endpoint = getWmsEndpoint("RECEIPT_SYNC");
        String url = endpoint.getBaseUrl() + "/receipt/sync?startTime=" + startTime + "&endTime=" + endTime;

        try {
            String response = executeGet(endpoint, url);
            log.info("[WMS-收货同步] 同步成功: {}", response);
            recordIntegrationLog("WMS", "RECEIPT_SYNC", "receipt", startTime + "~" + endTime, true, null);
            return 1;
        } catch (Exception e) {
            log.error("[WMS-收货同步] 同步失败", e);
            recordIntegrationLog("WMS", "RECEIPT_SYNC", "receipt", startTime + "~" + endTime, false, e.getMessage());
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "WMS收货同步失败: " + e.getMessage());
        }
    }

    @Override
    public void confirmAsnReceived(String asnNo, String wmsStatus, String wmsMessage) {
        log.info("[WMS回调] ASN已接收: asnNo={}, status={}, message={}", asnNo, wmsStatus, wmsMessage);
        recordIntegrationLog("WMS", "ASN_CALLBACK", "delivery", asnNo, "SUCCESS".equals(wmsStatus), wmsMessage);
    }

    private Map<String, Object> buildAsnBody(DeliveryNotice notice) {
        Map<String, Object> body = new HashMap<>();
        body.put("asnNo", notice.getNoticeNo());
        body.put("supplierId", notice.getSupplierId());
        body.put("supplierName", notice.getSupplierName());
        body.put("planDeliveryDate", notice.getPlanDeliveryDate() != null ? notice.getPlanDeliveryDate().toString() : null);
        body.put("deliveryMethod", notice.getDeliveryMethod());
        body.put("deliveryCompany", notice.getDeliveryCompany());
        body.put("deliveryNo", notice.getDeliveryNo());
        body.put("driverName", notice.getDriverName());
        body.put("driverPhone", notice.getDriverPhone());
        body.put("vehicleNo", notice.getVehicleNo());
        body.put("deliveryAddress", notice.getDeliveryAddress());
        body.put("receiver", notice.getReceiver());
        body.put("receiverPhone", notice.getReceiverPhone());
        return body;
    }

    private String executePost(IntegrationEndpoint endpoint, String url, Map<String, Object> body) {
        HttpHeaders headers = buildHeaders(endpoint);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String executeGet(IntegrationEndpoint endpoint, String url) {
        HttpHeaders headers = buildHeaders(endpoint);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    private HttpHeaders buildHeaders(IntegrationEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if ("BEARER".equalsIgnoreCase(endpoint.getAuthType()) && endpoint.getRemark() != null) {
            headers.setBearerAuth(endpoint.getRemark());
        }
        return headers;
    }

    private IntegrationEndpoint getWmsEndpoint(String code) {
        IntegrationEndpoint endpoint = endpointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IntegrationEndpoint>()
                        .eq(IntegrationEndpoint::getSystemType, "WMS")
                        .eq(IntegrationEndpoint::getEndpointCode, code)
                        .eq(IntegrationEndpoint::getStatus, 1)
        );
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "WMS集成端点未配置: " + code);
        }
        return endpoint;
    }

    private void recordIntegrationLog(String systemType, String interfaceCode, String businessType,
                                       String businessId, boolean success, String errorMessage) {
        try {
            IntegrationLog logEntry = new IntegrationLog();
            logEntry.setTraceId(systemType + "_" + interfaceCode + "_" + System.currentTimeMillis());
            logEntry.setEndpointCode(interfaceCode);
            logEntry.setSystemType(systemType);
            logEntry.setInterfaceCode(interfaceCode);
            logEntry.setDirection(0);
            logEntry.setBusinessType(businessType);
            try {
                logEntry.setBusinessId(Long.valueOf(businessId));
            } catch (NumberFormatException ignored) {
            }
            logEntry.setResultStatus(success ? 1 : 0);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setCostMs(0);
            logEntry.setRetryCount(0);
            integrationLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("[WMS] 记录集成日志失败", e);
        }
    }
}