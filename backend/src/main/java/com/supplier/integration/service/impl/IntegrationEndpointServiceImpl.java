package com.supplier.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.converter.IntegrationEndpointConverter;
import com.supplier.integration.dto.IntegrationEndpointCreateDTO;
import com.supplier.integration.dto.IntegrationEndpointUpdateDTO;
import com.supplier.integration.entity.IntegrationEndpoint;
import com.supplier.integration.mapper.IntegrationEndpointMapper;
import com.supplier.integration.query.IntegrationEndpointQuery;
import com.supplier.integration.service.IntegrationEndpointService;
import com.supplier.integration.vo.IntegrationEndpointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class IntegrationEndpointServiceImpl implements IntegrationEndpointService {

    private final IntegrationEndpointMapper integrationEndpointMapper;

    @Override
    public PageResult<IntegrationEndpointVO> page(IntegrationEndpointQuery query) {
        LambdaQueryWrapper<IntegrationEndpoint> wrapper = new LambdaQueryWrapper<IntegrationEndpoint>()
                .eq(StringUtils.hasText(query.getSystemType()), IntegrationEndpoint::getSystemType, query.getSystemType())
                .eq(StringUtils.hasText(query.getIntegrationMode()), IntegrationEndpoint::getIntegrationMode, query.getIntegrationMode())
                .eq(query.getStatus() != null, IntegrationEndpoint::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(IntegrationEndpoint::getEndpointCode, query.getKeyword())
                        .or()
                        .like(IntegrationEndpoint::getEndpointName, query.getKeyword()))
                .orderByDesc(IntegrationEndpoint::getCreateTime);
        Page<IntegrationEndpoint> page = integrationEndpointMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(IntegrationEndpointConverter::toVO));
    }

    @Override
    public IntegrationEndpointVO getDetail(Long id) {
        IntegrationEndpoint endpoint = integrationEndpointMapper.selectById(id);
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return IntegrationEndpointConverter.toVO(endpoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(IntegrationEndpointCreateDTO dto) {
        Long count = integrationEndpointMapper.selectCount(new LambdaQueryWrapper<IntegrationEndpoint>()
                .eq(IntegrationEndpoint::getEndpointCode, dto.getEndpointCode()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "端点编码已存在");
        }
        IntegrationEndpoint endpoint = IntegrationEndpointConverter.toEntity(dto);
        endpoint.setStatus(1);
        integrationEndpointMapper.insert(endpoint);
        return endpoint.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, IntegrationEndpointUpdateDTO dto) {
        IntegrationEndpoint endpoint = integrationEndpointMapper.selectById(id);
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (StringUtils.hasText(dto.getEndpointName())) {
            endpoint.setEndpointName(dto.getEndpointName());
        }
        if (StringUtils.hasText(dto.getSystemType())) {
            endpoint.setSystemType(dto.getSystemType());
        }
        if (StringUtils.hasText(dto.getIntegrationMode())) {
            endpoint.setIntegrationMode(dto.getIntegrationMode());
        }
        if (dto.getBaseUrl() != null) {
            endpoint.setBaseUrl(dto.getBaseUrl());
        }
        if (StringUtils.hasText(dto.getAuthType())) {
            endpoint.setAuthType(dto.getAuthType());
        }
        if (dto.getTimeoutMs() != null) {
            endpoint.setTimeoutMs(dto.getTimeoutMs());
        }
        if (dto.getRetryLimit() != null) {
            endpoint.setRetryLimit(dto.getRetryLimit());
        }
        if (dto.getRemark() != null) {
            endpoint.setRemark(dto.getRemark());
        }
        integrationEndpointMapper.updateById(endpoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        IntegrationEndpoint endpoint = integrationEndpointMapper.selectById(id);
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        integrationEndpointMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        IntegrationEndpoint endpoint = integrationEndpointMapper.selectById(id);
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        endpoint.setStatus(1);
        integrationEndpointMapper.updateById(endpoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        IntegrationEndpoint endpoint = integrationEndpointMapper.selectById(id);
        if (endpoint == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        endpoint.setStatus(0);
        integrationEndpointMapper.updateById(endpoint);
    }
}