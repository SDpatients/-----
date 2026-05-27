package com.supplier.system.vo;

import lombok.Data;

@Data
public class SysDictItemVO {

    private Long id;
    private Long dictId;
    private String itemLabel;
    private String itemValue;
    private Integer sort;
    private String description;
}