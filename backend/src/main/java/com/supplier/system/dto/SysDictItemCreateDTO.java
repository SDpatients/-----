package com.supplier.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysDictItemCreateDTO {

    @NotNull(message = "字典ID不能为空")
    private Long dictId;

    @NotBlank(message = "字典项标签不能为空")
    private String itemLabel;

    @NotBlank(message = "字典项值不能为空")
    private String itemValue;

    private Integer sort;
    private String description;
}