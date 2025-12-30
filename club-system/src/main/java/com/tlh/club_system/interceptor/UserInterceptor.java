package com.tlh.club_system.interceptor;

import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.UserMapper;
import com.tlh.club_system.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户身份拦截器 (JWT版)
 */
@Component
public class UserInterceptor implements HandlerInterceptor {
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是 OPTIONS 请求，直接放行 (CORS 预检)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        
        // 2. 检查 Header 格式: Bearer <token>
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀
        
        // 3. 验证 Token 并获取 UserId
        Integer userId = JwtUtils.getUserIdFromToken(token);
        
        if (userId != null) {
            // 4. 查询数据库获取完整用户信息 (为了 UserContext)
            User user = userMapper.selectByPrimaryKey(Long.valueOf(userId));
            if (user != null) {
                UserContext.setCurrentUser(user);
                return true;
            }
        }
        
        // 验证失败
        response.setStatus(401);
        return false;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
