package com.supplier.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer userType;
    private Long supplierId;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private String remark;
}