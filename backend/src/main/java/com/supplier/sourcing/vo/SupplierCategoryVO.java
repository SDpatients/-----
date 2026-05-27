package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierCategoryVO {

    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}