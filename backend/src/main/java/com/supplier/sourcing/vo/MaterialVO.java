package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialVO {

    private Long id;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private String category;
    private Integer status;
    private LocalDateTime createTime;
}