package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.dto.DeliveryTemplateCreateDTO;
import com.supplier.logistics.entity.DeliveryTemplate;
import com.supplier.logistics.mapper.DeliveryTemplateMapper;
import com.supplier.logistics.query.DeliveryTemplateQuery;
import com.supplier.logistics.service.DeliveryTemplateService;
import com.supplier.logistics.vo.DeliveryTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DeliveryTemplateServiceImpl implements DeliveryTemplateService {

    private final DeliveryTemplateMapper templateMapper;

    @Override
    public PageResult<DeliveryTemplateVO> page(DeliveryTemplateQuery query) {
        LambdaQueryWrapper<DeliveryTemplate> wrapper = new LambdaQueryWrapper<DeliveryTemplate>()
                .and(StringUtils.hasText(query.getKeyword()), w ->
                        w.like(DeliveryTemplate::getTemplateName, query.getKeyword())
                                .or().like(DeliveryTemplate::getTemplateCode, query.getKeyword()))
                .orderByDesc(DeliveryTemplate::getCreateTime);
        Page<DeliveryTemplate> page = templateMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public DeliveryTemplateVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeliveryTemplateCreateDTO dto) {
        DeliveryTemplate entity = new DeliveryTemplate();
        entity.setTemplateName(dto.getTemplateName());
        entity.setTemplateCode(dto.getTemplateCode());
        entity.setDescription(dto.getDescription());
        entity.setHeaderConfig(dto.getHeaderConfig());
        entity.setFooterConfig(dto.getFooterConfig());
        entity.setColumnConfig(dto.getColumnConfig());
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        entity.setRemark(dto.getRemark());
        templateMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DeliveryTemplateCreateDTO dto) {
        DeliveryTemplate entity = getById(id);
        if (StringUtils.hasText(dto.getTemplateName())) entity.setTemplateName(dto.getTemplateName());
        if (StringUtils.hasText(dto.getTemplateCode())) entity.setTemplateCode(dto.getTemplateCode());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getHeaderConfig() != null) entity.setHeaderConfig(dto.getHeaderConfig());
        if (dto.getFooterConfig() != null) entity.setFooterConfig(dto.getFooterConfig());
        if (dto.getColumnConfig() != null) entity.setColumnConfig(dto.getColumnConfig());
        if (dto.getIsDefault() != null) entity.setIsDefault(dto.getIsDefault());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        templateMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        templateMapper.deleteById(id);
    }

    private DeliveryTemplate getById(Long id) {
        DeliveryTemplate entity = templateMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private DeliveryTemplateVO toVO(DeliveryTemplate e) {
        DeliveryTemplateVO vo = new DeliveryTemplateVO();
        vo.setId(e.getId()); vo.setTemplateName(e.getTemplateName()); vo.setTemplateCode(e.getTemplateCode());
        vo.setDescription(e.getDescription()); vo.setHeaderConfig(e.getHeaderConfig());
        vo.setFooterConfig(e.getFooterConfig()); vo.setColumnConfig(e.getColumnConfig());
        vo.setIsDefault(e.getIsDefault()); vo.setRemark(e.getRemark());
        return vo;
    }
}