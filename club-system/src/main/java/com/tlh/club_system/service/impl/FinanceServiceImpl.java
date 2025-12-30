package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.Finance;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.FinanceMapper;
import com.tlh.club_system.service.FinanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务管理服务实现类
 */
@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private FinanceMapper financeMapper;
    
    @Resource
    private ClubMapper clubMapper;

    @Override
    public List<Finance> listFinances(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        
        // 如果未登录，返回空列表或抛出异常（这里选择返回空列表，或者也可以抛出异常由Controller捕获）
        if (currentUser == null) {
            // throw new RuntimeException("请先登录"); 
            // 为了防止前端直接红屏，这里也可以返回空列表，但Controller层最好拦截
            return java.util.Collections.emptyList();
        }

        // 如果指定了 clubId，直接查询该社团的
        if (clubId != null) {
            return financeMapper.selectByClubId(clubId);
        }
        
        // 如果是管理员，返回所有
        if (isAdmin(currentUser)) {
            return financeMapper.selectAllWithNames();
        }
        
        // 如果是社长，只返回自己管理的社团的财务记录
        List<Finance> allFinances = financeMapper.selectAllWithNames();
        List<Club> myClubs = clubMapper.selectByPresidentId(Long.valueOf(currentUser.getUserId()));
        List<Integer> myClubIds = myClubs.stream()
                .map(Club::getClubId)
                .collect(Collectors.toList());
        
        return allFinances.stream()
                .filter(f -> myClubIds.contains(f.getClubId()))
                .collect(Collectors.toList());
    }

    @Override
    public Result<String> addFinance(Finance finance) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 参数校验
        if (finance.getClubId() == null) {
            return Result.error("请选择社团");
        }
        if (finance.getType() == null) {
            return Result.error("请选择收支类型");
        }
        if (finance.getAmount() == null || finance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("金额必须大于0");
        }
        
        // 权限校验：管理员或该社团的社长
        if (!hasPermission(currentUser, finance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以添加财务记录");
        }
        
        // 设置默认值
        finance.setCreateTime(LocalDateTime.now());
        
        // 关键逻辑：如果是社长添加，强制设置为"审批中"；只有管理员可以设置其他状态
        if (isAdmin(currentUser)) {
            // 管理员可以自由设置状态，默认为通过
            if (finance.getStatus() == null) {
                finance.setStatus(1); // 管理员添加默认通过
            }
        } else {
            // 社长添加，强制设置为"审批中"
            finance.setStatus(0);
        }
        
        financeMapper.insertSelective(finance);
        return Result.success("财务记录添加成功" + (finance.getStatus() == 0 ? "，等待管理员审批" : ""));
    }

    @Override
    public Result<String> updateFinance(Finance finance) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 查询原记录
        Finance existingFinance = financeMapper.selectByPrimaryKey(Long.valueOf(finance.getFinanceId()));
        if (existingFinance == null) {
            return Result.error("财务记录不存在");
        }
        
        // 权限校验
        if (!hasPermission(currentUser, existingFinance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以修改财务记录");
        }
        
        // 关键逻辑：如果是社长修改，不允许修改 status 字段
        if (!isAdmin(currentUser)) {
            // 社长不能修改状态，保持原状态
            finance.setStatus(existingFinance.getStatus());
        }
        
        financeMapper.updateByPrimaryKeySelective(finance);
        return Result.success("财务记录更新成功");
    }

    @Override
    public Result<String> deleteFinance(Integer financeId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 查询原记录
        Finance existingFinance = financeMapper.selectByPrimaryKey(Long.valueOf(financeId));
        if (existingFinance == null) {
            return Result.error("财务记录不存在");
        }
        
        // 权限校验
        if (!hasPermission(currentUser, existingFinance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以删除财务记录");
        }
        
        financeMapper.deleteByPrimaryKey(Long.valueOf(financeId));
        return Result.success("财务记录已删除");
    }

    @Override
    public Map<String, Object> getFinanceSummary(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        
        // 权限校验
        if (clubId != null && !hasPermission(currentUser, clubId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "权限不足");
            return error;
        }
        
        Map<String, Object> summary = financeMapper.calculateSummary(clubId);
        
        // 计算余额
        BigDecimal totalIncome = (BigDecimal) summary.get("totalIncome");
        BigDecimal totalExpense = (BigDecimal) summary.get("totalExpense");
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        summary.put("balance", balance);
        return summary;
    }
    
    /**
     * 判断是否是管理员
     */
    private boolean isAdmin(User user) {
        if (user == null) return false;
        String role = user.getRoleKey();
        return "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
    }
    
    /**
     * 判断当前用户是否有权限操作该社团的财务
     */
    private boolean hasPermission(User user, Integer clubId) {
        if (user == null) return false;
        
        // 管理员有权限
        if (isAdmin(user)) {
            return true;
        }
        
        // 检查是否是该社团的社长
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club != null && club.getPresidentId() != null) {
            return club.getPresidentId().equals(user.getUserId());
        }
        
        return false;
    }
}
