package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RfqInviteDTO {

    @NotEmpty(message = "供应商ID列表不能为空")
    private List<Long> supplierIds;
}