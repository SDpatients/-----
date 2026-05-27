package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.system.dto.SysExportTaskCreateDTO;
import com.supplier.system.entity.SysExportTask;
import com.supplier.system.mapper.SysExportTaskMapper;
import com.supplier.system.query.SysExportTaskQuery;
import com.supplier.system.service.SysExportTaskService;
import com.supplier.system.service.SysFileAttachmentService;
import com.supplier.system.vo.SysExportTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SysExportTaskServiceImpl implements SysExportTaskService {
    private final SysExportTaskMapper mapper;
    private final SysFileAttachmentService fileAttachmentService;

    @Override
    public PageResult<SysExportTaskVO> page(SysExportTaskQuery query) {
        Page<SysExportTask> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), new LambdaQueryWrapper<SysExportTask>()
                .eq(StringUtils.hasText(query.getTaskNo()), SysExportTask::getTaskNo, query.getTaskNo())
                .eq(StringUtils.hasText(query.getTaskType()), SysExportTask::getTaskType, query.getTaskType())
                .eq(query.getTaskStatus() != null, SysExportTask::getTaskStatus, query.getTaskStatus())
                .orderByDesc(SysExportTask::getCreateTime));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public SysExportTaskVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysExportTaskCreateDTO dto) {
        SysExportTask entity = new SysExportTask();
        entity.setTaskNo("EXP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        entity.setTaskType(dto.getTaskType());
        entity.setExportParams(dto.getExportParams());
        entity.setTotalCount(dto.getTotalCount());
        entity.setProcessedCount(0);
        entity.setTaskStatus(0);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markProcessing(Long id) {
        SysExportTask entity = getById(id);
        entity.setTaskStatus(1);
        entity.setStartTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long id, Long fileId) {
        SysExportTask entity = getById(id);
        entity.setTaskStatus(2);
        entity.setFileId(fileId);
        entity.setFinishTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long id, String errorMessage) {
        SysExportTask entity = getById(id);
        entity.setTaskStatus(3);
        entity.setErrorMessage(errorMessage);
        entity.setFinishTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    @Override
    public SysFileAttachmentService.FileDownload download(Long id) throws java.io.IOException {
        SysExportTask entity = getById(id);
        if (!Integer.valueOf(2).equals(entity.getTaskStatus()) || entity.getFileId() == null) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "导出任务未完成或未生成文件");
        }
        return fileAttachmentService.load(entity.getFileId());
    }

    private SysExportTask getById(Long id) {
        SysExportTask entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private SysExportTaskVO toVO(SysExportTask e) {
        SysExportTaskVO vo = new SysExportTaskVO();
        vo.setId(e.getId()); vo.setTaskNo(e.getTaskNo()); vo.setTaskType(e.getTaskType()); vo.setFileId(e.getFileId()); vo.setExportParams(e.getExportParams()); vo.setTotalCount(e.getTotalCount()); vo.setProcessedCount(e.getProcessedCount()); vo.setTaskStatus(e.getTaskStatus()); vo.setErrorMessage(e.getErrorMessage()); vo.setStartTime(e.getStartTime()); vo.setFinishTime(e.getFinishTime());
        return vo;
    }
}
