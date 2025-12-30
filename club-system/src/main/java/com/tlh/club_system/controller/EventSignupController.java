package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.EventSignup;
import com.tlh.club_system.service.EventSignupService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event/signup")
@CrossOrigin
public class EventSignupController {

    @Resource
    private EventSignupService eventSignupService;

    // 1. 用户报名
    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Integer> params) {
        Integer eventId = params.get("eventId");
        Integer userId = params.get("userId");
        if (eventId == null || userId == null) {
            return Result.error("参数不完整");
        }
        return eventSignupService.signup(eventId, userId);
    }

    // 2. 取消报名
    @DeleteMapping("/{signupId}")
    public Result<String> cancel(@PathVariable("signupId") Integer signupId) {
        return eventSignupService.cancel(signupId);
    }

    // 3. 查询某活动的报名名单 (管理员/社长用)
    @GetMapping("/list/event/{eventId}")
    public Result<List<EventSignup>> listByEvent(@PathVariable("eventId") Integer eventId) {
        return Result.success(eventSignupService.listByEvent(eventId));
    }

    // 4. 查询某用户的报名记录 (我的活动)
    @GetMapping("/list/user/{userId}")
    public Result<List<EventSignup>> listByUser(@PathVariable("userId") Integer userId) {
        return Result.success(eventSignupService.listByUser(userId));
    }

    // 5. 审核/签到 (管理员用)
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Integer> params) {
        Integer signupId = params.get("signupId");
        Integer status = params.get("status");
        return eventSignupService.updateStatus(signupId, status);
    }
    
    // 6. 检查状态
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam("eventId") Integer eventId, @RequestParam("userId") Integer userId) {
        return Result.success(eventSignupService.checkHasSignedUp(eventId, userId));
    }
}
