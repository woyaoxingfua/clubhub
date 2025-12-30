package com.tlh.club_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 密钥 (实际开发中应放在配置文件或环境变量中，长度至少32字节)
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token 有效期 (24小时)
    private static final long EXPIRATION = 86400000L;

    /**
     * 生成 Token
     */
    public static String generateToken(Integer userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token 获取 UserId
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            return null; // 解析失败或过期
        }
    }
    
    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
