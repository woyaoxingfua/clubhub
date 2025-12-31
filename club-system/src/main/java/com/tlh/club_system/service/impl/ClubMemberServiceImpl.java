package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.service.ClubMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClubMemberServiceImpl implements ClubMemberService {

    @Resource
    private ClubMemberMapper clubMemberMapper;

    @Resource
    private ClubMapper clubMapper;

    @Override
    public List<ClubMember> getFullRoster(Integer clubId) {
        return clubMemberMapper.selectFullRoster(clubId);
    }

    @Override
    public List<ClubMember> getMembersByStatus(Integer clubId, Integer status) {
        return status == null ? clubMemberMapper.selectFullRoster(clubId) : clubMemberMapper.selectByClubAndStatus(clubId, status);
    }

    @Override
    public List<ClubMember> getMembersByDepartment(Integer clubId, String department) {
        return clubMemberMapper.selectByDepartment(clubId, department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateMemberInfo(Integer memberId, String department, String position) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员不存在");
        }

        if (!hasManagePermission(member.getClubId())) {
            return Result.error("权限不足");
        }

        if (member.getStatus() == 0) {
            return Result.error("该成员已离社，无法修改信息");
        }
        
        User currentUser = UserContext.getCurrentUser();
        boolean isSystemAdmin = currentUser != null && ("SYS_ADMIN".equals(currentUser.getRoleKey()) || "DEPT_ADMIN".equals(currentUser.getRoleKey()));
        
        if ("社长".equals(position)) {
            if (!isSystemAdmin) {
                return Result.error("只有系统管理员才能设置社长职位");
            }
            
            Club club = clubMapper.selectByPrimaryKey(Long.valueOf(member.getClubId()));
            Integer oldPresidentId = club.getPresidentId();
            
            club.setPresidentId(member.getUserId());
            clubMapper.updateByPrimaryKeySelective(club);
            
            if (oldPresidentId != null && !oldPresidentId.equals(member.getUserId())) {
                ClubMember oldPresident = clubMemberMapper.selectByClubAndUser(member.getClubId(), Long.valueOf(oldPresidentId));
                if (oldPresident != null) {
                    clubMemberMapper.updateDepartmentAndPosition(oldPresident.getId(), "管理层", "副社长");
                    clubMemberMapper.assignRoleGroup(oldPresident.getId(), "admin");
                }
            }
        }

        clubMemberMapper.updateDepartmentAndPosition(memberId, department, position);
        clubMemberMapper.assignRoleGroup(memberId, determineRoleGroup(position));

        return Result.success("成员信息更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public Result<String> assignRoleGroup(Integer memberId, String roleGroup) {
        return Result.error("功能已废弃，权限组由职位自动决定");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> transferIn(Integer memberId) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) return Result.error("记录不存在");

        if (!hasManagePermission(member.getClubId())) return Result.error("权限不足");

        if (member.getStatus() == 1) return Result.error("该成员已在社");

        clubMemberMapper.reJoin(memberId);
        return Result.success("成员转入成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> quitMember(Integer memberId, String reason) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) return Result.error("记录不存在");

        if (!hasManagePermission(member.getClubId())) return Result.error("权限不足");

        if (member.getStatus() == 0) return Result.error("该成员已离社");

        if ("社长".equals(member.getPosition())) return Result.error("社长不能直接退出");

        clubMemberMapper.quitWithReason(memberId, reason == null || reason.trim().isEmpty() ? "个人原因" : reason);
        return Result.success("退出登记成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> dismissMember(Integer memberId, String reason) {
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) return Result.error("记录不存在");

        if (!hasManagePermission(member.getClubId())) return Result.error("权限不足");

        if (member.getStatus() == 0) return Result.error("该成员已离社");

        if ("社长".equals(member.getPosition())) return Result.error("无法开除社长");

        clubMemberMapper.dismiss(memberId, reason == null || reason.trim().isEmpty() ? "违反规定" : reason);
        return Result.success("开除操作已完成");
    }

    @Override
    public Map<String, Object> getMemberStatistics(Integer clubId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeMembers", clubMemberMapper.countActiveMembers(clubId));
        stats.put("departmentStats", clubMemberMapper.countByDepartment(clubId));

        List<ClubMember> allMembers = clubMemberMapper.selectFullRoster(clubId);
        stats.put("quitMembers", allMembers.stream().filter(m -> m.getStatus() == 0).count());
        stats.put("voluntaryQuit", allMembers.stream().filter(m -> m.getStatus() == 0 && Integer.valueOf(1).equals(m.getQuitType())).count());
        stats.put("dismissed", allMembers.stream().filter(m -> m.getStatus() == 0 && Integer.valueOf(2).equals(m.getQuitType())).count());

        return stats;
    }

    private boolean hasManagePermission(Integer clubId) {
        User user = UserContext.getCurrentUser();
        if (user == null) return false;
        
        String role = user.getRoleKey();
        if ("SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role)) return true;

        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        return club != null && club.getPresidentId() != null && club.getPresidentId().equals(user.getUserId());
    }

    private String determineRoleGroup(String position) {
        if (position == null) return "member";
        if (position.contains("社长") || position.contains("会长")) return "admin";
        if (position.contains("部长") || position.contains("副社长")) return "leader";
        return "member";
    }
    
    @Override
    public List<Map<String, Object>> getMyMemberships(Integer userId) {
        return clubMemberMapper.selectMyMemberships(userId);
    }
}
