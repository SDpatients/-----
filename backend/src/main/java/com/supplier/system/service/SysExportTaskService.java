package com.supplier.system.service;

import com.supplier.common.result.PageResult;
import com.supplier.system.dto.SysExportTaskCreateDTO;
import com.supplier.system.query.SysExportTaskQuery;
import com.supplier.system.vo.SysExportTaskVO;

public interface SysExportTaskService {
    PageResult<SysExportTaskVO> page(SysExportTaskQuery query);
    SysExportTaskVO getDetail(Long id);
    Long create(SysExportTaskCreateDTO dto);
    void markProcessing(Long id);
    void markSuccess(Long id, Long fileId);
    void markFailed(Long id, String errorMessage);
    SysFileAttachmentService.FileDownload download(Long id) throws java.io.IOException;
}
