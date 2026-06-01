package com.supplier.sourcing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RfqCreateDTO {

    private String rfqNo;

    @NotBlank(message = "询价标题不能为空")
    private String rfqTitle;

    private Long orgId;
    private String currency;

    @NotNull(message = "报价截止时间不能为空")
    private LocalDateTime quoteDeadline;

    private String remark;

    @NotEmpty(message = "询价行项目不能为空")
    @Valid
    private List<RfqLineDTO> lines;

    @Data
    public static class RfqLineDTO {
        private Integer lineNo;

        @NotBlank(message = "物料编码不能为空")
        private String materialCode;

        @NotBlank(message = "物料名称不能为空")
        private String materialName;

        private String spec;
        private String unit;
        private BigDecimal quantity;
        private LocalDate deliveryDate;
        private String remark;
    }
}