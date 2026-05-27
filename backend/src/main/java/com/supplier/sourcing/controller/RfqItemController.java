package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.RfqItemCreateDTO;
import com.supplier.sourcing.dto.RfqItemUpdateDTO;
import com.supplier.sourcing.query.RfqItemQuery;
import com.supplier.sourcing.service.RfqItemService;
import com.supplier.sourcing.vo.RfqItemVO;
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
@Tag(name = "询价物料管理", description = "询价单物料明细管理")
@RestController
@RequestMapping("/v1/rfq-items")
@RequiredArgsConstructor
public class RfqItemController {

    private final RfqItemService rfqItemService;

    @Operation(summary = "分页查询物料明细")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<RfqItemVO>> page(@Valid RfqItemQuery query) {
        return Result.success(rfqItemService.page(query));
    }

    @Operation(summary = "查询物料明细详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<RfqItemVO> detail(@PathVariable @NotNull(message = "物料ID不能为空") Long id) {
        return Result.success(rfqItemService.getDetail(id));
    }

    @Operation(summary = "新增物料明细")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody RfqItemCreateDTO dto) {
        return Result.success(rfqItemService.create(dto));
    }

    @Operation(summary = "更新物料明细")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "物料ID不能为空") Long id,
                               @Valid @RequestBody RfqItemUpdateDTO dto) {
        rfqItemService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除物料明细")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "物料ID不能为空") Long id) {
        rfqItemService.delete(id);
        return Result.success();
    }
}