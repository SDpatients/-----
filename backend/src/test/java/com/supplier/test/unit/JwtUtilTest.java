package com.supplier.test.unit;

import com.supplier.security.util.JwtUtil;
import com.supplier.test.BaseUnitTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest extends BaseUnitTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "supplier-collaboration-secret-key-for-jwt-token-generation-and-validation-must-be-at-least-256-bits";
    private static final Long EXPIRATION = 86400000L;
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "header", HEADER);
        ReflectionTestUtils.setField(jwtUtil, "prefix", PREFIX);
    }

    @Nested
    @DisplayName("generateToken 方法测试")
    class GenerateTokenTest {

        @Test
        @DisplayName("generateToken(username) 生成非空 token")
        void generateTokenWithUsername() {
            String token = jwtUtil.generateToken("admin");

            assertNotNull(token);
            assertTrue(token.length() > 0);
        }

        @Test
        @DisplayName("generateToken(username, claims) 生成包含自定义声明的 token")
        void generateTokenWithClaims() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            String token = jwtUtil.generateToken("admin", claims);

            assertNotNull(token);

            Claims parsed = jwtUtil.parseToken(token);
            assertNotNull(parsed);
            assertEquals("admin", parsed.getSubject());
            assertEquals("ADMIN", parsed.get("role", String.class));
        }
    }

    @Nested
    @DisplayName("parseToken 方法测试")
    class ParseTokenTest {

        @Test
        @DisplayName("解析有效 token 返回正确的 subject")
        void parseValidToken() {
            String token = jwtUtil.generateToken("testuser");
            Claims claims = jwtUtil.parseToken(token);

            assertNotNull(claims);
            assertEquals("testuser", claims.getSubject());
        }

        @Test
        @DisplayName("解析格式错误的 token 返回 null")
        void parseMalformedToken() {
            Claims claims = jwtUtil.parseToken("invalid.token.format");

            assertNull(claims);
        }

        @Test
        @DisplayName("解析空字符串返回 null")
        void parseEmptyToken() {
            Claims claims = jwtUtil.parseToken("");

            assertNull(claims);
        }

        @Test
        @DisplayName("解析 null 返回 null")
        void parseNullToken() {
            Claims claims = jwtUtil.parseToken(null);

            assertNull(claims);
        }
    }

    @Nested
    @DisplayName("validateToken 方法测试")
    class ValidateTokenTest {

        @Test
        @DisplayName("有效 token 验证通过")
        void validateValidToken() {
            String token = jwtUtil.generateToken("admin");

            assertTrue(jwtUtil.validateToken(token));
        }

        @Test
        @DisplayName("格式错误的 token 验证失败")
        void validateMalformedToken() {
            assertFalse(jwtUtil.validateToken("malformed.token.here"));
        }

        @Test
        @DisplayName("过期的 token 验证失败")
        void validateExpiredToken() {
            ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
            String token = jwtUtil.generateToken("admin");

            ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
            assertFalse(jwtUtil.validateToken(token));
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken 方法测试")
    class GetUsernameFromTokenTest {

        @Test
        @DisplayName("从有效 token 中获取用户名")
        void getUsernameFromValidToken() {
            String token = jwtUtil.generateToken("admin");
            String username = jwtUtil.getUsernameFromToken(token);

            assertEquals("admin", username);
        }

        @Test
        @DisplayName("从无效 token 中获取用户名返回 null")
        void getUsernameFromInvalidToken() {
            String username = jwtUtil.getUsernameFromToken("invalid.token");

            assertNull(username);
        }
    }

    @Nested
    @DisplayName("getJtiFromToken 方法测试")
    class GetJtiFromTokenTest {

        @Test
        @DisplayName("从 token 中获取唯一 ID (jti)")
        void getJtiFromValidToken() {
            String token = jwtUtil.generateToken("admin");
            String jti = jwtUtil.getJtiFromToken(token);

            assertNotNull(jti);
            assertTrue(jti.length() > 0);
        }

        @Test
        @DisplayName("每次生成的 token 的 jti 不同")
        void jtiIsUnique() {
            String token1 = jwtUtil.generateToken("admin");
            String token2 = jwtUtil.generateToken("admin");
            String jti1 = jwtUtil.getJtiFromToken(token1);
            String jti2 = jwtUtil.getJtiFromToken(token2);

            assertNotNull(jti1);
            assertNotNull(jti2);
            assertFalse(jti1.equals(jti2));
        }
    }

    @Nested
    @DisplayName("getExpirationDateFromToken 方法测试")
    class GetExpirationDateFromTokenTest {

        @Test
        @DisplayName("从 token 中获取过期时间")
        void getExpirationDateFromValidToken() {
            String token = jwtUtil.generateToken("admin");
            java.util.Date expiration = jwtUtil.getExpirationDateFromToken(token);

            assertNotNull(expiration);
            assertTrue(expiration.after(new java.util.Date()));
        }
    }

    @Nested
    @DisplayName("配置属性测试")
    class ConfigurationTest {

        @Test
        @DisplayName("getHeader() 返回配置的 header 值")
        void getHeaderReturnsConfiguredValue() {
            assertEquals(HEADER, jwtUtil.getHeader());
        }

        @Test
        @DisplayName("getPrefix() 返回配置的 prefix 值")
        void getPrefixReturnsConfiguredValue() {
            assertEquals(PREFIX, jwtUtil.getPrefix());
        }
    }
}
