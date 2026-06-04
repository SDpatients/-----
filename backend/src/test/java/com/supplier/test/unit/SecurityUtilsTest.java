package com.supplier.test.unit;

import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.system.entity.SysPermission;
import com.supplier.system.entity.SysRole;
import com.supplier.system.entity.SysUser;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SecurityUtils 单元测试")
class SecurityUtilsTest extends BaseUnitTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private LoginUser createLoginUser(Long userId, String realName, Integer userType, Long supplierId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setRealName(realName);
        user.setUserType(userType);
        user.setSupplierId(supplierId);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setStatus(1);
        return new LoginUser(user, Collections.emptyList(), Collections.emptyList());
    }

    private void setAuthentication(LoginUser loginUser) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("getLoginUser 方法测试")
    class GetLoginUserTest {

        @Test
        @DisplayName("SecurityContext 中有 LoginUser 时返回 LoginUser")
        void getLoginUserWithAuthenticatedUser() {
            LoginUser loginUser = createLoginUser(1L, "张三", 1, null);
            setAuthentication(loginUser);

            LoginUser result = SecurityUtils.getLoginUser();

            assertEquals(loginUser, result);
        }

        @Test
        @DisplayName("SecurityContext 为空时返回 null")
        void getLoginUserWithEmptyContext() {
            SecurityContextHolder.clearContext();

            assertNull(SecurityUtils.getLoginUser());
        }

        @Test
        @DisplayName("Authentication 的 principal 不是 LoginUser 时返回 null")
        void getLoginUserWithNonLoginUserPrincipal() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            assertNull(SecurityUtils.getLoginUser());
        }

        @Test
        @DisplayName("Authentication 为 null 时返回 null")
        void getLoginUserWithNullAuthentication() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            assertNull(SecurityUtils.getLoginUser());
        }
    }

    @Nested
    @DisplayName("getUserId 方法测试")
    class GetUserIdTest {

        @Test
        @DisplayName("已登录用户返回正确的 userId")
        void getUserIdWithAuthenticatedUser() {
            LoginUser loginUser = createLoginUser(100L, "张三", 1, null);
            setAuthentication(loginUser);

            assertEquals(100L, SecurityUtils.getUserId());
        }

        @Test
        @DisplayName("未登录时返回 null")
        void getUserIdWithoutAuthentication() {
            SecurityContextHolder.clearContext();

            assertNull(SecurityUtils.getUserId());
        }
    }

    @Nested
    @DisplayName("isSupplierUser 方法测试")
    class IsSupplierUserTest {

        @Test
        @DisplayName("userType=2 时返回 true")
        void isSupplierUserWithType2() {
            LoginUser loginUser = createLoginUser(1L, "供应商用户", 2, 100L);
            setAuthentication(loginUser);

            assertTrue(SecurityUtils.isSupplierUser());
        }

        @Test
        @DisplayName("userType=1 时返回 false")
        void isNotSupplierUserWithType1() {
            LoginUser loginUser = createLoginUser(1L, "平台用户", 1, null);
            setAuthentication(loginUser);

            assertFalse(SecurityUtils.isSupplierUser());
        }

        @Test
        @DisplayName("未登录时返回 false")
        void isSupplierUserWithoutAuthentication() {
            SecurityContextHolder.clearContext();

            assertFalse(SecurityUtils.isSupplierUser());
        }

        @Test
        @DisplayName("userType=null 时返回 false")
        void isSupplierUserWithNullType() {
            LoginUser loginUser = createLoginUser(1L, "用户", null, null);
            setAuthentication(loginUser);

            assertFalse(SecurityUtils.isSupplierUser());
        }
    }

    @Nested
    @DisplayName("getSupplierId 方法测试")
    class GetSupplierIdTest {

        @Test
        @DisplayName("供应商用户返回正确的 supplierId")
        void getSupplierIdWithSupplierUser() {
            LoginUser loginUser = createLoginUser(1L, "供应商用户", 2, 200L);
            setAuthentication(loginUser);

            assertEquals(200L, SecurityUtils.getSupplierId());
        }

        @Test
        @DisplayName("非供应商用户 supplierId 为 null")
        void getSupplierIdWithNonSupplierUser() {
            LoginUser loginUser = createLoginUser(1L, "平台用户", 1, null);
            setAuthentication(loginUser);

            assertNull(SecurityUtils.getSupplierId());
        }

        @Test
        @DisplayName("未登录时返回 null")
        void getSupplierIdWithoutAuthentication() {
            SecurityContextHolder.clearContext();

            assertNull(SecurityUtils.getSupplierId());
        }
    }
}
