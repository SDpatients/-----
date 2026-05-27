package com.supplier.integration.service;

import java.util.Map;

public interface ThirdPartyPoApiService {

    /**
     * 根据配置ID，将采购订单推送到第三方（新增采购订单）
     * @param configId 第三方接口配置ID
     * @param orderId 采购订单ID
     * @return 第三方返回结果
     */
    String pushOrderToThirdParty(Long configId, Long orderId);

    /**
     * 根据配置ID，从第三方获取最新采购订单并保存到本地
     * @param configId 第三方接口配置ID
     * @param params 额外查询参数
     * @return 同步结果描述
     */
    String fetchLatestOrderFromThirdParty(Long configId, Map<String, Object> params);
}