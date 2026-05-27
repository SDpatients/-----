package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    private Long dictId;
    private String itemLabel;
    private String itemValue;
    private Integer sort;
    private String description;
    private Integer status;
}