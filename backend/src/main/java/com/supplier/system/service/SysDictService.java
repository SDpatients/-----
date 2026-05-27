package com.supplier.system.service;

import com.supplier.system.vo.SysDictItemVO;

import java.util.List;

public interface SysDictService {

    /**
     * 根据字典编码获取字典项列表（带缓存）
     */
    List<SysDictItemVO> getItemsByCode(String dictCode);

    /**
     * 清除指定字典编码的缓存
     */
    void evictCache(String dictCode);
}