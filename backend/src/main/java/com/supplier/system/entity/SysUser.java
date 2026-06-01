package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private Long tenantId;
    private Long orgId;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer userType;
    private Long supplierId;
    private Integer status;
    private LocalDateTime passwordUpdateTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer tokenVersion;
    private String remark;
}
