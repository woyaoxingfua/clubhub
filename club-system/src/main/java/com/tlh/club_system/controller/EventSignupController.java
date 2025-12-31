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

    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Integer> params) {
        Integer eventId = params.get("eventId");
        Integer userId = params.get("userId");
        if (eventId == null || userId == null) {
            return Result.error("参数不完整");
        }
        return eventSignupService.signup(eventId, userId);
    }

    @DeleteMapping("/{signupId}")
    public Result<String> cancel(@PathVariable("signupId") Integer signupId) {
        return eventSignupService.cancel(signupId);
    }

    @GetMapping("/list/event/{eventId}")
    public Result<List<EventSignup>> listByEvent(@PathVariable("eventId") Integer eventId) {
        return Result.success(eventSignupService.listByEvent(eventId));
    }

    @GetMapping("/list/user/{userId}")
    public Result<List<EventSignup>> listByUser(@PathVariable("userId") Integer userId) {
        return Result.success(eventSignupService.listByUser(userId));
    }

    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Integer> params) {
        Integer signupId = params.get("signupId");
        Integer status = params.get("status");
        return eventSignupService.updateStatus(signupId, status);
    }
    
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam("eventId") Integer eventId, @RequestParam("userId") Integer userId) {
        return Result.success(eventSignupService.checkHasSignedUp(eventId, userId));
    }
}
