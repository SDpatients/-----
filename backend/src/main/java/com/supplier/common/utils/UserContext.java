package com.supplier.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                String userIdStr = request.getHeader(USER_ID_HEADER);
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        userId = Long.parseLong(userIdStr);
                        USER_ID.set(userId);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return userId;
    }

    public static void setUserName(String userName) {
        USER_NAME.set(userName);
    }

    public static String getUserName() {
        String userName = USER_NAME.get();
        if (userName == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                userName = request.getHeader(USER_NAME_HEADER);
                if (userName != null) {
                    USER_NAME.set(userName);
                }
            }
        }
        return userName;
    }

    public static void setUserRole(String userRole) {
        USER_ROLE.set(userRole);
    }

    public static String getUserRole() {
        String userRole = USER_ROLE.get();
        if (userRole == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                userRole = request.getHeader(USER_ROLE_HEADER);
                if (userRole != null) {
                    USER_ROLE.set(userRole);
                }
            }
        }
        return userRole;
    }

    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
        USER_ROLE.remove();
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
