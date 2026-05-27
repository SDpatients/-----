package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    private String configName;
    private String configKey;
    private String configValue;
    private Integer configType;
    private Integer encrypted;
    private Integer status;
    private String remark;
}