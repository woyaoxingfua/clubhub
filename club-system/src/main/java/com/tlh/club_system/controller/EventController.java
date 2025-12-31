package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/event")
@CrossOrigin
public class EventController {

    @Resource
    private EventService eventService;

    @Resource
    private ClubMapper clubMapper;

    @GetMapping("/list")
    public Result<List<Event>> list() {
        User currentUser = UserContext.getCurrentUser();
        
        if (currentUser == null) {
            return Result.success(eventService.getPublicEventList());
        }
        
        String roleKey = currentUser.getRoleKey();
        Integer userId = currentUser.getUserId();
        Integer deptId = currentUser.getDeptId();
        
        if ("SYS_ADMIN".equals(roleKey) || "DEPT_ADMIN".equals(roleKey)) {
            return Result.success(eventService.getEventList());
        }
        
        if ("ADVISOR".equals(roleKey)) {
            return Result.success(eventService.getEventListForAdvisor(userId));
        }
        
        if ("CLUB_ADMIN".equals(roleKey)) {
            return Result.success(eventService.getEventListForClubAdmin(userId, deptId));
        }
        
        return Result.success(eventService.getEventListForStudent(userId, deptId));
    }

    @GetMapping("/detail/{id}")
    public Result<Event> getInfo(@PathVariable("id") Integer id) {
        return Result.success(eventService.selectByPrimaryKey(id));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Event event) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        event.setCreateBy(currentUser.getUserId().longValue());
        
        if (event.getClubId() == null) {
            return Result.error("必须选择主办社团");
        }
        
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(event.getClubId()));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        if (!club.getPresidentId().equals(currentUser.getUserId())) {
            return Result.error("您不是该社团的社长，无权发起活动申请");
        }
        
        if (event.getTargetType() == null) {
            return Result.error("必须选择活动面向范围（全校或指定院系）");
        }
        if (event.getTargetType() == 1 && event.getTargetDeptId() == null) {
            return Result.error("选择院系范围时，必须指定目标院系");
        }
        if (event.getTargetType() == 0) {
            event.setTargetDeptId(null);
        }

        event.setAuditStatus(0);
        eventService.insertSelective(event);
        return Result.success("申请已提交");
    }

    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Object> params) {
        Long eventId = Long.valueOf(params.get("eventId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        Boolean pass = (Boolean) params.get("pass");
        String reason = (String) params.get("reason");
        return eventService.auditEvent(eventId, userId, pass, reason);
    }

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Map<String, Object> params) {
        Long eventId = Long.valueOf(params.get("eventId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        return eventService.submitEvent(eventId, userId);
    }

    @DeleteMapping("/{eventId}")
    public Result<String> delete(@PathVariable("eventId") Integer eventId,
                                 @RequestParam("userId") Long userId) {
        Event event = eventService.selectByPrimaryKey(eventId);
        if (event == null) {
            return Result.error("活动不存在");
        }

        if (event.getCreateBy() == null || !event.getCreateBy().equals(userId)) {
            return Result.error("操作失败：您只能撤销自己创建的活动！");
        }

        if (event.getAuditStatus() != 0) {
            return Result.error("操作失败：当前状态无法撤销");
        }

        eventService.deleteByPrimaryKey(eventId);
        return Result.success("活动申请已成功撤销");
    }
    
    @GetMapping("/calendar")
    public Result<List<Event>> calendar(
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end) {
        List<Event> events = eventService.getPublicEventList();
        
        if (start != null && end != null) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(start);
                LocalDateTime endTime = LocalDateTime.parse(end);
                
                events = events.stream()
                    .filter(e -> e.getStartTime() != null && 
                                !e.getStartTime().isBefore(startTime) && 
                                !e.getStartTime().isAfter(endTime))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // Ignore parse errors, return all events
            }
        }
        
        return Result.success(events);
    }
}