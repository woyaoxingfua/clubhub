package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.service.ClubMemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@CrossOrigin
public class ClubMemberController {

    @Resource
    private ClubMemberMapper clubMemberMapper;
    
    @Resource
    private ClubMemberService clubMemberService;
    
    @Resource
    private com.tlh.club_system.mapper.ClubMapper clubMapper;

    // ==================== 原有接口 ====================
    
    /**
     * 获取某社团的【在社】成员列表（保留原有接口，用于抽屉展示）
     */
    @GetMapping("/list/{clubId}")
    public Result<List<ClubMember>> list(@PathVariable("clubId") Integer clubId) {
        return Result.success(clubMemberMapper.selectActiveMembers(clubId));
    }

    /**
     * 学生申请加入
     */
    @PostMapping("/join")
    public Result<String> join(@RequestBody Map<String, Object> params) {
        Integer clubId = (Integer) params.get("clubId");
        Long userId = Long.valueOf(params.get("userId").toString());

        ClubMember exist = clubMemberMapper.selectByClubAndUser(clubId, userId);

        if (exist != null) {
            if (Integer.valueOf(1).equals(exist.getStatus())) {
                String position = exist.getPosition();
                if ("社长".equals(position) || "副社长".equals(position)) {
                    return Result.error("您已经是该社团的" + position + "，无需申请加入");
                } else if ("管理员".equals(position)) {
                    return Result.error("您已经是该社团的管理员，无需申请加入");
                } else {
                    return Result.error("您已经是该社团成员，请勿重复加入");
                }
            } else {
                clubMemberMapper.reJoin(exist.getId());
                return Result.success("欢迎回来！您已重新加入社团");
            }
        } else {
            ClubMember member = new ClubMember();
            member.setClubId(clubId);
            member.setUserId(userId.intValue());
            member.setJoinTime(java.time.LocalDateTime.now());
            member.setStatus(1);
            member.setPosition("成员");
            member.setDepartment("未分配");
            member.setRoleGroup("member");
            clubMemberMapper.insertSelective(member);
            return Result.success("恭喜！您已成功加入社团");
        }
    }

    /**
     * 踢出成员 (软删除)
     */
    @DeleteMapping("/{id}")
    public Result<String> kick(@PathVariable("id") Integer id) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(id));
        if (member == null) {
            return Result.error("成员记录不存在");
        }
        
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        String role = currentUser.getRoleKey();
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role) || "CLUB_ADMIN".equals(role);
        
        if (!hasPermission) {
             com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(member.getClubId()));
             if (club != null && club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId())) {
                 hasPermission = true;
             }
        }
        
        if (!hasPermission) {
            return Result.error("权限不足：您无权操作该社团成员");
        }
        
        if ("社长".equals(member.getPosition())) {
            return Result.error("无法移除社长，请联系管理员进行职位变更");
        }
        if ("指导老师".equals(member.getPosition())) {
             return Result.error("无法移除指导老师");
        }

        clubMemberMapper.quit(id);
        return Result.success("成员已移除");
    }
    
    // ==================== 新增接口 ====================
    
    /**
     * 获取成员完整花名册（包含所有状态）
     */
    @GetMapping("/roster/{clubId}")
    public Result<List<ClubMember>> getFullRoster(@PathVariable("clubId") Integer clubId) {
        List<ClubMember> roster = clubMemberService.getFullRoster(clubId);
        return Result.success(roster);
    }
    
    /**
     * 按条件筛选成员
     */
    @GetMapping("/filter/{clubId}")
    public Result<List<ClubMember>> filterMembers(
            @PathVariable("clubId") Integer clubId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "department", required = false) String department) {
        
        List<ClubMember> members;
        
        if (department != null && !department.isEmpty()) {
            members = clubMemberService.getMembersByDepartment(clubId, department);
        } else {
            members = clubMemberService.getMembersByStatus(clubId, status);
        }
        
        return Result.success(members);
    }
    
    /**
     * 更新成员信息（部门和职务）
     */
    @PutMapping("/update")
    public Result<String> updateMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String department = (String) params.get("department");
        String position = (String) params.get("position");
        
        return clubMemberService.updateMemberInfo(memberId, department, position);
    }
    
    /**
     * 分配权限组
     */
    @PutMapping("/assign-role")
    public Result<String> assignRole(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String roleGroup = (String) params.get("roleGroup");
        
        return clubMemberService.assignRoleGroup(memberId, roleGroup);
    }
    
    /**
     * 成员转入（重新激活）
     */
    @PostMapping("/transfer-in/{memberId}")
    public Result<String> transferIn(@PathVariable("memberId") Integer memberId) {
        return clubMemberService.transferIn(memberId);
    }
    
    /**
     * 成员退出
     */
    @PutMapping("/quit")
    public Result<String> quitMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String reason = (String) params.get("reason");
        
        return clubMemberService.quitMember(memberId, reason);
    }
    
    /**
     * 开除成员
     */
    @PutMapping("/dismiss")
    public Result<String> dismissMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String reason = (String) params.get("reason");
        
        return clubMemberService.dismissMember(memberId, reason);
    }
    
    /**
     * 获取成员统计信息
     */
    @GetMapping("/statistics/{clubId}")
    public Result<Map<String, Object>> getStatistics(@PathVariable("clubId") Integer clubId) {
        Map<String, Object> stats = clubMemberService.getMemberStatistics(clubId);
        return Result.success(stats);
    }
    
    /**
     * 获取我加入的所有社团（用于通讯录）
     */
    @GetMapping("/my-memberships")
    public Result<List<Map<String, Object>>> getMyMemberships(@RequestParam("userId") Integer userId) {
        List<Map<String, Object>> clubs = clubMemberService.getMyMemberships(userId);
        return Result.success(clubs);
    }
}
