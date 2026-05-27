package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.system.dto.SysFileAttachmentCreateDTO;
import com.supplier.system.entity.SysFileAttachment;
import com.supplier.system.mapper.SysFileAttachmentMapper;
import com.supplier.system.query.SysFileAttachmentQuery;
import com.supplier.system.service.SysFileAttachmentService;
import com.supplier.system.service.FileStorageService;
import com.supplier.system.vo.SysFileAttachmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SysFileAttachmentServiceImpl implements SysFileAttachmentService {
    private final SysFileAttachmentMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    public PageResult<SysFileAttachmentVO> page(SysFileAttachmentQuery query) {
        Page<SysFileAttachment> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), new LambdaQueryWrapper<SysFileAttachment>()
                .eq(StringUtils.hasText(query.getBusinessType()), SysFileAttachment::getBusinessType, query.getBusinessType())
                .eq(query.getBusinessId() != null, SysFileAttachment::getBusinessId, query.getBusinessId())
                .eq(StringUtils.hasText(query.getBusinessNo()), SysFileAttachment::getBusinessNo, query.getBusinessNo())
                .like(StringUtils.hasText(query.getFileName()), SysFileAttachment::getFileName, query.getFileName())
                .orderByDesc(SysFileAttachment::getUploadTime));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public SysFileAttachmentVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysFileAttachmentCreateDTO dto) {
        SysFileAttachment entity = new SysFileAttachment();
        entity.setBusinessType(dto.getBusinessType());
        entity.setBusinessId(dto.getBusinessId());
        entity.setBusinessNo(dto.getBusinessNo());
        entity.setFileName(dto.getFileName());
        entity.setFileExt(dto.getFileExt());
        entity.setFileSize(dto.getFileSize());
        entity.setContentType(dto.getContentType());
        entity.setBucketName(dto.getBucketName());
        entity.setObjectKey(dto.getObjectKey());
        entity.setFileHash(dto.getFileHash());
        entity.setUploadUserId(SecurityUtils.getUserId());
        entity.setUploadTime(LocalDateTime.now());
        entity.setStatus(1);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(MultipartFile file, String businessType, Long businessId, String businessNo) throws IOException {
        SysFileAttachment attachment = fileStorageService.store(file, businessType, businessId, businessNo);
        mapper.insert(attachment);
        return attachment.getId();
    }

    @Override
    public FileDownload load(Long id) throws IOException {
        SysFileAttachment attachment = getById(id);
        return new FileDownload(attachment.getFileName(), attachment.getContentType(), fileStorageService.load(attachment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        SysFileAttachment entity = getById(id);
        entity.setStatus(0);
        mapper.updateById(entity);
    }

    private SysFileAttachment getById(Long id) {
        SysFileAttachment entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private SysFileAttachmentVO toVO(SysFileAttachment e) {
        SysFileAttachmentVO vo = new SysFileAttachmentVO();
        vo.setId(e.getId()); vo.setBusinessType(e.getBusinessType()); vo.setBusinessId(e.getBusinessId()); vo.setBusinessNo(e.getBusinessNo()); vo.setFileName(e.getFileName()); vo.setFileExt(e.getFileExt()); vo.setFileSize(e.getFileSize()); vo.setContentType(e.getContentType()); vo.setBucketName(e.getBucketName()); vo.setObjectKey(e.getObjectKey()); vo.setFileHash(e.getFileHash()); vo.setUploadUserId(e.getUploadUserId()); vo.setUploadTime(e.getUploadTime()); vo.setStatus(e.getStatus());
        return vo;
    }
}
