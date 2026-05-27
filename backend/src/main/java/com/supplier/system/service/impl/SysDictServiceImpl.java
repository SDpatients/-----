package com.supplier.system.service.impl;

import com.supplier.system.entity.SysDictItem;
import com.supplier.system.mapper.SysDictItemMapper;
import com.supplier.system.service.SysDictService;
import com.supplier.system.vo.SysDictItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictItemMapper sysDictItemMapper;

    @Override
    @Cacheable(value = "dict", key = "#dictCode")
    public List<SysDictItemVO> getItemsByCode(String dictCode) {
        List<SysDictItem> items = sysDictItemMapper.selectByDictCode(dictCode);
        return items.stream().map(item -> {
            SysDictItemVO vo = new SysDictItemVO();
            vo.setId(item.getId());
            vo.setDictId(item.getDictId());
            vo.setItemLabel(item.getItemLabel());
            vo.setItemValue(item.getItemValue());
            vo.setSort(item.getSort());
            vo.setDescription(item.getDescription());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "dict", key = "#dictCode")
    public void evictCache(String dictCode) {
        log.info("字典缓存已清除: {}", dictCode);
    }
}