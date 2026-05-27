package com.supplier.security.util;

import com.supplier.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static boolean isSupplierUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && Integer.valueOf(2).equals(loginUser.getUserType());
    }

    public static Long getSupplierId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getSupplierId();
    }
}
