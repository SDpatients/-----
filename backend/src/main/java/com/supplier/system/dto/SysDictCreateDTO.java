package com.supplier.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDictCreateDTO {

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    private String description;
}