package com.supplier.system.service;

import com.supplier.system.dto.SysDictCreateDTO;
import com.supplier.system.dto.SysDictItemCreateDTO;
import com.supplier.system.dto.SysDictItemUpdateDTO;
import com.supplier.system.vo.SysDictItemVO;
import com.supplier.system.vo.SysDictVO;

import java.util.List;

public interface SysDictService {

    List<SysDictVO> listAll();

    Long createDict(SysDictCreateDTO dto);

    void deleteDict(Long id);

    List<SysDictItemVO> getItemsByDictId(Long dictId);

    Long createItem(SysDictItemCreateDTO dto);

    void updateItem(Long id, SysDictItemUpdateDTO dto);

    void deleteItem(Long id);

    List<SysDictItemVO> getItemsByCode(String dictCode);

    void evictCache(String dictCode);
}