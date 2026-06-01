package com.supplier.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成测试用 BCrypt 密码哈希
 */
public class TestHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("test123456");
        System.out.println("BCrypt hash for 'test123456': " + hash);
    }
}