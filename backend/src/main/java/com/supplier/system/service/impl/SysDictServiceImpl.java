package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.system.dto.SysDictCreateDTO;
import com.supplier.system.dto.SysDictItemCreateDTO;
import com.supplier.system.dto.SysDictItemUpdateDTO;
import com.supplier.system.entity.SysDict;
import com.supplier.system.entity.SysDictItem;
import com.supplier.system.mapper.SysDictItemMapper;
import com.supplier.system.mapper.SysDictMapper;
import com.supplier.system.service.SysDictService;
import com.supplier.system.vo.SysDictItemVO;
import com.supplier.system.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictMapper sysDictMapper;
    private final SysDictItemMapper sysDictItemMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<SysDictVO> listAll() {
        List<SysDict> dicts = sysDictMapper.selectList(
                new LambdaQueryWrapper<SysDict>().orderByAsc(SysDict::getId));
        return dicts.stream().map(this::toDictVO).collect(Collectors.toList());
    }

    @Override
    public Long createDict(SysDictCreateDTO dto) {
        SysDict dict = new SysDict();
        dict.setDictName(dto.getDictName());
        dict.setDictCode(dto.getDictCode());
        dict.setDescription(dto.getDescription());
        dict.setStatus(1);
        sysDictMapper.insert(dict);
        return dict.getId();
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void deleteDict(Long id) {
        sysDictMapper.deleteById(id);
        sysDictItemMapper.delete(
                new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, id));
    }

    @Override
    public List<SysDictItemVO> getItemsByDictId(Long dictId) {
        List<SysDictItem> items = sysDictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dictId)
                        .orderByAsc(SysDictItem::getSort)
                        .orderByAsc(SysDictItem::getId));
        return items.stream().map(this::toItemVO).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public Long createItem(SysDictItemCreateDTO dto) {
        SysDictItem item = new SysDictItem();
        item.setDictId(dto.getDictId());
        item.setItemLabel(dto.getItemLabel());
        item.setItemValue(dto.getItemValue());
        item.setSort(dto.getSort() != null ? dto.getSort() : 0);
        item.setDescription(dto.getDescription());
        item.setStatus(1);
        sysDictItemMapper.insert(item);
        return item.getId();
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void updateItem(Long id, SysDictItemUpdateDTO dto) {
        SysDictItem item = sysDictItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("字典项不存在");
        }
        if (StringUtils.hasText(dto.getItemLabel())) item.setItemLabel(dto.getItemLabel());
        if (StringUtils.hasText(dto.getItemValue())) item.setItemValue(dto.getItemValue());
        if (dto.getSort() != null) item.setSort(dto.getSort());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
        sysDictItemMapper.updateById(item);
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void deleteItem(Long id) {
        sysDictItemMapper.hardDeleteById(id);
    }

    @Override
    @Cacheable(value = "dict", key = "#dictCode")
    public List<SysDictItemVO> getItemsByCode(String dictCode) {
        List<SysDictItem> items = sysDictItemMapper.selectByDictCode(dictCode);
        return items.stream().map(this::toItemVO).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "dict", key = "#dictCode")
    public void evictCache(String dictCode) {
        log.info("字典缓存已清除: {}", dictCode);
    }

    private SysDictVO toDictVO(SysDict dict) {
        SysDictVO vo = new SysDictVO();
        vo.setId(dict.getId());
        vo.setDictName(dict.getDictName());
        vo.setDictCode(dict.getDictCode());
        vo.setDescription(dict.getDescription());
        vo.setStatus(dict.getStatus());
        if (dict.getCreateTime() != null) {
            vo.setCreateTime(dict.getCreateTime().format(FMT));
        }
        return vo;
    }

    private SysDictItemVO toItemVO(SysDictItem item) {
        SysDictItemVO vo = new SysDictItemVO();
        vo.setId(item.getId());
        vo.setDictId(item.getDictId());
        vo.setItemLabel(item.getItemLabel());
        vo.setItemValue(item.getItemValue());
        vo.setSort(item.getSort());
        vo.setDescription(item.getDescription());
        vo.setStatus(item.getStatus());
        return vo;
    }
}