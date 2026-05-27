package com.supplier.system.service;

import com.supplier.common.result.PageResult;
import com.supplier.system.query.SysAuditLogQuery;
import com.supplier.system.vo.SysAuditLogVO;

public interface SysAuditLogService {
    PageResult<SysAuditLogVO> page(SysAuditLogQuery query);
    SysAuditLogVO getDetail(Long id);
}
