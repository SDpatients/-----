package com.supplier.portal.controller;

import com.supplier.common.result.Result;
import com.supplier.portal.service.DashboardService;
import com.supplier.portal.vo.DashboardMetricVO;
import com.supplier.portal.vo.DashboardRiskVO;
import com.supplier.portal.vo.DashboardTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/metrics")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DashboardMetricVO>> metrics() { return Result.success(service.metrics()); }

    @GetMapping("/trends")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DashboardTrendVO>> trends() { return Result.success(service.trends()); }

    @GetMapping("/risks")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DashboardRiskVO>> risks() { return Result.success(service.risks()); }
}
