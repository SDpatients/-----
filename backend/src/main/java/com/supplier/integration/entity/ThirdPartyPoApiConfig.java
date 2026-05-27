package com.supplier.integration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方采购订单接口配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("third_party_po_api_config")
public class ThirdPartyPoApiConfig extends BaseEntity {

    /** 配置名称 */
    private String configName;

    /** 接口类型: CREATE-新增采购订单, GET_LATEST-获取最新采购订单 */
    private String apiType;

    /** 第三方接口地址 */
    private String baseUrl;

    /** HTTP方法: GET, POST, PUT */
    private String httpMethod;

    /** 鉴权方式: NONE, BASIC, BEARER, API_KEY */
    private String authType;

    /** 鉴权凭证 JSON */
    private String authCredentials;

    /** 自定义请求头 JSON */
    private String requestHeaders;

    /** 请求体模板 JSON (支持占位符) */
    private String requestBodyTemplate;

    /** 超时时间(秒) */
    private Integer timeoutSeconds;

    /** 重试次数 */
    private Integer retryCount;

    /** 启用状态: 0-停用, 1-启用 */
    private Integer enabled;

    /** 备注 */
    private String remark;
}