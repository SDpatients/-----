package com.supplier.security.controller;

import com.supplier.common.result.Result;
import com.supplier.security.dto.LoginRequest;
import com.supplier.security.dto.LoginResponse;
import com.supplier.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "通过用户名密码登录获取JWT token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success("登录成功", loginResponse);
    }

    @Operation(summary = "用户登出", description = "退出登录清除认证信息")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功", null);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    @GetMapping("/info")
    public Result<LoginResponse.UserInfo> getUserInfo() {
        return Result.success("获取成功", authService.getCurrentUserInfo());
    }
}
