package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.RecruitApplication;
import com.tlh.club_system.entity.RecruitPlan;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.mapper.RecruitApplicationMapper;
import com.tlh.club_system.mapper.RecruitPlanMapper;
import com.tlh.club_system.service.RecruitService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecruitServiceImpl implements RecruitService {

    @Resource
    private RecruitPlanMapper recruitPlanMapper;

    @Resource
    private RecruitApplicationMapper recruitApplicationMapper;

    @Resource
    private ClubMemberMapper clubMemberMapper;
    
    @Resource
    private com.tlh.club_system.mapper.ClubMapper clubMapper;
    
    @Resource
    private com.tlh.club_system.mapper.UserMapper userMapper;

    @Override
    public List<RecruitPlan> listPlans(Integer clubId) {
        return recruitPlanMapper.selectAll(); 
    }

    @Override
    public RecruitPlan getPlan(Integer planId) {
        return recruitPlanMapper.selectByPrimaryKey(Long.valueOf(planId));
    }

    @Override
    public Result<String> createPlan(RecruitPlan plan) {
        // 1. 基础参数校验
        if (plan.getClubId() == null) {
            return Result.error("关联社团不能为空");
        }
        
        // 2. 权限校验
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 调试日志：打印当前用户信息
        System.out.println("[DEBUG] 招新计划 - 当前用户ID: " + currentUser.getUserId() + ", 角色: " + currentUser.getRoleKey());
        
        String role = currentUser.getRoleKey();
        
        // 管理员直接放行
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
        
        // 如果不是管理员，检查是否为该社团的社长（注意：社长的roleKey是STUDENT）
        if (!hasPermission) {
            com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(plan.getClubId()));
            if (club == null) {
                return Result.error("社团不存在");
            }
            // 调试日志
            System.out.println("[DEBUG] 招新计划 - 社团ID: " + club.getClubId() + ", 社长ID: " + club.getPresidentId());
            
            // 检查是否为社长（不再检查roleKey，直接看presidentId）
            if (club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId())) {
                hasPermission = true;
                System.out.println("[DEBUG] 招新计划 - 用户是社长，允许操作");
            } else {
                System.out.println("[DEBUG] 招新计划 - 用户不是社长，拒绝操作");
            }
        }
        
        if (!hasPermission) {
            return Result.error("权限不足：只有管理员或社长可以发布招新计划");
        }

        plan.setStartTime(LocalDateTime.now());
        // 默认结束时间为1个月后，防止DB非空错误
        if (plan.getEndTime() == null) {
            plan.setEndTime(LocalDateTime.now().plusMonths(1));
        }
        if (plan.getStatus() == null) {
            plan.setStatus(1); // 默认进行中
        }
        recruitPlanMapper.insertSelective(plan);
        return Result.success("招新计划发布成功");
    }

    @Override
    public Result<String> updatePlan(RecruitPlan plan) {
        if (plan.getPlanId() == null || plan.getClubId() == null) {
            return Result.error("参数错误");
        }
        
        // 权限校验：管理员或该社团社长
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        String role = currentUser.getRoleKey();
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
        
        if (!hasPermission) {
            // 检查是否是该社团的社长
            com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(plan.getClubId()));
            if (club != null && club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId())) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            return Result.error("权限不足：只有管理员或社长可以修改招新计划");
        }
        
        recruitPlanMapper.updateByPrimaryKeySelective(plan);
        return Result.success("招新计划更新成功");
    }

    @Override
    public Result<String> deletePlan(Integer planId) {
        // 先查询招新计划以获取clubId
        RecruitPlan plan = recruitPlanMapper.selectByPrimaryKey(Long.valueOf(planId));
        if (plan == null) {
            return Result.error("招新计划不存在");
        }
        
        // 权限校验：管理员或该社团社长
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        String role = currentUser.getRoleKey();
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
        
        if (!hasPermission) {
            // 检查是否是该社团的社长
            com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(plan.getClubId()));
            if (club != null && club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId())) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            return Result.error("权限不足：只有管理员或社长可以删除招新计划");
        }
        
        recruitPlanMapper.deleteByPrimaryKey(Long.valueOf(planId));
        return Result.success("招新计划已删除");
    }

    @Override
    public Result<String> submitApplication(RecruitApplication application) {
        // 1. 检查是否重复申请
        List<RecruitApplication> all = recruitApplicationMapper.selectAll();
        boolean exists = all.stream().anyMatch(a -> 
            a.getPlanId().equals(application.getPlanId()) && 
            a.getUserId().equals(application.getUserId()) &&
            a.getStatus() != 2 // 除非被拒绝，否则不能重复申 (或者被拒绝也不能? 通常被拒后可以再试，但这里简单起见，暂不限制被拒后的重申，只限制待审核和已通过)
        );
        
        if (exists) {
            return Result.error("您已申请过该计划，请勿重复提交");
        }

        application.setApplyTime(LocalDateTime.now());
        application.setStatus(0); // 待审核
        recruitApplicationMapper.insertSelective(application);
        return Result.success("申请已提交");
    }

    @Override
    public List<RecruitApplication> listApplications(Integer planId) {
        return recruitApplicationMapper.selectAllWithUserName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> auditApplication(Integer appId, Integer status) {
        // 1. 查询申请
        RecruitApplication app = recruitApplicationMapper.selectByPrimaryKey(Long.valueOf(appId));
        if (app == null) {
            return Result.error("申请不存在");
        }
        
        // 2. 权限校验
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 查出对应的计划和社团
        RecruitPlan plan = recruitPlanMapper.selectByPrimaryKey(Long.valueOf(app.getPlanId()));
        if (plan == null) {
             return Result.error("关联的招新计划不存在");
        }
        com.tlh.club_system.entity.Club club = clubMapper.selectByPrimaryKey(Long.valueOf(plan.getClubId()));
        
        // 校验：必须是系统管理员、院系管理员 或者 该社团的社长
        String role = currentUser.getRoleKey();
        boolean hasPermission = "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
        
        // 如果不是管理员，检查是否为该社团的社长（不再检查CLUB_ADMIN角色）
        if (!hasPermission && club != null && club.getPresidentId() != null) {
            if (club.getPresidentId().equals(currentUser.getUserId())) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            return Result.error("权限不足：您无权审批该社团的申请");
        }
        
        // 如果已经是最终状态，不允许修改
        if (app.getStatus() != 0) {
            return Result.error("该申请已审批，不可重复操作");
        }

        // 更新申请状态
        app.setStatus(status);
        recruitApplicationMapper.updateByPrimaryKeySelective(app);
        
        // 如果是录用 (status == 1)，则自动加入社团成员表
        if (status == 1) {
            // 查是否已经是成员
            ClubMember existMember = clubMemberMapper.selectByClubAndUser(plan.getClubId(), Long.valueOf(app.getUserId()));
            if (existMember == null) {
                ClubMember newMember = new ClubMember();
                newMember.setClubId(plan.getClubId());
                newMember.setUserId(app.getUserId());
                newMember.setJoinTime(LocalDateTime.now());
                newMember.setStatus(1); // 1-正常
                // 默认职位
                newMember.setPosition("成员"); 
                clubMemberMapper.insertSelective(newMember);
            } else if (existMember.getStatus() != null && existMember.getStatus() == 0) {
                // 如果是退社状态，恢复
                clubMemberMapper.reJoin(existMember.getId());
            }
        }

        return Result.success("审批完成");
    }
}
