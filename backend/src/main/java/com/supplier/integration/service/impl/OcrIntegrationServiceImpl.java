package com.supplier.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.IntegrationEndpoint;
import com.supplier.integration.entity.IntegrationLog;
import com.supplier.integration.mapper.IntegrationEndpointMapper;
import com.supplier.integration.mapper.IntegrationLogMapper;
import com.supplier.integration.service.OcrIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * OCR 识别集成实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrIntegrationServiceImpl implements OcrIntegrationService {

    private final IntegrationEndpointMapper endpointMapper;
    private final IntegrationLogMapper integrationLogMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String recognizeInvoice(String fileUrl) {
        return doOcrRecognize("OCR_INVOICE", "invoice", fileUrl);
    }

    @Override
    public String recognizeBusinessLicense(String fileUrl) {
        return doOcrRecognize("OCR_LICENSE", "business_license", fileUrl);
    }

    private String doOcrRecognize(String endpointCode, String bizType, String fileUrl) {
        IntegrationEndpoint endpoint = getOcrEndpoint(endpointCode);
        String url = endpoint.getBaseUrl() + "/recognize";

        try {
            Map<String, String> body = new HashMap<>();
            body.put("imageUrl", fileUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if ("BEARER".equalsIgnoreCase(endpoint.getAuthType())) {
                headers.setBearerAuth(endpoint.getRemark() != null ? endpoint.getRemark() : "");
            }
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String result = response.getBody();
            log.info("[OCR] {} 识别成功", bizType);
            recordLog("OCR", endpointCode, bizType, fileUrl, true, null);
            return result;
        } catch (Exception e) {
            log.error("[OCR] {} 识别失败: {}", bizType, e.getMessage());
            recordLog("OCR", endpointCode, bizType, fileUrl, false, e.getMessage());
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "OCR识别失败: " + e.getMessage());
        }
    }

    private IntegrationEndpoint getOcrEndpoint(String code) {
        IntegrationEndpoint endpoint = endpointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IntegrationEndpoint>()
                        .eq(IntegrationEndpoint::getSystemType, "OCR")
                        .eq(IntegrationEndpoint::getEndpointCode, code)
                        .eq(IntegrationEndpoint::getStatus, 1)
        );
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "OCR集成端点未配置: " + code);
        }
        return endpoint;
    }

    private void recordLog(String systemType, String interfaceCode, String businessType,
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
            log.warn("[OCR] 记录集成日志失败", e);
        }
    }
}