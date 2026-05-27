package com.supplier.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.entity.MessageTemplate;
import com.supplier.message.mapper.MessageTemplateMapper;
import com.supplier.message.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {
    private final MessageTemplateMapper mapper;

    @Override
    public PageResult<MessageTemplate> page(int pageNum, int pageSize, String keyword, Integer channel) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<MessageTemplate>()
                .eq(channel != null, MessageTemplate::getChannel, channel)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(MessageTemplate::getTemplateCode, keyword)
                        .or()
                        .like(MessageTemplate::getTemplateName, keyword)
                        .or()
                        .like(MessageTemplate::getBusinessType, keyword))
                .orderByAsc(MessageTemplate::getChannel)
                .orderByAsc(MessageTemplate::getTemplateCode);
        Page<MessageTemplate> page = mapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    @Override
    public MessageTemplate getById(Long id) {
        MessageTemplate entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MessageTemplate template) {
        if (mapper.selectCount(new LambdaQueryWrapper<MessageTemplate>().eq(MessageTemplate::getTemplateCode, template.getTemplateCode())) > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "模板编码已存在");
        }
        template.setStatus(template.getStatus() == null ? 1 : template.getStatus());
        mapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MessageTemplate template) {
        MessageTemplate exist = getById(template.getId());
        Long count = mapper.selectCount(new LambdaQueryWrapper<MessageTemplate>()
                .eq(MessageTemplate::getTemplateCode, template.getTemplateCode())
                .ne(MessageTemplate::getId, template.getId()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "模板编码已存在");
        }
        mapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id) {
        MessageTemplate entity = getById(id);
        entity.setStatus(entity.getStatus() == 1 ? 0 : 1);
        mapper.updateById(entity);
    }

    @Override
    public List<MessageTemplate> listByBusinessType(String businessType, Integer channel) {
        return mapper.selectList(new LambdaQueryWrapper<MessageTemplate>()
                .eq(StringUtils.hasText(businessType), MessageTemplate::getBusinessType, businessType)
                .eq(channel != null, MessageTemplate::getChannel, channel)
                .eq(MessageTemplate::getStatus, 1));
    }
}