package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Event;

public interface EventService {


    int deleteByPrimaryKey(Integer eventId);
    int insert(Event record);
    int insertSelective(Event record);
    Event selectByPrimaryKey(Integer eventId);
    int updateByPrimaryKeySelective(Event record);
    int updateByPrimaryKey(Event record);

    // --- 【修正】核心审批接口 (参数类型改为 Long) ---
    /**
     * 活动审批
     * @param eventId 活动ID (Long)
     * @param userId 审批人ID (Long)
     * @param pass 是否通过
     * @param reason 审批意见
     */
    Result<String> auditEvent(Long eventId, Long userId, boolean pass, String reason);

    /**
     * 提交活动审批
     * @param eventId 活动ID
     * @param userId 提交人ID
     */
    Result<String> submitEvent(Long eventId, Long userId);

    // 查询所有活动（管理员用）
    java.util.List<com.tlh.club_system.entity.Event> getEventList();
    
    // 查询已通过的公开活动（未登录用户）
    java.util.List<com.tlh.club_system.entity.Event> getPublicEventList();
    
    // 查询指导老师相关的活动
    java.util.List<com.tlh.club_system.entity.Event> getEventListForAdvisor(Integer advisorId);
    
    // 查询社长/社团管理员相关的活动
    java.util.List<com.tlh.club_system.entity.Event> getEventListForClubAdmin(Integer userId, Integer deptId);
    
    // 查询学生可见的活动（包括自己创建的活动）
    java.util.List<com.tlh.club_system.entity.Event> getEventListForStudent(Integer userId, Integer deptId);
}