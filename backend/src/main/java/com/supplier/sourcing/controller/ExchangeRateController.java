package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.ExchangeRateCreateDTO;
import com.supplier.sourcing.dto.ExchangeRateUpdateDTO;
import com.supplier.sourcing.query.ExchangeRateQuery;
import com.supplier.sourcing.service.ExchangeRateService;
import com.supplier.sourcing.vo.ExchangeRateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "汇率管理", description = "币种汇率维护")
@RestController
@RequestMapping("/v1/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "分页查询汇率")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ExchangeRateVO>> page(@Valid ExchangeRateQuery query) {
        return Result.success(exchangeRateService.page(query));
    }

    @Operation(summary = "查询汇率详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ExchangeRateVO> detail(@PathVariable @NotNull(message = "汇率ID不能为空") Long id) {
        return Result.success(exchangeRateService.getDetail(id));
    }

    @Operation(summary = "新增汇率")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ExchangeRateCreateDTO dto) {
        return Result.success(exchangeRateService.create(dto));
    }

    @Operation(summary = "更新汇率")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "汇率ID不能为空") Long id,
                               @Valid @RequestBody ExchangeRateUpdateDTO dto) {
        exchangeRateService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除汇率")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "汇率ID不能为空") Long id) {
        exchangeRateService.delete(id);
        return Result.success();
    }
}