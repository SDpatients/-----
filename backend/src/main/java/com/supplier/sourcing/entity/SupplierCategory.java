package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商品类/分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_category")
public class SupplierCategory extends BaseEntity {

    /** 父级ID */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类编码 */
    private String categoryCode;

    /** 排序 */
    private Integer sort;

    /** 状态: 0=禁用, 1=启用 */
    private Integer status;

    /** 分类描述 */
    private String description;

    /** 备注 */
    private String remark;
}