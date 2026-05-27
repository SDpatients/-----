package com.supplier.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("""
            SELECT DISTINCT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.perm_id = p.id AND rp.deleted = 0
            INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id AND ur.deleted = 0
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0 AND r.status = 1
            WHERE ur.user_id = #{userId} AND p.deleted = 0 AND p.status = 1
            ORDER BY p.parent_id ASC, p.sort ASC, p.id ASC
            """)
    List<SysPermission> selectByUserId(@Param("userId") Long userId);
}
