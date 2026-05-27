package com.supplier.portal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricVO {
    private String name;
    private Long value;
    private String unit;
}
