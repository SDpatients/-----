package com.supplier.sourcing.controller;

import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteUpdateDTO;
import com.supplier.sourcing.service.QuoteService;
import com.supplier.sourcing.vo.BargainVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "议价管理", description = "报价议价/还价记录管理")
@RestController
@RequestMapping("/v1/bargains")
@RequiredArgsConstructor
public class BargainController {

    private final QuoteService quoteService;

    @Operation(summary = "查询议价记录")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<BargainVO>> list(@RequestParam @NotNull(message = "报价单ID不能为空") Long quoteId) {
        return Result.success(quoteService.getBargains(quoteId));
    }

    @Operation(summary = "采购方发起还价")
    @PostMapping("/{quoteId}/reprice")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reprice(@PathVariable @NotNull(message = "报价单ID不能为空") Long quoteId,
                                @Valid @RequestBody BargainDTO dto) {
        quoteService.bargain(quoteId, dto);
        return Result.success();
    }

    @Operation(summary = "供应商重新提交报价")
    @PostMapping("/{quoteId}/resubmit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> resubmit(@PathVariable @NotNull(message = "报价单ID不能为空") Long quoteId,
                                 @RequestBody QuoteUpdateDTO dto) {
        quoteService.resubmit(quoteId, dto);
        return Result.success();
    }
}