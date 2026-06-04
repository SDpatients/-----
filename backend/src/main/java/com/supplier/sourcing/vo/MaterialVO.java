package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private String category;
    private Integer status;
    private LocalDateTime createTime;
}