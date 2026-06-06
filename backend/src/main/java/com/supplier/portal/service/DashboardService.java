package com.supplier.portal.service;

import com.supplier.portal.vo.DashboardMetricVO;
import com.supplier.portal.vo.DashboardRiskVO;
import com.supplier.portal.vo.DashboardTrendVO;

import java.util.List;

public interface DashboardService {
    List<DashboardMetricVO> metrics();
    List<DashboardTrendVO> trends();
    List<DashboardRiskVO> risks();
}
