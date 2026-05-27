package com.supplier.sourcing.query;

import lombok.Data;

@Data
public class RfqItemQuery {

    private Long rfqId;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}