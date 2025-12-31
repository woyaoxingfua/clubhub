package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.EventSignup;
import com.tlh.club_system.mapper.EventSignupMapper;
import com.tlh.club_system.service.EventSignupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventSignupServiceImpl implements EventSignupService {

    @Resource
    private EventSignupMapper eventSignupMapper;

    @Override
    public Result<String> signup(Integer eventId, Integer userId) {
        if (checkHasSignedUp(eventId, userId)) {
            return Result.error("您已报名过该活动，请勿重复报名");
        }

        EventSignup signup = new EventSignup();
        signup.setEventId(eventId);
        signup.setUserId(userId);
        signup.setSignupTime(LocalDateTime.now());
        signup.setStatus(0);

        eventSignupMapper.insertSelective(signup);
        return Result.success("报名成功");
    }

    @Override
    public Result<String> cancel(Integer signupId) {
        eventSignupMapper.deleteByPrimaryKey(Long.valueOf(signupId));
        return Result.success("已取消报名");
    }

    @Override
    public List<EventSignup> listByEvent(Integer eventId) {
        List<EventSignup> all = eventSignupMapper.selectAll();
        return all.stream()
                .filter(s -> s.getEventId().equals(eventId))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventSignup> listByUser(Integer userId) {
        List<EventSignup> all = eventSignupMapper.selectAll();
        return all.stream()
                .filter(s -> s.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Result<String> updateStatus(Integer signupId, Integer status) {
        EventSignup signup = eventSignupMapper.selectByPrimaryKey(Long.valueOf(signupId));
        if (signup == null) {
            return Result.error("记录不存在");
        }
        signup.setStatus(status);
        eventSignupMapper.updateByPrimaryKeySelective(signup);
        return Result.success("状态更新成功");
    }

    @Override
    public boolean checkHasSignedUp(Integer eventId, Integer userId) {
        List<EventSignup> userSignups = listByUser(userId);
        return userSignups.stream().anyMatch(s -> s.getEventId().equals(eventId));
    }
}
