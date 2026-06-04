package com.supplier.system.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysUserQuery {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}