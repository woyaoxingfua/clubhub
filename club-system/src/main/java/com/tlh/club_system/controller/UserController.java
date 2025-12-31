package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.dto.LoginDTO;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.service.UserService;
import com.tlh.club_system.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.listAll());
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        String token = JwtUtils.generateToken(user.getUserId(), user.getUsername());
        user.setPassword(null);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }

        if (userService.isUsernameExists(user.getUsername())) {
            return Result.error("用户名已存在");
        }

        User registeredUser = userService.register(user);
        if (registeredUser == null) {
            return Result.error("注册失败，请稍后重试");
        }

        registeredUser.setPassword(null);
        return Result.success(registeredUser);
    }
}