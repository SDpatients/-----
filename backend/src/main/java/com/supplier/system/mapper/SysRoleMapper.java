package com.supplier.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.system.entity.SysRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            SELECT r.* FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id AND ur.deleted = 0
            WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1
            ORDER BY r.sort ASC, r.id ASC
            """)
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id, create_time, deleted) VALUES (#{userId}, #{roleId}, NOW(), 0)")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
