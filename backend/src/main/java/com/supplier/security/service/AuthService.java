package com.supplier.security.service;

import com.supplier.security.dto.LoginRequest;
import com.supplier.security.dto.LoginResponse;
import com.supplier.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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
        SecurityContextHolder.clearContext();
    }

    private LoginResponse.UserInfo buildUserInfo(Authentication authentication) {
        return LoginResponse.UserInfo.builder()
                .username(authentication.getName())
                .build();
    }
}
