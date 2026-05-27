package com.supplier.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.integration.converter.IntegrationSyncTaskConverter;
import com.supplier.integration.dto.SyncTaskRetryDTO;
import com.supplier.integration.entity.IntegrationSyncTask;
import com.supplier.integration.mapper.IntegrationSyncTaskMapper;
import com.supplier.integration.query.IntegrationSyncTaskQuery;
import com.supplier.integration.service.IntegrationSyncTaskService;
import com.supplier.integration.vo.IntegrationSyncTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class IntegrationSyncTaskServiceImpl implements IntegrationSyncTaskService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_FAILED = 3;

    private final IntegrationSyncTaskMapper integrationSyncTaskMapper;

    @Override
    public PageResult<IntegrationSyncTaskVO> page(IntegrationSyncTaskQuery query) {
        LambdaQueryWrapper<IntegrationSyncTask> wrapper = new LambdaQueryWrapper<IntegrationSyncTask>()
                .eq(StringUtils.hasText(query.getSystemType()), IntegrationSyncTask::getSystemType, query.getSystemType())
                .eq(StringUtils.hasText(query.getTaskType()), IntegrationSyncTask::getTaskType, query.getTaskType())
                .eq(query.getTaskStatus() != null, IntegrationSyncTask::getTaskStatus, query.getTaskStatus())
                .orderByDesc(IntegrationSyncTask::getCreateTime);
        Page<IntegrationSyncTask> page = integrationSyncTaskMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(IntegrationSyncTaskConverter::toVO));
    }

    @Override
    public IntegrationSyncTaskVO getDetail(Long id) {
        IntegrationSyncTask task = integrationSyncTaskMapper.selectById(id);
        if (task == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return IntegrationSyncTaskConverter.toVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long id, SyncTaskRetryDTO dto) {
        dto = normalizeRetry(dto);
        IntegrationSyncTask task = integrationSyncTaskMapper.selectById(id);
        if (task == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!Integer.valueOf(STATUS_FAILED).equals(task.getTaskStatus()) && !Integer.valueOf(STATUS_PENDING).equals(task.getTaskStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待处理或失败的任务可以重试");
        }
        task.setTaskStatus(STATUS_PENDING);
        task.setRetryCount(task.getRetryCount() != null ? task.getRetryCount() + 1 : 1);
        task.setErrorMessage(null);
        integrationSyncTaskMapper.updateById(task);
    }

    private SyncTaskRetryDTO normalizeRetry(SyncTaskRetryDTO dto) {
        return dto == null ? new SyncTaskRetryDTO() : dto;
    }
}