package com.supplier.integration.converter;

import com.supplier.integration.dto.IntegrationEndpointCreateDTO;
import com.supplier.integration.entity.IntegrationEndpoint;
import com.supplier.integration.vo.IntegrationEndpointVO;

public class IntegrationEndpointConverter {

    public static IntegrationEndpoint toEntity(IntegrationEndpointCreateDTO dto) {
        IntegrationEndpoint entity = new IntegrationEndpoint();
        entity.setEndpointCode(dto.getEndpointCode());
        entity.setEndpointName(dto.getEndpointName());
        entity.setSystemType(dto.getSystemType());
        entity.setIntegrationMode(dto.getIntegrationMode());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setAuthType(dto.getAuthType());
        entity.setTimeoutMs(dto.getTimeoutMs());
        entity.setRetryLimit(dto.getRetryLimit());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public static IntegrationEndpointVO toVO(IntegrationEndpoint entity) {
        IntegrationEndpointVO vo = new IntegrationEndpointVO();
        vo.setId(entity.getId());
        vo.setEndpointCode(entity.getEndpointCode());
        vo.setEndpointName(entity.getEndpointName());
        vo.setSystemType(entity.getSystemType());
        vo.setIntegrationMode(entity.getIntegrationMode());
        vo.setBaseUrl(entity.getBaseUrl());
        vo.setAuthType(entity.getAuthType());
        vo.setTimeoutMs(entity.getTimeoutMs());
        vo.setRetryLimit(entity.getRetryLimit());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}