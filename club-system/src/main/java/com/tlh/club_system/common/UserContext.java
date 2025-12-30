package com.tlh.club_system.common;

import com.tlh.club_system.entity.User;

/**
 * 当前登录用户上下文
 * 使用 ThreadLocal 存储当前请求的用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    
    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        User user = currentUser.get();
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        User user = currentUser.get();
        return user != null ? user.getRoleKey() : null;
    }
    
    /**
     * 获取当前用户所属院系ID
     */
    public static Integer getCurrentUserDeptId() {
        User user = currentUser.get();
        return user != null ? user.getDeptId() : null;
    }
    
    /**
     * 清除当前用户
     */
    public static void clear() {
        currentUser.remove();
    }
    
    /**
     * 判断当前用户是否登录
     */
    public static boolean isLogin() {
        return currentUser.get() != null;
    }
}
