package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCategoryCreateDTO {

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;

    private Integer sort;
    private String remark;
}