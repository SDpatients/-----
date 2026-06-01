package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material_info")
public class MaterialInfo extends BaseEntity {

    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private String category;
    private Integer status;
}