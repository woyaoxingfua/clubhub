package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/event")
@CrossOrigin
public class EventController {

    @Resource
    private EventService eventService;

    // 1. 列表接口 - 修改：基于当前用户权限过滤
    @GetMapping("/list")
    public Result<java.util.List<Event>> list() {
        // 获取当前登录用户
        User currentUser = UserContext.getCurrentUser();
        
        // 如果没有登录用户信息，返回所有已通过的活动
        if (currentUser == null) {
            return Result.success(eventService.getPublicEventList());
        }
        
        // 根据用户角色返回不同的活动列表
        String roleKey = currentUser.getRoleKey();
        Integer userId = currentUser.getUserId();
        Integer deptId = currentUser.getDeptId();
        
        // 系统管理员和院系管理员：查看所有活动
        if ("SYS_ADMIN".equals(roleKey) || "DEPT_ADMIN".equals(roleKey)) {
            return Result.success(eventService.getEventList());
        }
        
        // 指导老师：查看需要审批的活动 + 已通过的活动
        if ("ADVISOR".equals(roleKey)) {
            return Result.success(eventService.getEventListForAdvisor(userId));
        }
        
        // 社长/社团管理员：查看自己社团的活动 + 相关的公开活动
        if ("CLUB_ADMIN".equals(roleKey)) {
            return Result.success(eventService.getEventListForClubAdmin(userId, deptId));
        }
        
        // 普通学生(包括社长)：查看已通过的活动 + 自己创建的活动
        return Result.success(eventService.getEventListForStudent(userId, deptId));
    }

    // 2. 详情接口
    @GetMapping("/detail/{id}")
    public Result<Event> getInfo(@PathVariable("id") Integer id) {
        return Result.success(eventService.selectByPrimaryKey(id));
    }

    @Resource
    private com.tlh.club_system.mapper.ClubMapper clubMapper;

    // 3. 发起申请 (修正：权限校验 + 目标范围)
    @PostMapping("/add")
    public Result<String> add(@RequestBody Event event) {
        // 获取当前登录用户
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 调试日志：打印当前用户信息
        System.out.println("[DEBUG] 活动申请 - 当前用户ID: " + currentUser.getUserId() + ", 角色: " + currentUser.getRoleKey());
        
        // 设置创建人为当前用户
        event.setCreateBy(currentUser.getUserId().longValue());
        
        // 1. 基础校验
        if (event.getClubId() == null) {
            return Result.error("必须选择主办社团");
        }
        
        // 2. 权限校验：必须是该社团的社长
        com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(event.getClubId()));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        // 调试日志
        System.out.println("[DEBUG] 活动申请 - 社团ID: " + club.getClubId() + ", 社长ID: " + club.getPresidentId());
        
        // 比较社长ID与当前用户ID
        if (!club.getPresidentId().equals(currentUser.getUserId())) {
            System.out.println("[DEBUG] 活动申请 - 用户不是社长，拒绝操作");
            return Result.error("您不是该社团的社长，无权发起活动申请");
        }
        
        System.out.println("[DEBUG] 活动申请 - 用户是社长，允许操作");
        
        // 3. 校验目标范围
        if (event.getTargetType() == null) {
            return Result.error("必须选择活动面向范围（全校或指定院系）");
        }
        if (event.getTargetType() == 1 && event.getTargetDeptId() == null) {
            return Result.error("选择院系范围时，必须指定目标院系");
        }
        if (event.getTargetType() == 0) {
            event.setTargetDeptId(null); // 全校范围，清空院系ID
        }

        event.setAuditStatus(0); // 初始状态
        eventService.insertSelective(event);
        return Result.success("申请已提交");
    }

    // 4. 审批接口
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Object> params) {
        Long eventId = Long.valueOf(params.get("eventId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        Boolean pass = (Boolean) params.get("pass");
        String reason = (String) params.get("reason");
        return eventService.auditEvent(eventId, userId, pass, reason);
    }

    // [新增] 提交审批接口
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Map<String, Object> params) {
        Long eventId = Long.valueOf(params.get("eventId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        return eventService.submitEvent(eventId, userId);
    }

    // 5. 撤销/删除接口 (修正：增加权限校验)
    // DELETE /api/event/123?userId=456
    @DeleteMapping("/{eventId}")
    public Result<String> delete(@PathVariable("eventId") Integer eventId,
                                 @RequestParam("userId") Long userId) {

        // A. 先查出这个活动
        Event event = eventService.selectByPrimaryKey(eventId);
        if (event == null) {
            return Result.error("活动不存在");
        }

        // B. 严谨的权限判断 (尽善尽美！)
        // 数据库存的 Long，比较时用 equals
        if (event.getCreateBy() == null || !event.getCreateBy().equals(userId)) {
            return Result.error("操作失败：您只能撤销自己创建的活动！");
        }

        // C. 只有状态为 0 (待提交) 才能撤销
        if (event.getAuditStatus() != 0) {
            return Result.error("操作失败：当前状态无法撤销");
        }

        // D. 执行删除
        eventService.deleteByPrimaryKey(eventId);
        return Result.success("活动申请已成功撤销");
    }
    
    /**
     * 活动日历接口 - 返回所有已发布的活动（用于日历视图）
     */
    @GetMapping("/calendar")
    public Result<java.util.List<Event>> calendar(
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end) {
        // 查询所有已通过审批的活动
        java.util.List<Event> events = eventService.getPublicEventList();
        
        // 如果提供了时间范围，进行过滤
        if (start != null && end != null) {
            try {
                java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(start);
                java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(end);
                
                events = events.stream()
                    .filter(e -> e.getStartTime() != null && 
                                !e.getStartTime().isBefore(startTime) && 
                                !e.getStartTime().isAfter(endTime))
                    .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                // 日期解析失败，返回所有活动
            }
        }
        
        return Result.success(events);
    }
}