package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private Long tenantId;
    private String roleName;
    private String roleCode;
    private Integer roleType;
    private Integer dataScope;
    private String description;
    private Integer sort;
    private Integer status;
}
