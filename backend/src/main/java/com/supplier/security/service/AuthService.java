package com.supplier.security.service;

import com.supplier.security.dto.LoginRequest;
import com.supplier.security.dto.LoginResponse;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.JwtUtil;
import com.supplier.system.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    public LoginResponse login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Object> claims = new HashMap<>();
        String token = jwtUtil.generateToken(loginRequest.getUsername(), claims);

        LoginResponse.UserInfo userInfo = buildUserInfo(authentication);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userInfo(userInfo)
                .build();
    }

    public void logout() {
        // 将当前 token 的 JTI 写入 Redis 黑名单
        String token = resolveTokenFromRequest();
        if (StringUtils.hasText(token)) {
            String jti = jwtUtil.getJtiFromToken(token);
            Date expiration = jwtUtil.getExpirationDateFromToken(token);
            if (jti != null && expiration != null) {
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttl, TimeUnit.MILLISECONDS);
                    log.info("JWT令牌已加入黑名单: jti={}, ttl={}ms", jti, ttl);
                }
            }
        }
        SecurityContextHolder.clearContext();
    }

    public LoginResponse.UserInfo getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return buildUserInfo(authentication);
    }

    private String resolveTokenFromRequest() {
        String bearerToken = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtUtil.getPrefix())) {
            return bearerToken.substring(jwtUtil.getPrefix().length());
        }
        return null;
    }

    private LoginResponse.UserInfo buildUserInfo(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            SysUser user = loginUser.getUser();
            return LoginResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(loginUser.getRoleCodes().isEmpty() ? null : loginUser.getRoleCodes().get(0))
                    .userType(user.getUserType())
                    .supplierId(user.getSupplierId())
                    .roles(loginUser.getRoleCodes())
                    .permissions(loginUser.getPermissionCodes())
                    .build();
        }
        return LoginResponse.UserInfo.builder()
                .username(authentication == null ? null : authentication.getName())
                .build();
    }
}
