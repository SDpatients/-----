package com.supplier.security.filter;

import com.supplier.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 检查 Redis 黑名单
            String jti = jwtUtil.getJtiFromToken(token);
            if (jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti))) {
                log.warn("JWT令牌已失效(黑名单): jti={}", jti);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "令牌已失效，请重新登录");
                return;
            }

            String username = jwtUtil.getUsernameFromToken(token);

            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT认证成功, 用户: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtUtil.getPrefix())) {
            return bearerToken.substring(jwtUtil.getPrefix().length());
        }
        return null;
    }
}
