package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.EventMapper;
import com.tlh.club_system.mapper.UserMapper;
import com.tlh.club_system.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ClubMapper clubMapper;

    @Override
    public int deleteByPrimaryKey(Integer eventId) { return eventMapper.deleteByPrimaryKey(Long.valueOf(eventId)); }
    @Override
    public int insert(Event record) { return eventMapper.insert(record); }
    @Override
    public int insertSelective(Event record) { return eventMapper.insertSelective(record); }
    @Override
    public Event selectByPrimaryKey(Integer eventId) { return eventMapper.selectByPrimaryKey(Long.valueOf(eventId)); }
    @Override
    public int updateByPrimaryKeySelective(Event record) { return eventMapper.updateByPrimaryKeySelective(record); }
    @Override
    public int updateByPrimaryKey(Event record) { return eventMapper.updateByPrimaryKey(record); }

    @Override
    public Result<String> submitEvent(Long eventId, Long userId) {
        Event event = eventMapper.selectByPrimaryKey(eventId);
        if (event == null) {
            return Result.error("活动不存在");
        }

        if (!event.getCreateBy().equals(userId)) {
            return Result.error("无权操作：只能提交自己创建的活动");
        }

        if (event.getAuditStatus() != 0) {
            return Result.error("操作失败：当前状态无法提交审批");
        }

        event.setAuditStatus(1);
        eventMapper.updateByPrimaryKeySelective(event);
        return Result.success("提交成功，请等待指导老师审批");
    }

    @Override
    public Result<String> auditEvent(Long eventId, Long userId, boolean pass, String reason) {
        Event event = eventMapper.selectByPrimaryKey(eventId);
        if (event == null) {
            return Result.error("活动不存在");
        }

        User currentUser = userMapper.selectByPrimaryKey(userId);
        if (currentUser == null) {
            return Result.error("审批人不存在");
        }

        String roleKey = currentUser.getRoleKey();
        Integer currentStatus = event.getAuditStatus();

        if (!pass) {
            event.setAuditStatus(4);
            event.setAuditRemark(reason);
            eventMapper.updateByPrimaryKeySelective(event);
            return Result.success("操作成功：活动已驳回");
        }

        // 指导老师审批
        if ("ADVISOR".equals(roleKey)) {
            if (currentStatus == 1) {
                event.setAuditStatus(2);
                event.setAuditRemark("指导老师审批通过：" + (reason == null ? "同意" : reason));
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("指导老师审批通过，已提交至院系");
            } else {
                return Result.error("操作失败：当前状态不需要指导老师审批");
            }
        }

        // 院系或系统管理员审批
        else if ("DEPT_ADMIN".equals(roleKey) || "SYS_ADMIN".equals(roleKey)) {
            if (currentStatus == 2) {
                event.setAuditStatus(3);
                event.setAuditRemark("院系审批通过：" + (reason == null ? "同意" : reason));
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("院系审批通过，活动正式发布");
            } else if (currentStatus == 1) {
                // 允许越级审批
                event.setAuditStatus(3);
                event.setAuditRemark("院系越级审批通过");
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("越级审批通过");
            } else {
                return Result.error("操作失败：当前状态无法进行终审");
            }
        }

        return Result.error("权限不足：您不是有效的审批角色");
    }

    @Override
    public List<Event> getEventList() {
        return eventMapper.selectAll();
    }

    @Override
    public List<Event> getPublicEventList() {
        return eventMapper.selectAll().stream()
            .filter(this::isPubliclyVisible)
            .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventListForAdvisor(Integer advisorId) {
        List<Event> allEvents = eventMapper.selectAll();
        Set<Integer> advisorClubIds = clubMapper.selectAll().stream()
                .filter(c -> c.getAdvisorId() != null && c.getAdvisorId().equals(advisorId))
                .map(Club::getClubId)
                .collect(Collectors.toSet());

        return allEvents.stream()
            .filter(event -> (event.getClubId() != null && advisorClubIds.contains(event.getClubId())) 
                          || isPubliclyVisible(event))
            .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventListForClubAdmin(Integer userId, Integer deptId) {
        List<Event> allEvents = eventMapper.selectAll();
        Set<Integer> presidentClubIds = clubMapper.selectAll().stream()
                .filter(c -> c.getPresidentId() != null && c.getPresidentId().equals(userId))
                .map(Club::getClubId)
                .collect(Collectors.toSet());

        return allEvents.stream()
            .filter(event -> (event.getClubId() != null && presidentClubIds.contains(event.getClubId()))
                          || isVisibleToStudent(event, deptId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventListForStudent(Integer userId, Integer deptId) {
        return eventMapper.selectAll().stream()
            .filter(event -> (event.getCreateBy() != null && event.getCreateBy().equals(userId.longValue()))
                          || isVisibleToStudent(event, deptId))
            .collect(Collectors.toList());
    }
    
    // --- Helper Methods ---

    private boolean isPubliclyVisible(Event event) {
        return event.getAuditStatus() != null && event.getAuditStatus() == 3;
    }

    private boolean isVisibleToStudent(Event event, Integer userDeptId) {
        if (!isPubliclyVisible(event)) return false;
        
        // 全校范围
        if (event.getTargetType() != null && event.getTargetType() == 0) return true;
        
        // 本院系范围
        return event.getTargetType() != null && event.getTargetType() == 1
                && event.getTargetDeptId() != null && event.getTargetDeptId().equals(userDeptId);
    }
}