package com.supplier.system.service;

import com.supplier.common.result.PageResult;
import com.supplier.system.dto.SysFileAttachmentCreateDTO;
import com.supplier.system.query.SysFileAttachmentQuery;
import com.supplier.system.vo.SysFileAttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SysFileAttachmentService {
    PageResult<SysFileAttachmentVO> page(SysFileAttachmentQuery query);
    SysFileAttachmentVO getDetail(Long id);
    Long create(SysFileAttachmentCreateDTO dto);
    Long upload(MultipartFile file, String businessType, Long businessId, String businessNo) throws IOException;
    FileDownload load(Long id) throws IOException;
    void disable(Long id);
    void rename(Long id, String fileName);

    record FileDownload(String fileName, String contentType, byte[] content) {}
}
