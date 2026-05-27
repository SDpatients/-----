package com.supplier.integration.task;

import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.IntegrationLog;
import com.supplier.integration.mapper.IntegrationLogMapper;
import com.supplier.integration.service.ThirdPartyPoApiService;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderErpMapping;
import com.supplier.order.mapper.PurchaseOrderErpMappingMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ERP 订单同步定时任务
 * 定时从 ERP 系统拉取最新采购订单并同步到本地
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErpOrderSyncScheduler {

    private final ThirdPartyPoApiService thirdPartyPoApiService;
    private final PurchaseOrderErpMappingMapper erpMappingMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final IntegrationLogMapper integrationLogMapper;

    /**
     * 每30分钟执行一次 ERP 订单同步
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void syncErpOrders() {
        log.info("[ERP订单同步] 开始执行定时同步");
        // 默认使用 configId=1 的 GET_LATEST 配置拉取订单
        // 实际使用时可从配置表动态获取启用的 ERP 同步配置ID
        syncFromConfig(1L);
    }

    private void syncFromConfig(Long configId) {
        long startMs = System.currentTimeMillis();
        String result = null;
        Integer resultStatus = 1;
        String errorMessage = null;

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("pageSize", 100);
            result = thirdPartyPoApiService.fetchLatestOrderFromThirdParty(configId, params);
            log.info("[ERP订单同步] 同步成功: {}", result);
        } catch (Exception e) {
            resultStatus = 0;
            errorMessage = e.getMessage();
            log.error("[ERP订单同步] 同步失败: {}", e.getMessage(), e);
        }

        // 记录同步日志
        try {
            IntegrationLog syncLog = new IntegrationLog();
            syncLog.setTraceId("erp_sync_" + System.currentTimeMillis());
            syncLog.setEndpointCode("ERP_SYNC");
            syncLog.setSystemType("ERP");
            syncLog.setInterfaceCode("FETCH_PURCHASE_ORDER");
            syncLog.setDirection(0);
            syncLog.setBusinessType("purchase_order");
            syncLog.setResponseSummary(result);
            syncLog.setResultStatus(resultStatus);
            syncLog.setErrorMessage(errorMessage);
            syncLog.setCostMs((int) (System.currentTimeMillis() - startMs));
            syncLog.setRetryCount(0);
            integrationLogMapper.insert(syncLog);
        } catch (Exception e) {
            log.error("[ERP订单同步] 记录同步日志失败", e);
        }
    }

    /**
     * 记录平台订单与ERP订单号的映射关系
     */
    public void saveErpMapping(Long orderId, String orderNo, String erpOrderNo, String erpSystemType) {
        PurchaseOrderErpMapping mapping = new PurchaseOrderErpMapping();
        mapping.setOrderId(orderId);
        mapping.setOrderNo(orderNo);
        mapping.setErpOrderNo(erpOrderNo);
        mapping.setErpSystemType(erpSystemType);
        mapping.setSyncTime(LocalDateTime.now());
        erpMappingMapper.insert(mapping);
    }
}