package com.tlh.club_system.config;

import com.tlh.club_system.interceptor.UserInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 静态资源映射和拦截器配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Resource
    private UserInterceptor userInterceptor;
    
    // 文件上传基础路径（与FileController保持一致）
    private static final String UPLOAD_BASE_DIR = System.getProperty("user.home") + "/club-system-uploads/";
    
    /**
     * 配置全局CORS跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 允许携带凭证
                .maxAge(3600);  // 预检请求缓存时间
        
        System.out.println("========================================");
        System.out.println("CORS跨域配置完成:");
        System.out.println("  允许所有域名访问");
        System.out.println("  允许携带Cookie和认证信息");
        System.out.println("========================================");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到用户目录下的 club-system-uploads/ 目录
        // 这样前端访问 http://localhost:8080/club_system_war_exploded/uploads/logos/xxx.png
        // 就会自动映射到 C:\Users\Lenovo\club-system-uploads\logos\xxx.png
        
        String uploadLocation = "file:" + UPLOAD_BASE_DIR;
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
        
        System.out.println("========================================");
        System.out.println("静态资源映射配置完成:");
        System.out.println("  URL路径: /uploads/**");
        System.out.println("  物理路径: " + uploadLocation);
        System.out.println("  - logos/     -> Logo图片目录");
        System.out.println("  - files/     -> 普通文件目录");
        System.out.println("  - resumes/   -> 简历文件目录");
        System.out.println("========================================");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",      // 登录接口
                        "/api/auth/register",   // 注册接口
                        "/api/dept/list",       // 院系列表（注册需要）
                        "/api/club/list",       // 社团列表（公开）
                        "/api/club/detail/**",  // 社团详情（公开）
                        "/api/event/list",      // 活动列表（公开）
                        "/api/notice/list"      // 公告列表（公开）
                );
        
        System.out.println("========================================");
        System.out.println("拦截器配置完成:");
        System.out.println("  公开接口（无需登录）:");
        System.out.println("    - /api/auth/login");
        System.out.println("    - /api/auth/register");
        System.out.println("    - /api/club/list");
        System.out.println("    - /api/club/detail/**");
        System.out.println("    - /api/event/list");
        System.out.println("    - /api/notice/list");
        System.out.println("  其他接口需要登录");
        System.out.println("========================================");
    }
}
