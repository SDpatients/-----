package com.supplier.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.entity.IntegrationLog;
import com.supplier.integration.mapper.IntegrationLogMapper;
import com.supplier.integration.query.IntegrationLogQuery;
import com.supplier.integration.service.IntegrationLogService;
import com.supplier.integration.vo.IntegrationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IntegrationLogServiceImpl implements IntegrationLogService {

    private final IntegrationLogMapper integrationLogMapper;

    @Override
    public PageResult<IntegrationLogVO> page(IntegrationLogQuery query) {
        LambdaQueryWrapper<IntegrationLog> wrapper = new LambdaQueryWrapper<IntegrationLog>()
                .eq(StringUtils.hasText(query.getSystemType()), IntegrationLog::getSystemType, query.getSystemType())
                .eq(StringUtils.hasText(query.getEndpointCode()), IntegrationLog::getEndpointCode, query.getEndpointCode())
                .eq(StringUtils.hasText(query.getInterfaceCode()), IntegrationLog::getInterfaceCode, query.getInterfaceCode())
                .eq(query.getDirection() != null, IntegrationLog::getDirection, query.getDirection())
                .eq(query.getResultStatus() != null, IntegrationLog::getResultStatus, query.getResultStatus())
                .ge(query.getStartTime() != null, IntegrationLog::getCreateTime, query.getStartTime())
                .le(query.getEndTime() != null, IntegrationLog::getCreateTime, query.getEndTime())
                .orderByDesc(IntegrationLog::getCreateTime);
        Page<IntegrationLog> page = integrationLogMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public IntegrationLogVO getDetail(Long id) {
        IntegrationLog log = integrationLogMapper.selectById(id);
        if (log == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(log);
    }

    private IntegrationLogVO toVO(IntegrationLog entity) {
        IntegrationLogVO vo = new IntegrationLogVO();
        vo.setId(entity.getId());
        vo.setTraceId(entity.getTraceId());
        vo.setEndpointCode(entity.getEndpointCode());
        vo.setSystemType(entity.getSystemType());
        vo.setInterfaceCode(entity.getInterfaceCode());
        vo.setDirection(entity.getDirection());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setRequestSummary(entity.getRequestSummary());
        vo.setResponseSummary(entity.getResponseSummary());
        vo.setResultStatus(entity.getResultStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCostMs(entity.getCostMs());
        vo.setRetryCount(entity.getRetryCount());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}