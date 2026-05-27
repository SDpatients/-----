package com.supplier.sourcing.dto;

import lombok.Data;

@Data
public class SupplierCategoryUpdateDTO {

    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private Integer status;
    private String remark;
}