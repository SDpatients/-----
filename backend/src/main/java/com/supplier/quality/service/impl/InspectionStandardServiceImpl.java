package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.quality.converter.InspectionStandardConverter;
import com.supplier.quality.dto.InspectionStandardCreateDTO;
import com.supplier.quality.dto.InspectionStandardItemDTO;
import com.supplier.quality.dto.InspectionStandardUpdateDTO;
import com.supplier.quality.entity.InspectionStandard;
import com.supplier.quality.entity.InspectionStandardItem;
import com.supplier.quality.mapper.InspectionStandardItemMapper;
import com.supplier.quality.mapper.InspectionStandardMapper;
import com.supplier.quality.query.InspectionStandardQuery;
import com.supplier.quality.service.InspectionStandardService;
import com.supplier.quality.vo.InspectionStandardItemVO;
import com.supplier.quality.vo.InspectionStandardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionStandardServiceImpl implements InspectionStandardService {

    private final InspectionStandardMapper inspectionStandardMapper;
    private final InspectionStandardItemMapper itemMapper;

    @Override
    public PageResult<InspectionStandardVO> page(InspectionStandardQuery query) {
        LambdaQueryWrapper<InspectionStandard> wrapper = new LambdaQueryWrapper<InspectionStandard>()
                .eq(StringUtils.hasText(query.getMaterialCode()), InspectionStandard::getMaterialCode, query.getMaterialCode())
                .eq(query.getInspectionStrategy() != null, InspectionStandard::getInspectionStrategy, query.getInspectionStrategy())
                .and(StringUtils.hasText(query.getKeyword()), w ->
                        w.like(InspectionStandard::getMaterialCode, query.getKeyword())
                                .or().like(InspectionStandard::getMaterialName, query.getKeyword())
                                .or().like(InspectionStandard::getStandardName, query.getKeyword()))
                .orderByDesc(InspectionStandard::getCreateTime);
        Page<InspectionStandard> page = inspectionStandardMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public InspectionStandardVO getDetail(Long id) {
        InspectionStandardVO vo = toVO(getById(id));
        vo.setItems(getItemsByStandardId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InspectionStandardCreateDTO dto) {
        InspectionStandard entity = InspectionStandardConverter.toEntity(dto);
        entity.setStandardNo(InspectionStandardConverter.generateStandardNo());
        entity.setInspectionStrategy(dto.getInspectionStrategy());
        entity.setSampleRate(dto.getSampleRate());
        entity.setAcceptanceRate(dto.getAcceptanceRate());
        inspectionStandardMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InspectionStandardUpdateDTO dto) {
        InspectionStandard entity = getById(dto.getId());
        if (StringUtils.hasText(dto.getMaterialCode())) entity.setMaterialCode(dto.getMaterialCode());
        if (StringUtils.hasText(dto.getMaterialName())) entity.setMaterialName(dto.getMaterialName());
        if (StringUtils.hasText(dto.getStandardName())) entity.setStandardName(dto.getStandardName());
        if (StringUtils.hasText(dto.getSampleRule())) entity.setSampleRule(dto.getSampleRule());
        if (StringUtils.hasText(dto.getVersionNo())) entity.setVersionNo(dto.getVersionNo());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        inspectionStandardMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        InspectionStandard entity = getById(id);
        entity.setStatus(status);
        inspectionStandardMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        inspectionStandardMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<InspectionStandardItem>().eq(InspectionStandardItem::getStandardId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionStandardItemVO addItem(InspectionStandardItemDTO dto) {
        InspectionStandardItem item = InspectionStandardConverter.toItemEntity(dto);
        itemMapper.insert(item);
        return InspectionStandardConverter.toItemVO(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(InspectionStandardItemDTO dto) {
        InspectionStandardItem item = itemMapper.selectById(dto.getStandardId());
        if (item == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        InspectionStandardItem updated = InspectionStandardConverter.toItemEntity(dto);
        updated.setId(item.getId());
        itemMapper.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        itemMapper.deleteById(itemId);
    }

    @Override
    public List<InspectionStandardItemVO> getItemsByStandardId(Long standardId) {
        List<InspectionStandardItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InspectionStandardItem>().eq(InspectionStandardItem::getStandardId, standardId).orderByAsc(InspectionStandardItem::getSort));
        return items.stream().map(InspectionStandardConverter::toItemVO).toList();
    }

    @Override
    public InspectionStandardVO getByMaterialCode(String materialCode) {
        InspectionStandard entity = inspectionStandardMapper.selectOne(
                new LambdaQueryWrapper<InspectionStandard>().eq(InspectionStandard::getMaterialCode, materialCode));
        if (entity == null) {
            return null;
        }
        InspectionStandardVO vo = toVO(entity);
        vo.setItems(getItemsByStandardId(entity.getId()));
        return vo;
    }

    private InspectionStandard getById(Long id) {
        InspectionStandard entity = inspectionStandardMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private InspectionStandardVO toVO(InspectionStandard entity) {
        InspectionStandardVO vo = InspectionStandardConverter.toVO(entity);
        vo.setInspectionStrategy(entity.getInspectionStrategy());
        vo.setSampleRate(entity.getSampleRate());
        vo.setAcceptanceRate(entity.getAcceptanceRate());
        return vo;
    }
}