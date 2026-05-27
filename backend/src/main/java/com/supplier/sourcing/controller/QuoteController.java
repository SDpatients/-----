package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.query.QuoteQuery;
import com.supplier.sourcing.service.QuoteCompareService;
import com.supplier.sourcing.service.QuoteService;
import com.supplier.sourcing.vo.QuoteCompareVO;
import com.supplier.sourcing.vo.QuoteNegotiationVO;
import com.supplier.sourcing.vo.QuoteVO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "报价管理", description = "报价单管理")
@RestController
@RequestMapping("/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteCompareService quoteCompareService;

    @Operation(summary = "分页查询报价单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<QuoteVO>> page(@Valid QuoteQuery query) {
        return Result.success(quoteService.page(query));
    }

    @Operation(summary = "查询报价单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<QuoteVO> detail(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        return Result.success(quoteService.getDetail(id));
    }

    @Operation(summary = "新增报价单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody QuoteCreateDTO dto) {
        return Result.success(quoteService.create(dto));
    }

    @Operation(summary = "提交报价")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        quoteService.submit(id);
        return Result.success();
    }

    @Operation(summary = "撤回报价")
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> withdraw(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        quoteService.withdraw(id);
        return Result.success();
    }

    @Operation(summary = "采纳报价")
    @PostMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> accept(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        quoteService.accept(id);
        return Result.success();
    }

    @Operation(summary = "拒绝报价")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        quoteService.reject(id);
        return Result.success();
    }

    @Operation(summary = "删除报价单")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        quoteService.delete(id);
        return Result.success();
    }

    @Operation(summary = "横向比价")
    @GetMapping("/compare/{rfqId}")
    @PreAuthorize("isAuthenticated()")
    public Result<QuoteCompareVO> compare(@PathVariable @NotNull(message = "询价单ID不能为空") Long rfqId) {
        return Result.success(quoteCompareService.compare(rfqId));
    }

    @Operation(summary = "发起议价")
    @PostMapping("/{id}/bargain")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> bargain(@PathVariable @NotNull(message = "报价单ID不能为空") Long id,
                                @Valid @RequestBody BargainDTO dto) {
        quoteService.bargain(id, dto);
        return Result.success();
    }

    @Operation(summary = "查询议价记录")
    @GetMapping("/{id}/negotiations")
    @PreAuthorize("isAuthenticated()")
    public Result<List<QuoteNegotiationVO>> negotiations(@PathVariable @NotNull(message = "报价单ID不能为空") Long id) {
        return Result.success(quoteService.getNegotiations(id));
    }
}