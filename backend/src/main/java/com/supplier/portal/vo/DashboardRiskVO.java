package com.supplier.portal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRiskVO {
    private String riskType;
    private String title;
    private Long count;
    private String level;
}
