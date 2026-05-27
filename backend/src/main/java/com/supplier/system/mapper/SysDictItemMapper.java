package com.supplier.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.system.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    @Select("""
            SELECT di.* FROM sys_dict_item di
            INNER JOIN sys_dict d ON d.id = di.dict_id AND d.deleted = 0
            WHERE d.dict_code = #{dictCode} AND di.deleted = 0 AND di.status = 1
            ORDER BY di.sort ASC, di.id ASC
            """)
    List<SysDictItem> selectByDictCode(@Param("dictCode") String dictCode);
}