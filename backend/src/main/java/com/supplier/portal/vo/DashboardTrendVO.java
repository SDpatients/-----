package com.supplier.portal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendVO {
    private String period;
    private Long orderCount;
    private Long deliveryCount;
}
