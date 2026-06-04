package com.supplier.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDictItemUpdateDTO {

    @NotBlank(message = "字典项标签不能为空")
    private String itemLabel;

    @NotBlank(message = "字典项值不能为空")
    private String itemValue;

    private Integer sort;
    private String description;
    private Integer status;
}