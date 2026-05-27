package com.supplier.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.result.PageResult;
import com.supplier.message.entity.MessageTemplate;

import java.util.List;

public interface MessageTemplateService {
    PageResult<MessageTemplate> page(int pageNum, int pageSize, String keyword, Integer channel);
    MessageTemplate getById(Long id);
    Long create(MessageTemplate template);
    void update(MessageTemplate template);
    void delete(Long id);
    void toggleStatus(Long id);
    List<MessageTemplate> listByBusinessType(String businessType, Integer channel);
}