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

/**
 * 社团成员管理服务实现类
 */
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
        if (status == null) {
            return clubMemberMapper.selectFullRoster(clubId);
        }
        return clubMemberMapper.selectByClubAndStatus(clubId, status);
    }

    @Override
    public List<ClubMember> getMembersByDepartment(Integer clubId, String department) {
        return clubMemberMapper.selectByDepartment(clubId, department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateMemberInfo(Integer memberId, String department, String position) {
        // 1. 查询成员是否存在
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员不存在");
        }

        // 2. 权限校验
        Result<String> permissionCheck = checkManagePermission(member.getClubId());
        if (permissionCheck.getCode() != 200) {
            return permissionCheck;
        }

        // 3. 业务规则校验
        if (member.getStatus() == 0) {
            return Result.error("该成员已离社，无法修改信息");
        }
        
        // 4. 检查是否设置为"社长"职位
        User currentUser = UserContext.getCurrentUser();
        String roleKey = currentUser != null ? currentUser.getRoleKey() : null;
        boolean isSystemAdmin = "SYS_ADMIN".equals(roleKey) || "DEPT_ADMIN".equals(roleKey);
        
        if ("社长".equals(position)) {
            if (!isSystemAdmin) {
                return Result.error("只有系统管理员才能设置社长职位");
            }
            
            // 管理员设置社长时，需要同步更新 tb_club.president_id
            Club club = clubMapper.selectByPrimaryKey(Long.valueOf(member.getClubId()));
            Integer oldPresidentId = club.getPresidentId();
            
            // 更新社团的 president_id
            club.setPresidentId(member.getUserId());
            clubMapper.updateByPrimaryKeySelective(club);
            
            // 将旧社长降级为副社长（如果存在）
            if (oldPresidentId != null && !oldPresidentId.equals(member.getUserId())) {
                ClubMember oldPresident = clubMemberMapper.selectByClubAndUser(
                    member.getClubId(), Long.valueOf(oldPresidentId)
                );
                if (oldPresident != null) {
                    clubMemberMapper.updateDepartmentAndPosition(
                        oldPresident.getId(), "管理层", "副社长"
                    );
                    clubMemberMapper.assignRoleGroup(oldPresident.getId(), "admin");
                }
            }
        }

        // 5. 更新成员信息
        clubMemberMapper.updateDepartmentAndPosition(memberId, department, position);

        // 6. 根据职务自动调整权限组（强制绑定）
        String roleGroup = determineRoleGroup(position);
        clubMemberMapper.assignRoleGroup(memberId, roleGroup);

        return Result.success("成员信息更新成功");
    }

    /**
     * 独立的权限组分配功能已废弃
     * 权限组现在完全由职位决定，不允许单独修改
     * 修改职位时会自动更新权限组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public Result<String> assignRoleGroup(Integer memberId, String roleGroup) {
        return Result.error("权限组分配功能已废弃，权限组由职位自动决定。请通过修改职位来调整权限。");
    }

    /*
    // 原有的权限组分配逻辑（已废弃）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> assignRoleGroup(Integer memberId, String roleGroup) {
        // 1. 验证权限组参数
        if (!isValidRoleGroup(roleGroup)) {
            return Result.error("无效的权限组，仅支持：admin, leader, member");
        }

        // 2. 查询成员是否存在
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员不存在");
        }

        // 3. 权限校验（只有管理员和社长可以分配权限组）
        Result<String> permissionCheck = checkManagePermission(member.getClubId());
        if (permissionCheck.getCode() != 200) {
            return permissionCheck;
        }

        // 4. 业务规则校验
        if (member.getStatus() == 0) {
            return Result.error("该成员已离社，无法分配权限组");
        }

        // 5. 不能降低社长的权限
        if ("社长".equals(member.getPosition()) && !"admin".equals(roleGroup)) {
            return Result.error("不能降低社长的权限等级");
        }

        // 6. 更新权限组
        clubMemberMapper.assignRoleGroup(memberId, roleGroup);

        return Result.success("权限组分配成功");
    }
    */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> transferIn(Integer memberId) {
        // 1. 查询成员记录
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员记录不存在");
        }

        // 2. 权限校验
        Result<String> permissionCheck = checkManagePermission(member.getClubId());
        if (permissionCheck.getCode() != 200) {
            return permissionCheck;
        }

        // 3. 业务规则校验
        if (member.getStatus() == 1) {
            return Result.error("该成员已在社，无需转入");
        }

        // 4. 重新激活成员（清除退出/开除信息）
        clubMemberMapper.reJoin(memberId);

        return Result.success("成员转入成功，欢迎回归！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> quitMember(Integer memberId, String reason) {
        // 1. 查询成员记录
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员记录不存在");
        }

        // 2. 权限校验
        Result<String> permissionCheck = checkManagePermission(member.getClubId());
        if (permissionCheck.getCode() != 200) {
            return permissionCheck;
        }

        // 3. 业务规则校验
        if (member.getStatus() == 0) {
            return Result.error("该成员已离社");
        }

        if ("社长".equals(member.getPosition())) {
            return Result.error("社长不能退出，请先转让社长职位");
        }

        // 4. 记录退出信息
        if (reason == null || reason.trim().isEmpty()) {
            reason = "个人原因";
        }
        clubMemberMapper.quitWithReason(memberId, reason);

        return Result.success("成员退出登记成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> dismissMember(Integer memberId, String reason) {
        // 1. 查询成员记录
        ClubMember member = clubMemberMapper.selectByPrimaryKey(Long.valueOf(memberId));
        if (member == null) {
            return Result.error("成员记录不存在");
        }

        // 2. 权限校验（开除操作需要更高权限）
        Result<String> permissionCheck = checkManagePermission(member.getClubId());
        if (permissionCheck.getCode() != 200) {
            return permissionCheck;
        }

        // 3. 业务规则校验
        if (member.getStatus() == 0) {
            return Result.error("该成员已离社");
        }

        if ("社长".equals(member.getPosition())) {
            return Result.error("无法开除社长");
        }

        // 4. 记录开除信息
        if (reason == null || reason.trim().isEmpty()) {
            reason = "违反社团规定";
        }
        clubMemberMapper.dismiss(memberId, reason);

        return Result.success("成员开除操作已完成");
    }

    @Override
    public Map<String, Object> getMemberStatistics(Integer clubId) {
        Map<String, Object> statistics = new HashMap<>();

        // 1. 在社成员总数
        int activeCount = clubMemberMapper.countActiveMembers(clubId);
        statistics.put("activeMembers", activeCount);

        // 2. 各部门人数统计
        List<Map<String, Object>> departmentStats = clubMemberMapper.countByDepartment(clubId);
        statistics.put("departmentStats", departmentStats);

        // 3. 所有成员列表（用于统计已离社人数）
        List<ClubMember> allMembers = clubMemberMapper.selectFullRoster(clubId);
        long quitCount = allMembers.stream().filter(m -> m.getStatus() == 0).count();
        statistics.put("quitMembers", quitCount);

        // 4. 主动退出和被开除的人数
        long voluntaryQuit = allMembers.stream()
                .filter(m -> m.getStatus() == 0 && m.getQuitType() != null && m.getQuitType() == 1)
                .count();
        long dismissed = allMembers.stream()
                .filter(m -> m.getStatus() == 0 && m.getQuitType() != null && m.getQuitType() == 2)
                .count();
        statistics.put("voluntaryQuit", voluntaryQuit);
        statistics.put("dismissed", dismissed);

        return statistics;
    }

    /**
     * 检查当前用户是否有管理该社团的权限
     */
    private Result<String> checkManagePermission(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        String roleKey = currentUser.getRoleKey();

        // 系统管理员和院系管理员有权限
        if ("SYS_ADMIN".equals(roleKey) || "DEPT_ADMIN".equals(roleKey)) {
            return Result.success();
        }

        // 检查是否为该社团的社长
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club != null && club.getPresidentId() != null &&
                club.getPresidentId().equals(currentUser.getUserId())) {
            return Result.success();
        }

        return Result.error("权限不足：只有管理员或社长可以管理成员");
    }

    /**
     * 根据职务确定权限组
     */
    private String determineRoleGroup(String position) {
        if (position == null) {
            return "member";
        }

        if (position.contains("社长") || position.contains("会长")) {
            return "admin";
        }

        if (position.contains("部长") || position.contains("副社长")) {
            return "leader";
        }

        return "member";
    }

    /**
     * 验证权限组是否合法
     */
    private boolean isValidRoleGroup(String roleGroup) {
        return "admin".equals(roleGroup) ||
                "leader".equals(roleGroup) ||
                "member".equals(roleGroup);
    }
    
    @Override
    public List<Map<String, Object>> getMyMemberships(Integer userId) {
        return clubMemberMapper.selectMyMemberships(userId);
    }
}
