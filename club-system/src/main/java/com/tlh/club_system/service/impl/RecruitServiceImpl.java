package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.RecruitApplication;
import com.tlh.club_system.entity.RecruitPlan;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.mapper.RecruitApplicationMapper;
import com.tlh.club_system.mapper.RecruitPlanMapper;
import com.tlh.club_system.mapper.UserMapper;
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
    private ClubMapper clubMapper;
    
    @Resource
    private UserMapper userMapper;

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
        if (plan.getClubId() == null) {
            return Result.error("关联社团不能为空");
        }
        
        if (!checkManagePermission(plan.getClubId())) {
             return Result.error("权限不足：只有管理员或社长可以发布招新计划");
        }

        plan.setStartTime(LocalDateTime.now());
        // 默认结束时间为1个月后
        if (plan.getEndTime() == null) {
            plan.setEndTime(LocalDateTime.now().plusMonths(1));
        }
        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        recruitPlanMapper.insertSelective(plan);
        return Result.success("招新计划发布成功");
    }

    @Override
    public Result<String> updatePlan(RecruitPlan plan) {
        if (plan.getPlanId() == null || plan.getClubId() == null) {
            return Result.error("参数错误");
        }
        
        if (!checkManagePermission(plan.getClubId())) {
            return Result.error("权限不足：只有管理员或社长可以修改招新计划");
        }
        
        recruitPlanMapper.updateByPrimaryKeySelective(plan);
        return Result.success("招新计划更新成功");
    }

    @Override
    public Result<String> deletePlan(Integer planId) {
        RecruitPlan plan = recruitPlanMapper.selectByPrimaryKey(Long.valueOf(planId));
        if (plan == null) {
            return Result.error("招新计划不存在");
        }
        
        if (!checkManagePermission(plan.getClubId())) {
            return Result.error("权限不足：只有管理员或社长可以删除招新计划");
        }
        
        recruitPlanMapper.deleteByPrimaryKey(Long.valueOf(planId));
        return Result.success("招新计划已删除");
    }

    @Override
    public Result<String> submitApplication(RecruitApplication application) {
        List<RecruitApplication> all = recruitApplicationMapper.selectAll();
        boolean exists = all.stream().anyMatch(a -> 
            a.getPlanId().equals(application.getPlanId()) && 
            a.getUserId().equals(application.getUserId()) &&
            a.getStatus() != 2
        );
        
        if (exists) {
            return Result.error("您已申请过该计划，请勿重复提交");
        }

        application.setApplyTime(LocalDateTime.now());
        application.setStatus(0);
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
        RecruitApplication app = recruitApplicationMapper.selectByPrimaryKey(Long.valueOf(appId));
        if (app == null) {
            return Result.error("申请不存在");
        }
        
        RecruitPlan plan = recruitPlanMapper.selectByPrimaryKey(Long.valueOf(app.getPlanId()));
        if (plan == null) {
             return Result.error("关联的招新计划不存在");
        }
        
        if (!checkManagePermission(plan.getClubId())) {
            return Result.error("权限不足：您无权审批该社团的申请");
        }
        
        if (app.getStatus() != 0) {
            return Result.error("该申请已审批，不可重复操作");
        }

        app.setStatus(status);
        recruitApplicationMapper.updateByPrimaryKeySelective(app);
        
        // 录用后自动入社
        if (status == 1) {
            ClubMember existMember = clubMemberMapper.selectByClubAndUser(plan.getClubId(), Long.valueOf(app.getUserId()));
            if (existMember == null) {
                ClubMember newMember = new ClubMember();
                newMember.setClubId(plan.getClubId());
                newMember.setUserId(app.getUserId());
                newMember.setJoinTime(LocalDateTime.now());
                newMember.setStatus(1);
                newMember.setPosition("成员"); 
                clubMemberMapper.insertSelective(newMember);
            } else if (existMember.getStatus() != null && existMember.getStatus() == 0) {
                clubMemberMapper.reJoin(existMember.getId());
            }
        }

        return Result.success("审批完成");
    }

    /**
     * 检查当前用户是否有管理社团的权限（管理员 或 该社团社长）
     */
    private boolean checkManagePermission(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) return false;
        
        String role = currentUser.getRoleKey();
        if ("SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role)) {
            return true;
        }
        
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        return club != null && club.getPresidentId() != null && club.getPresidentId().equals(currentUser.getUserId());
    }
}
