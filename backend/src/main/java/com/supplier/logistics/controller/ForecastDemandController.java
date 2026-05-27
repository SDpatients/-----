package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.DemandActionDTO;
import com.supplier.logistics.dto.ForecastDemandCreateDTO;
import com.supplier.logistics.query.ForecastDemandQuery;
import com.supplier.logistics.service.ForecastDemandService;
import com.supplier.logistics.vo.ForecastDemandVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "需求预测管理", description = "需求预测查询、新增、发布、响应和关闭接口")
@RestController
@RequestMapping("/v1/forecast-demands")
@RequiredArgsConstructor
public class ForecastDemandController {

    private final ForecastDemandService forecastDemandService;

    @Operation(summary = "分页查询需求预测")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ForecastDemandVO>> page(@Valid ForecastDemandQuery query) {
        return Result.success(forecastDemandService.page(query));
    }

    @Operation(summary = "查询需求预测详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ForecastDemandVO> detail(@PathVariable @NotNull(message = "需求ID不能为空") Long id) {
        return Result.success(forecastDemandService.getDetail(id));
    }

    @Operation(summary = "新增需求预测")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ForecastDemandCreateDTO dto) {
        return Result.success(forecastDemandService.create(dto));
    }

    @Operation(summary = "发布需求预测")
    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> publish(@PathVariable @NotNull(message = "需求ID不能为空") Long id,
                                @RequestBody DemandActionDTO dto) {
        forecastDemandService.publish(id, dto);
        return Result.success();
    }

    @Operation(summary = "响应需求预测")
    @PostMapping("/{id}/respond")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> respond(@PathVariable @NotNull(message = "需求ID不能为空") Long id,
                                @RequestBody DemandActionDTO dto) {
        forecastDemandService.respond(id, dto);
        return Result.success();
    }

    @Operation(summary = "关闭需求预测")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "需求ID不能为空") Long id,
                              @RequestBody DemandActionDTO dto) {
        forecastDemandService.close(id, dto);
        return Result.success();
    }
}