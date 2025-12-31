package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.service.ClubMemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private ClubMapper clubMapper;

    @GetMapping("/list/{clubId}")
    public Result<List<ClubMember>> list(@PathVariable("clubId") Integer clubId) {
        return Result.success(clubMemberMapper.selectActiveMembers(clubId));
    }

    @PostMapping("/join")
    public Result<String> join(@RequestBody Map<String, Object> params) {
        Integer clubId = (Integer) params.get("clubId");
        Long userId = Long.valueOf(params.get("userId").toString());

        ClubMember exist = clubMemberMapper.selectByClubAndUser(clubId, userId);

        if (exist != null) {
            if (Integer.valueOf(1).equals(exist.getStatus())) {
                String pos = exist.getPosition();
                return Result.error("您已经是该社团的" + (pos != null ? pos : "成员") + "，请勿重复加入");
            } else {
                clubMemberMapper.reJoin(exist.getId());
                return Result.success("欢迎回来！您已重新加入社团");
            }
        }

        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId.intValue());
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(1);
        member.setPosition("成员");
        member.setDepartment("未分配");
        member.setRoleGroup("member");
        clubMemberMapper.insertSelective(member);
        return Result.success("恭喜！您已成功加入社团");
    }

    @DeleteMapping("/{id}")
    public Result<String> kick(@PathVariable("id") Integer id) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(id));
        if (member == null) return Result.error("成员记录不存在");
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        
        String role = currentUser.getRoleKey();
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role) || "CLUB_ADMIN".equals(role);
        
        if (!hasPermission) {
             Club club = clubMapper.selectByPrimaryKey(Long.valueOf(member.getClubId()));
             if (club != null && club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId())) {
                 hasPermission = true;
             }
        }
        
        if (!hasPermission) return Result.error("权限不足");
        
        if ("社长".equals(member.getPosition())) return Result.error("无法直接移除社长");
        if ("指导老师".equals(member.getPosition())) return Result.error("无法移除指导老师");

        clubMemberMapper.quit(id);
        return Result.success("成员已移除");
    }
    
    @GetMapping("/roster/{clubId}")
    public Result<List<ClubMember>> getFullRoster(@PathVariable("clubId") Integer clubId) {
        return Result.success(clubMemberService.getFullRoster(clubId));
    }
    
    @GetMapping("/filter/{clubId}")
    public Result<List<ClubMember>> filterMembers(
            @PathVariable("clubId") Integer clubId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "department", required = false) String department) {
        
        if (department != null && !department.isEmpty()) {
            return Result.success(clubMemberService.getMembersByDepartment(clubId, department));
        }
        return Result.success(clubMemberService.getMembersByStatus(clubId, status));
    }
    
    @PutMapping("/update")
    public Result<String> updateMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String department = (String) params.get("department");
        String position = (String) params.get("position");
        return clubMemberService.updateMemberInfo(memberId, department, position);
    }
    
    @PutMapping("/assign-role")
    public Result<String> assignRole(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String roleGroup = (String) params.get("roleGroup");
        return clubMemberService.assignRoleGroup(memberId, roleGroup);
    }
    
    @PostMapping("/transfer-in/{memberId}")
    public Result<String> transferIn(@PathVariable("memberId") Integer memberId) {
        return clubMemberService.transferIn(memberId);
    }
    
    @PutMapping("/quit")
    public Result<String> quitMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String reason = (String) params.get("reason");
        return clubMemberService.quitMember(memberId, reason);
    }
    
    @PutMapping("/dismiss")
    public Result<String> dismissMember(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        String reason = (String) params.get("reason");
        return clubMemberService.dismissMember(memberId, reason);
    }
    
    @GetMapping("/statistics/{clubId}")
    public Result<Map<String, Object>> getStatistics(@PathVariable("clubId") Integer clubId) {
        return Result.success(clubMemberService.getMemberStatistics(clubId));
    }
    
    @GetMapping("/my-memberships")
    public Result<List<Map<String, Object>>> getMyMemberships(@RequestParam("userId") Integer userId) {
        return Result.success(clubMemberService.getMyMemberships(userId));
    }
}
