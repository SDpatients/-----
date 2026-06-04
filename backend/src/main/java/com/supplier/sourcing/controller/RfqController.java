package com.supplier.sourcing.controller;

import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.dto.PricingDTO;
import com.supplier.sourcing.dto.RfqCreateDTO;
import com.supplier.sourcing.dto.RfqInviteDTO;
import com.supplier.sourcing.dto.RfqItemUpdateDTO;
import com.supplier.sourcing.dto.RfqUpdateDTO;
import com.supplier.sourcing.query.RfqQuery;
import com.supplier.sourcing.service.RfqItemService;
import com.supplier.sourcing.service.RfqService;
import com.supplier.sourcing.service.RfqSupplierService;
import com.supplier.sourcing.vo.RfqItemVO;
import com.supplier.sourcing.vo.RfqSupplierVO;
import com.supplier.sourcing.vo.RfqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@Tag(name = "询价管理", description = "询价单管理")
@RestController
@RequestMapping("/v1/rfqs")
@RequiredArgsConstructor
public class RfqController {

    private final RfqService rfqService;
    private final RfqItemService rfqItemService;
    private final RfqSupplierService rfqSupplierService;

    @Operation(summary = "分页查询询价单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<RfqVO>> page(@Valid RfqQuery query) {
        log.info("[RFQ-DEBUG] Controller.page: isSupplierUser={}, supplierId={}, query.supplierId={}",
                SecurityUtils.isSupplierUser(), SecurityUtils.getSupplierId(), query.getSupplierId());
        if (SecurityUtils.isSupplierUser()) {
            Long supplierId = SecurityUtils.getSupplierId();
            if (supplierId == null) {
                log.warn("[RFQ-DEBUG] 供应商用户但 supplierId 为 null! userId={}, username={}",
                        SecurityUtils.getUserId(), SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getUsername() : "null");
                return Result.success(PageResult.of(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getPageNum(), query.getPageSize())));
            }
            query.setSupplierId(supplierId);
        }
        return Result.success(rfqService.page(query));
    }

    @Operation(summary = "查询询价单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<RfqVO> detail(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        return Result.success(rfqService.getDetail(id));
    }

    @Operation(summary = "新增询价单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody RfqCreateDTO dto) {
        return Result.success(rfqService.create(dto));
    }

    @Operation(summary = "更新询价单")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                               @Valid @RequestBody RfqUpdateDTO dto) {
        rfqService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "发布询价单")
    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> publish(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.publish(id);
        return Result.success();
    }

    @Operation(summary = "截止询价")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.close(id);
        return Result.success();
    }

    @Operation(summary = "取消询价单")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "定价确认")
    @PostMapping("/{id}/price")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> price(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                              @Valid @RequestBody PricingDTO dto) {
        rfqService.price(id, dto);
        return Result.success();
    }

    @Operation(summary = "查询询价单物料行")
    @GetMapping("/{id}/lines")
    @PreAuthorize("isAuthenticated()")
    public Result<List<RfqItemVO>> lines(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        checkSupplierAccess(id);
        return Result.success(rfqItemService.getByRfqId(id));
    }

    @Operation(summary = "保存询价单物料行")
    @PutMapping("/{id}/lines")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> saveLines(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                                  @Valid @RequestBody List<RfqItemUpdateDTO> lines) {
        rfqItemService.saveLines(id, lines);
        return Result.success();
    }

    @Operation(summary = "邀请供应商")
    @PostMapping("/{id}/invite")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> invite(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                               @Valid @RequestBody RfqInviteDTO dto) {
        rfqSupplierService.inviteSuppliers(id, dto.getSupplierIds());
        return Result.success();
    }

    @Operation(summary = "查询已邀请供应商列表")
    @GetMapping("/{id}/invited")
    @PreAuthorize("isAuthenticated()")
    public Result<List<RfqSupplierVO>> invited(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        return Result.success(rfqSupplierService.getInvitedByRfqId(id));
    }

    private void checkSupplierAccess(Long rfqId) {
        if (SecurityUtils.isSupplierUser()) {
            Long supplierId = SecurityUtils.getSupplierId();
            if (supplierId == null) {
                throw BusinessException.of(ResultCode.FORBIDDEN.getCode(), "供应商信息缺失，无法访问该询价单");
            }
            List<RfqSupplierVO> invited = rfqSupplierService.getInvitedByRfqId(rfqId);
            boolean hasAccess = invited.stream()
                    .anyMatch(s -> supplierId.equals(s.getSupplierId())
                            && (Integer.valueOf(1).equals(s.getInviteStatus()) || Integer.valueOf(2).equals(s.getInviteStatus())));
            if (!hasAccess) {
                throw BusinessException.of(ResultCode.FORBIDDEN.getCode(), "您未被邀请参与该询价单");
            }
        }
    }
}