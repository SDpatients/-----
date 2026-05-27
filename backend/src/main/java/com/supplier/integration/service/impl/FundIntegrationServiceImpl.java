package com.supplier.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.IntegrationEndpoint;
import com.supplier.integration.entity.IntegrationLog;
import com.supplier.integration.mapper.IntegrationEndpointMapper;
import com.supplier.integration.mapper.IntegrationLogMapper;
import com.supplier.integration.service.FundIntegrationService;
import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 资金系统集成实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundIntegrationServiceImpl implements FundIntegrationService {

    private final PaymentMapper paymentMapper;
    private final IntegrationEndpointMapper endpointMapper;
    private final IntegrationLogMapper integrationLogMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pushPaymentInstruction(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "付款单不存在");
        }
        if (!Integer.valueOf(PaymentStatusEnum.APPROVED.getCode()).equals(payment.getPaymentStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已审批的付款单才能推送付款指令");
        }

        IntegrationEndpoint endpoint = getFundEndpoint("PAYMENT_PUSH");
        String url = endpoint.getBaseUrl() + "/payment/instruction";

        try {
            Map<String, Object> body = buildPaymentBody(payment);
            String response = executePost(endpoint, url, body);
            log.info("[资金系统] 付款指令下发成功: paymentNo={}, 响应={}", payment.getPaymentNo(), response);

            // 更新付款状态为已排期
            payment.setPaymentStatus(PaymentStatusEnum.SCHEDULED.getCode());
            payment.setPaymentTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            recordLog("FUND", "PAYMENT_PUSH", "payment", true, null);
            return response;
        } catch (Exception e) {
            log.error("[资金系统] 付款指令下发失败: paymentNo={}", payment.getPaymentNo(), e);
            recordLog("FUND", "PAYMENT_PUSH", "payment", false, e.getMessage());
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "资金系统付款指令下发失败: " + e.getMessage());
        }
    }

    @Override
    public String syncPaymentStatus(String paymentNo) {
        IntegrationEndpoint endpoint = getFundEndpoint("PAYMENT_STATUS");
        String url = endpoint.getBaseUrl() + "/payment/status?paymentNo=" + paymentNo;

        try {
            String response = executeGet(endpoint, url);
            log.info("[资金系统] 付款状态同步成功: paymentNo={}, 响应={}", paymentNo, response);

            // 解析并更新本地付款状态
            updatePaymentStatusFromFund(paymentNo, response);

            recordLog("FUND", "PAYMENT_STATUS", "payment", true, null);
            return response;
        } catch (Exception e) {
            log.error("[资金系统] 付款状态同步失败: paymentNo={}", paymentNo, e);
            recordLog("FUND", "PAYMENT_STATUS", "payment", false, e.getMessage());
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "资金系统付款状态同步失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(String paymentNo, String fundStatus, String fundMessage) {
        log.info("[资金系统回调] paymentNo={}, status={}, message={}", paymentNo, fundStatus, fundMessage);

        try {
            updatePaymentStatusFromCallback(paymentNo, fundStatus);
            recordLog("FUND", "PAYMENT_CALLBACK", "payment",
                    "SUCCESS".equalsIgnoreCase(fundStatus), fundMessage);
        } catch (Exception e) {
            log.error("[资金系统回调] 处理失败: paymentNo={}", paymentNo, e);
            throw BusinessException.of(ResultCode.INTERNAL_ERROR.getCode(), "资金系统回调处理失败: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPaymentBody(Payment payment) {
        Map<String, Object> body = new HashMap<>();
        body.put("paymentNo", payment.getPaymentNo());
        body.put("supplierId", payment.getSupplierId());
        body.put("supplierName", payment.getSupplierName());
        body.put("paymentAmount", payment.getPaymentAmount());
        body.put("paymentMethod", payment.getPaymentMethod());
        body.put("paymentAccount", payment.getPaymentAccount());
        body.put("paymentBank", payment.getPaymentBank());
        body.put("receiveAccount", payment.getReceiveAccount());
        body.put("receiveBank", payment.getReceiveBank());
        body.put("scheduleDate", payment.getScheduleDate() != null ? payment.getScheduleDate().toString() : null);
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

    private IntegrationEndpoint getFundEndpoint(String code) {
        IntegrationEndpoint endpoint = endpointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IntegrationEndpoint>()
                        .eq(IntegrationEndpoint::getSystemType, "FUND")
                        .eq(IntegrationEndpoint::getEndpointCode, code)
                        .eq(IntegrationEndpoint::getStatus, 1)
        );
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "资金系统集成端点未配置: " + code);
        }
        return endpoint;
    }

    private void updatePaymentStatusFromFund(String paymentNo, String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            String status = String.valueOf(result.getOrDefault("status", ""));
            if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                Payment payment = paymentMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payment>()
                                .eq(Payment::getPaymentNo, paymentNo)
                );
                if (payment != null) {
                    payment.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
                    paymentMapper.updateById(payment);
                }
            }
        } catch (Exception e) {
            log.warn("[资金系统] 解析付款状态失败: {}", e.getMessage());
        }
    }

    private void updatePaymentStatusFromCallback(String paymentNo, String fundStatus) {
        Payment payment = paymentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentNo, paymentNo)
        );
        if (payment == null) {
            log.warn("[资金系统回调] 未找到付款单: paymentNo={}", paymentNo);
            return;
        }
        if ("PAID".equalsIgnoreCase(fundStatus) || "SUCCESS".equalsIgnoreCase(fundStatus)) {
            payment.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
            payment.setPaymentTime(LocalDateTime.now());
        } else if ("FAIL".equalsIgnoreCase(fundStatus)) {
            payment.setPaymentStatus(PaymentStatusEnum.REJECTED.getCode());
        }
        paymentMapper.updateById(payment);
    }

    private void recordLog(String systemType, String interfaceCode, String businessType,
                            boolean success, String errorMessage) {
        try {
            IntegrationLog logEntry = new IntegrationLog();
            logEntry.setTraceId(systemType + "_" + interfaceCode + "_" + System.currentTimeMillis());
            logEntry.setEndpointCode(interfaceCode);
            logEntry.setSystemType(systemType);
            logEntry.setInterfaceCode(interfaceCode);
            logEntry.setDirection(0);
            logEntry.setBusinessType(businessType);
            logEntry.setResultStatus(success ? 1 : 0);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setCostMs(0);
            logEntry.setRetryCount(0);
            integrationLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("[资金系统] 记录集成日志失败", e);
        }
    }
}