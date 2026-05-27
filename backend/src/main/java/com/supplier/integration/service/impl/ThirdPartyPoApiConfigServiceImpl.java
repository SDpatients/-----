package com.supplier.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.dto.ThirdPartyPoApiConfigCreateDTO;
import com.supplier.integration.dto.ThirdPartyPoApiConfigUpdateDTO;
import com.supplier.integration.entity.ThirdPartyPoApiConfig;
import com.supplier.integration.mapper.ThirdPartyPoApiConfigMapper;
import com.supplier.integration.service.ThirdPartyPoApiConfigService;
import com.supplier.integration.vo.ThirdPartyPoApiConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThirdPartyPoApiConfigServiceImpl implements ThirdPartyPoApiConfigService {

    private final ThirdPartyPoApiConfigMapper mapper;

    @Override
    public List<ThirdPartyPoApiConfigVO> list(String apiType) {
        LambdaQueryWrapper<ThirdPartyPoApiConfig> wrapper = new LambdaQueryWrapper<ThirdPartyPoApiConfig>()
                .eq(StringUtils.hasText(apiType), ThirdPartyPoApiConfig::getApiType, apiType)
                .orderByDesc(ThirdPartyPoApiConfig::getCreateTime);
        return mapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ThirdPartyPoApiConfigVO getDetail(Long id) {
        ThirdPartyPoApiConfig config = mapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ThirdPartyPoApiConfigCreateDTO dto) {
        ThirdPartyPoApiConfig config = new ThirdPartyPoApiConfig();
        BeanUtils.copyProperties(dto, config);
        config.setEnabled(0);
        mapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ThirdPartyPoApiConfigUpdateDTO dto) {
        ThirdPartyPoApiConfig config = mapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (StringUtils.hasText(dto.getConfigName())) {
            config.setConfigName(dto.getConfigName());
        }
        if (StringUtils.hasText(dto.getApiType())) {
            config.setApiType(dto.getApiType());
        }
        if (dto.getBaseUrl() != null) {
            config.setBaseUrl(dto.getBaseUrl());
        }
        if (StringUtils.hasText(dto.getHttpMethod())) {
            config.setHttpMethod(dto.getHttpMethod());
        }
        if (dto.getAuthType() != null) {
            config.setAuthType(dto.getAuthType());
        }
        if (dto.getAuthCredentials() != null) {
            config.setAuthCredentials(dto.getAuthCredentials());
        }
        if (dto.getRequestHeaders() != null) {
            config.setRequestHeaders(dto.getRequestHeaders());
        }
        if (dto.getRequestBodyTemplate() != null) {
            config.setRequestBodyTemplate(dto.getRequestBodyTemplate());
        }
        if (dto.getTimeoutSeconds() != null) {
            config.setTimeoutSeconds(dto.getTimeoutSeconds());
        }
        if (dto.getRetryCount() != null) {
            config.setRetryCount(dto.getRetryCount());
        }
        if (dto.getRemark() != null) {
            config.setRemark(dto.getRemark());
        }
        mapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ThirdPartyPoApiConfig config = mapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        mapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        ThirdPartyPoApiConfig config = mapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        config.setEnabled(1);
        mapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        ThirdPartyPoApiConfig config = mapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        config.setEnabled(0);
        mapper.updateById(config);
    }

    private ThirdPartyPoApiConfigVO toVO(ThirdPartyPoApiConfig config) {
        ThirdPartyPoApiConfigVO vo = new ThirdPartyPoApiConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}