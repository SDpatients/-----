package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.WriteOffCreateDTO;
import com.supplier.logistics.query.WriteOffQuery;
import com.supplier.logistics.service.WriteOffService;
import com.supplier.logistics.vo.WriteOffVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/write-offs")
@RequiredArgsConstructor
public class WriteOffController {
    private final WriteOffService writeOffService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<WriteOffVO>> page(@Valid WriteOffQuery query) {
        return Result.success(writeOffService.page(query));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody WriteOffCreateDTO dto) {
        return Result.success(writeOffService.create(dto));
    }
}
