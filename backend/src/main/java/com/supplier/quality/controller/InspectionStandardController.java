package com.supplier.quality.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.quality.dto.InspectionStandardCreateDTO;
import com.supplier.quality.dto.InspectionStandardItemDTO;
import com.supplier.quality.dto.InspectionStandardUpdateDTO;
import com.supplier.quality.query.InspectionStandardQuery;
import com.supplier.quality.service.InspectionStandardService;
import com.supplier.quality.vo.InspectionStandardItemVO;
import com.supplier.quality.vo.InspectionStandardVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/inspection-standards")
@RequiredArgsConstructor
public class InspectionStandardController {

    private final InspectionStandardService inspectionStandardService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<InspectionStandardVO>> page(@Valid InspectionStandardQuery query) {
        return Result.success(inspectionStandardService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<InspectionStandardVO> detail(@PathVariable @NotNull Long id) {
        return Result.success(inspectionStandardService.getDetail(id));
    }

    @GetMapping("/by-material/{materialCode}")
    @PreAuthorize("isAuthenticated()")
    public Result<InspectionStandardVO> getByMaterialCode(@PathVariable String materialCode) {
        return Result.success(inspectionStandardService.getByMaterialCode(materialCode));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody InspectionStandardCreateDTO dto) {
        return Result.success(inspectionStandardService.create(dto));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@Valid @RequestBody InspectionStandardUpdateDTO dto) {
        inspectionStandardService.update(dto);
        return Result.success();
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateStatus(@PathVariable @NotNull Long id, @RequestParam Integer status) {
        inspectionStandardService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        inspectionStandardService.delete(id);
        return Result.success();
    }

    @GetMapping("/{standardId}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<List<InspectionStandardItemVO>> items(@PathVariable @NotNull Long standardId) {
        return Result.success(inspectionStandardService.getItemsByStandardId(standardId));
    }

    @PostMapping("/{standardId}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<InspectionStandardItemVO> addItem(@PathVariable @NotNull Long standardId, @Valid @RequestBody InspectionStandardItemDTO dto) {
        dto.setStandardId(standardId);
        return Result.success(inspectionStandardService.addItem(dto));
    }

    @PutMapping("/{standardId}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateItem(@Valid @RequestBody InspectionStandardItemDTO dto) {
        inspectionStandardService.updateItem(dto);
        return Result.success();
    }

    @DeleteMapping("/{standardId}/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteItem(@PathVariable @NotNull Long itemId) {
        inspectionStandardService.deleteItem(itemId);
        return Result.success();
    }
}