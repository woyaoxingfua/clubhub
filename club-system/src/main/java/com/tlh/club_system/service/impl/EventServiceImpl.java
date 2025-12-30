package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.EventMapper;
import com.tlh.club_system.mapper.UserMapper;
import com.tlh.club_system.service.EventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private com.tlh.club_system.mapper.ClubMapper clubMapper;

    // --- 基础 Mapper 委托 (保持原样，假设 Mapper 还是 Integer 主键) ---
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

    // ==========================================
    //  【新增】提交审批接口
    // ==========================================
    @Override
    public Result<String> submitEvent(Long eventId, Long userId) {
        // 1. 查询活动
        Event event = eventMapper.selectByPrimaryKey(eventId);
        if (event == null) {
            return Result.error("活动不存在");
        }

        // 2. 权限校验：只有创建者可以提交
        if (!event.getCreateBy().equals(userId)) {
            return Result.error("无权操作：只能提交自己创建的活动");
        }

        // 3. 状态校验：只有状态为0(待提交)才可以提交
        if (event.getAuditStatus() != 0) {
            return Result.error("操作失败：当前状态无法提交审批");
        }

        // 4. 更新状态 0 -> 1
        event.setAuditStatus(1);
        eventMapper.updateByPrimaryKeySelective(event);
        return Result.success("提交成功，请等待指导老师审批");
    }

    // ==========================================
    //  【重点修正】多级审批流逻辑
    // ==========================================
    @Override
    public Result<String> auditEvent(Long eventId, Long userId, boolean pass, String reason) {

        // 1. 转换 ID 类型并查询 (注意：如果你数据库主键是 int，这里转回 int 查库)
        Event event = eventMapper.selectByPrimaryKey((long) eventId.intValue());
        if (event == null) {
            return Result.error("活动不存在");
        }

        User currentUser = userMapper.selectByPrimaryKey((long) userId.intValue());
        if (currentUser == null) {
            return Result.error("审批人不存在");
        }

        // 2. 获取角色 (修正：使用 getRoleKey)
        // 假设 User 实体里是 private String roleKey;
        String roleKey = currentUser.getRoleKey();

        // 3. 获取当前状态
        // 0待提交, 1待指导老师审, 2待院系审, 3已通过, 4已驳回
        Integer currentStatus = event.getAuditStatus();

        // --- 分支 A: 驳回操作 ---
        if (!pass) {
            event.setAuditStatus(4); // 修正：4 代表已驳回
            event.setAuditRemark(reason); // 修正：使用 setAuditRemark
            eventMapper.updateByPrimaryKeySelective(event);
            return Result.success("操作成功：活动已驳回");
        }

        // --- 分支 B: 通过操作 (状态机) ---

        // 场景 1: 指导老师审批 (ADVISOR)
        if ("ADVISOR".equals(roleKey)) {
            // 只有状态为 1 (待指导老师审) 时，指导老师才能审
            if (currentStatus == 1) {
                event.setAuditStatus(2); // 1 -> 2 (推给院系)
                event.setAuditRemark("指导老师审批通过：" + (reason == null ? "同意" : reason));
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("指导老师审批通过，已提交至院系");
            } else {
                return Result.error("操作失败：当前状态不需要指导老师审批");
            }
        }

        // 场景 2: 院系管理员 (DEPT_ADMIN) 或 系统管理员 (SYS_ADMIN)
        else if ("DEPT_ADMIN".equals(roleKey) || "SYS_ADMIN".equals(roleKey)) {
            // 只有状态为 2 (待院系审) 时，院系才能审
            if (currentStatus == 2) {
                event.setAuditStatus(3); // 2 -> 3 (已通过)
                // 修正：删除了 setIsPublic，只更新备注
                event.setAuditRemark("院系审批通过：" + (reason == null ? "同意" : reason));
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("院系审批通过，活动正式发布");
            }
            // 补充：是否允许院系越级审批状态 1 (待指导老师审)? 通常为了灵活性可以允许
            else if (currentStatus == 1) {
                event.setAuditStatus(3); // 越级直接通过
                event.setAuditRemark("院系越级审批通过");
                eventMapper.updateByPrimaryKeySelective(event);
                return Result.success("越级审批通过");
            }
            else {
                return Result.error("操作失败：当前状态无法进行终审");
            }
        }

        else {
            return Result.error("权限不足：您不是有效的审批角色");
        }
    }

    @Override
    public java.util.List<com.tlh.club_system.entity.Event> getEventList() {
        // 查询所有活动
        return eventMapper.selectAll();
    }
    
    @Override
    public java.util.List<com.tlh.club_system.entity.Event> getPublicEventList() {
        // 查询已通过的活动（audit_status = 3）
        return eventMapper.selectAll().stream()
            .filter(event -> event.getAuditStatus() != null && event.getAuditStatus() == 3)
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public java.util.List<com.tlh.club_system.entity.Event> getEventListForAdvisor(Integer advisorId) {
        // 指导老师查看：
        // 1. 自己指导的社团的活动（所有状态）
        // 2. 已通过的活动
        java.util.List<com.tlh.club_system.entity.Event> allEvents = eventMapper.selectAll();
        java.util.Set<Integer> advisorClubIds = new java.util.HashSet<>();
        
        // 查找指导老师负责的社团
        java.util.List<com.tlh.club_system.entity.Club> allClubs = clubMapper.selectAll();
        for (com.tlh.club_system.entity.Club club : allClubs) {
            if (club.getAdvisorId() != null && club.getAdvisorId().equals(advisorId)) {
                advisorClubIds.add(club.getClubId());
            }
        }
        
        return allEvents.stream()
            .filter(event -> {
                // 是自己指导的社团的活动，或者是已通过的活动
                return (event.getClubId() != null && advisorClubIds.contains(event.getClubId()))
                    || (event.getAuditStatus() != null && event.getAuditStatus() == 3);
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public java.util.List<com.tlh.club_system.entity.Event> getEventListForClubAdmin(Integer userId, Integer deptId) {
        // 社长/社团管理员查看：
        // 1. 自己是社长的社团的所有活动
        // 2. 已通过的面向自己院系的活动
        // 3. 已通过的全校活动
        java.util.List<com.tlh.club_system.entity.Event> allEvents = eventMapper.selectAll();
        java.util.Set<Integer> presidentClubIds = new java.util.HashSet<>();
        
        // 查找用户作为社长的社团
        java.util.List<com.tlh.club_system.entity.Club> allClubs = clubMapper.selectAll();
        for (com.tlh.club_system.entity.Club club : allClubs) {
            if (club.getPresidentId() != null && club.getPresidentId().equals(userId)) {
                presidentClubIds.add(club.getClubId());
            }
        }
        
        return allEvents.stream()
            .filter(event -> {
                // 自己社团的所有活动
                if (event.getClubId() != null && presidentClubIds.contains(event.getClubId())) {
                    return true;
                }
                // 已通过的活动
                if (event.getAuditStatus() != null && event.getAuditStatus() == 3) {
                    // 全校范围的活动
                    if (event.getTargetType() != null && event.getTargetType() == 0) {
                        return true;
                    }
                    // 本院系的活动
                    if (event.getTargetType() != null && event.getTargetType() == 1 
                        && event.getTargetDeptId() != null && event.getTargetDeptId().equals(deptId)) {
                        return true;
                    }
                }
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public java.util.List<com.tlh.club_system.entity.Event> getEventListForStudent(Integer userId, Integer deptId) {
        // 普通学生查看：
        // 1. 自己创庻的所有活动（包括待提交、待审核等）
        // 2. 已通过的全校活动
        // 3. 已通过的面向自己院系的活动
        return eventMapper.selectAll().stream()
            .filter(event -> {
                // 自己创庻的活动（任何状态）
                if (event.getCreateBy() != null && event.getCreateBy().equals(userId.longValue())) {
                    return true;
                }
                // 必须是已通过的活动
                if (event.getAuditStatus() == null || event.getAuditStatus() != 3) {
                    return false;
                }
                // 全校范围
                if (event.getTargetType() != null && event.getTargetType() == 0) {
                    return true;
                }
                // 本院系范围
                if (event.getTargetType() != null && event.getTargetType() == 1 
                    && event.getTargetDeptId() != null && event.getTargetDeptId().equals(deptId)) {
                    return true;
                }
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}