package com.supplier.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class SysDictVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String dictName;
    private String dictCode;
    private String description;
    private Integer status;
    private String createTime;
}