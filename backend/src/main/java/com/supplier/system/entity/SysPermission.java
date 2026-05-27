package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String permName;
    private String permCode;
    private Integer permType;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
