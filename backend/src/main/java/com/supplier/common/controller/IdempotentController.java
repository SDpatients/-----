package com.supplier.common.controller;

import com.supplier.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "通用接口", description = "幂等令牌等通用接口")
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class IdempotentController {

    private final RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "获取幂等令牌", description = "前端在提交表单前调用此接口获取一次性幂等令牌")
    @GetMapping("/idempotent-token")
    public Result<String> getToken() {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("idempotent:" + token, "1", 5, TimeUnit.MINUTES);
        return Result.success("获取成功", token);
    }
}