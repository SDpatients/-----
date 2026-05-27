package com.supplier.quality.query;

import lombok.Data;

/**
 * 申诉查询参数
 */
@Data
public class AppealQuery {

    private Long supplierId;
    private Long ncrId;
    private Integer appealStatus;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}