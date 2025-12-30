package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.ClubMember;

import java.util.List;
import java.util.Map;

/**
 * 社团成员管理服务接口
 */
public interface ClubMemberService {
    
    /**
     * 获取成员花名册（完整列表，包含所有状态）
     * @param clubId 社团ID
     * @return 成员列表
     */
    List<ClubMember> getFullRoster(Integer clubId);
    
    /**
     * 按状态筛选成员
     * @param clubId 社团ID
     * @param status 状态（1=在社, 0=已离社, null=全部）
     * @return 成员列表
     */
    List<ClubMember> getMembersByStatus(Integer clubId, Integer status);
    
    /**
     * 按部门筛选成员
     * @param clubId 社团ID
     * @param department 部门名称
     * @return 成员列表
     */
    List<ClubMember> getMembersByDepartment(Integer clubId, String department);
    
    /**
     * 更新成员信息（部门和职务）
     * @param memberId 成员ID
     * @param department 新部门
     * @param position 新职务
     * @return 操作结果
     */
    Result<String> updateMemberInfo(Integer memberId, String department, String position);
    
    /**
     * 分配权限组
     * @param memberId 成员ID
     * @param roleGroup 权限组（admin/leader/member）
     * @return 操作结果
     */
    Result<String> assignRoleGroup(Integer memberId, String roleGroup);
    
    /**
     * 成员转入（重新激活已退社的成员）
     * @param memberId 成员ID
     * @return 操作结果
     */
    Result<String> transferIn(Integer memberId);
    
    /**
     * 成员主动退出
     * @param memberId 成员ID
     * @param reason 退出原因
     * @return 操作结果
     */
    Result<String> quitMember(Integer memberId, String reason);
    
    /**
     * 开除成员
     * @param memberId 成员ID
     * @param reason 开除原因
     * @return 操作结果
     */
    Result<String> dismissMember(Integer memberId, String reason);
    
    /**
     * 获取社团成员统计信息
     * @param clubId 社团ID
     * @return 统计信息
     */
    Map<String, Object> getMemberStatistics(Integer clubId);
    
    /**
     * 获取用户加入的所有社团（用于通讯录）
     * @param userId 用户ID
     * @return 社团列表
     */
    List<Map<String, Object>> getMyMemberships(Integer userId);
}
