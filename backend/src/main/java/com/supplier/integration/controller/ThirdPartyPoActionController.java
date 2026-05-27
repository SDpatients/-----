package com.supplier.integration.controller;

import com.supplier.common.result.Result;
import com.supplier.integration.service.ThirdPartyPoApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@Tag(name = "第三方采购订单接口调用", description = "执行第三方采购订单接口调用")
@RestController
@RequestMapping("/v1/third-party-po")
@RequiredArgsConstructor
public class ThirdPartyPoActionController {

    private final ThirdPartyPoApiService thirdPartyPoApiService;

    @Operation(summary = "推送采购订单到第三方（新增）")
    @PostMapping("/{configId}/push/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<String> push(@PathVariable @NotNull(message = "配置ID不能为空") Long configId,
                               @PathVariable @NotNull(message = "订单ID不能为空") Long orderId) {
        String response = thirdPartyPoApiService.pushOrderToThirdParty(configId, orderId);
        return Result.success(response);
    }

    @Operation(summary = "从第三方获取最新采购订单")
    @PostMapping("/{configId}/fetch-latest")
    @PreAuthorize("isAuthenticated()")
    public Result<String> fetchLatest(@PathVariable @NotNull(message = "配置ID不能为空") Long configId,
                                      @RequestBody(required = false) Map<String, Object> params) {
        String response = thirdPartyPoApiService.fetchLatestOrderFromThirdParty(configId, params);
        return Result.success(response);
    }
}