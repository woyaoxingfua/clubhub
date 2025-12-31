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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private FinanceMapper financeMapper;
    
    @Resource
    private ClubMapper clubMapper;

    @Override
    public List<Finance> listFinances(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if (clubId != null) {
            return financeMapper.selectByClubId(clubId);
        }
        
        if (isAdmin(currentUser)) {
            return financeMapper.selectAllWithNames();
        }
        
        List<Club> myClubs = clubMapper.selectByPresidentId(Long.valueOf(currentUser.getUserId()));
        List<Integer> myClubIds = myClubs.stream()
                .map(Club::getClubId)
                .collect(Collectors.toList());
        
        return financeMapper.selectAllWithNames().stream()
                .filter(f -> myClubIds.contains(f.getClubId()))
                .collect(Collectors.toList());
    }

    @Override
    public Result<String> addFinance(Finance finance) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        if (finance.getClubId() == null) {
            return Result.error("请选择社团");
        }
        if (finance.getType() == null) {
            return Result.error("请选择收支类型");
        }
        if (finance.getAmount() == null || finance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("金额必须大于0");
        }
        
        if (!hasPermission(currentUser, finance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以添加财务记录");
        }
        
        finance.setCreateTime(LocalDateTime.now());
        
        if (isAdmin(currentUser)) {
            if (finance.getStatus() == null) {
                finance.setStatus(1);
            }
        } else {
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
        
        Finance existingFinance = financeMapper.selectByPrimaryKey(Long.valueOf(finance.getFinanceId()));
        if (existingFinance == null) {
            return Result.error("财务记录不存在");
        }
        
        if (!hasPermission(currentUser, existingFinance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以修改财务记录");
        }
        
        if (!isAdmin(currentUser)) {
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
        
        Finance existingFinance = financeMapper.selectByPrimaryKey(Long.valueOf(financeId));
        if (existingFinance == null) {
            return Result.error("财务记录不存在");
        }
        
        if (!hasPermission(currentUser, existingFinance.getClubId())) {
            return Result.error("权限不足：只有管理员或该社团社长可以删除财务记录");
        }
        
        financeMapper.deleteByPrimaryKey(Long.valueOf(financeId));
        return Result.success("财务记录已删除");
    }

    @Override
    public Map<String, Object> getFinanceSummary(Integer clubId) {
        User currentUser = UserContext.getCurrentUser();
        
        if (clubId != null && !hasPermission(currentUser, clubId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "权限不足");
            return error;
        }
        
        Map<String, Object> summary = financeMapper.calculateSummary(clubId);
        
        BigDecimal totalIncome = (BigDecimal) summary.get("totalIncome");
        BigDecimal totalExpense = (BigDecimal) summary.get("totalExpense");
        
        // Handle null values from DB
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        summary.put("balance", balance);
        return summary;
    }
    
    private boolean isAdmin(User user) {
        if (user == null) return false;
        String role = user.getRoleKey();
        return "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
    }
    
    private boolean hasPermission(User user, Integer clubId) {
        if (user == null) return false;
        if (isAdmin(user)) return true;
        
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        return club != null && club.getPresidentId() != null && club.getPresidentId().equals(user.getUserId());
    }
}
