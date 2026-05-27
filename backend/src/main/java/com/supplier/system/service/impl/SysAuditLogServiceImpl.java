package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.system.entity.SysAuditLog;
import com.supplier.system.mapper.SysAuditLogMapper;
import com.supplier.system.query.SysAuditLogQuery;
import com.supplier.system.service.SysAuditLogService;
import com.supplier.system.vo.SysAuditLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysAuditLogServiceImpl implements SysAuditLogService {
    private final SysAuditLogMapper mapper;

    @Override
    public PageResult<SysAuditLogVO> page(SysAuditLogQuery query) {
        Page<SysAuditLog> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), new LambdaQueryWrapper<SysAuditLog>()
                .eq(StringUtils.hasText(query.getTraceId()), SysAuditLog::getTraceId, query.getTraceId())
                .eq(query.getUserId() != null, SysAuditLog::getUserId, query.getUserId())
                .eq(StringUtils.hasText(query.getBusinessType()), SysAuditLog::getBusinessType, query.getBusinessType())
                .eq(StringUtils.hasText(query.getBusinessNo()), SysAuditLog::getBusinessNo, query.getBusinessNo())
                .orderByDesc(SysAuditLog::getOperateTime));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public SysAuditLogVO getDetail(Long id) {
        SysAuditLog entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(entity);
    }

    private SysAuditLogVO toVO(SysAuditLog e) {
        SysAuditLogVO vo = new SysAuditLogVO();
        vo.setId(e.getId()); vo.setTraceId(e.getTraceId()); vo.setUserId(e.getUserId()); vo.setUsername(e.getUsername()); vo.setModuleName(e.getModuleName()); vo.setBusinessType(e.getBusinessType()); vo.setBusinessId(e.getBusinessId()); vo.setBusinessNo(e.getBusinessNo()); vo.setActionName(e.getActionName()); vo.setBeforeStatus(e.getBeforeStatus()); vo.setAfterStatus(e.getAfterStatus()); vo.setRequestMethod(e.getRequestMethod()); vo.setRequestPath(e.getRequestPath()); vo.setClientIp(e.getClientIp()); vo.setResultStatus(e.getResultStatus()); vo.setErrorMessage(e.getErrorMessage()); vo.setOperateTime(e.getOperateTime());
        return vo;
    }
}
