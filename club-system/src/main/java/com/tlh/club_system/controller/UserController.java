package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.dto.LoginDTO;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.service.UserService;
import com.tlh.club_system.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // 区分一下，专门做认证
@CrossOrigin // 允许 Vue 跨域调用
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public Result<java.util.List<User>> list() {
        return Result.success(userService.listAll());
    }

    // 登录接口
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 1. 调用 Service 查询用户
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 2. 生成 JWT Token
        String token = JwtUtils.generateToken(user.getUserId(), user.getUsername());

        // 3. 构建返回数据 (Token + 用户信息)
        user.setPassword(null); // 隐藏密码
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 1. 验证必填字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }

        // 2. 检查用户名是否已存在
        if (userService.isUsernameExists(user.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 3. 调用 Service 注册用户
        User registeredUser = userService.register(user);
        if (registeredUser == null) {
            return Result.error("注册失败，请稍后重试");
        }

        // 4. 注册成功，返回用户信息（不含密码）
        registeredUser.setPassword(null);
        return Result.success(registeredUser);
    }
}