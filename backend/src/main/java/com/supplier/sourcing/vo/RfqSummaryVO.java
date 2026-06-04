package com.supplier.sourcing.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 询价单简要信息（用于报价对比页左侧列表）
 * 继承自 RfqVO，并补充报价数与最后报价时间。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RfqSummaryVO extends RfqVO {

    /** 该询价单下的报价数量 */
    private Long quoteCount;

    /** 最后一次报价时间（用于排序与默认选中） */
    private LocalDateTime latestQuoteTime;
}
