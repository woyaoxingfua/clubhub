package com.tlh.club_system.service.impl;

import com.tlh.club_system.entity.*;
import com.tlh.club_system.mapper.*;
import com.tlh.club_system.service.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 * @author AI Assistant
 * @version v0.11.0
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private ClubMapper clubMapper;

    @Resource
    private ClubMemberMapper clubMemberMapper;

    @Resource
    private EventMapper eventMapper;

    @Resource
    private EventSignupMapper eventSignupMapper;

    @Resource
    private FinanceMapper financeMapper;

    @Resource
    private RecruitApplicationMapper recruitApplicationMapper;

    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 1. 社团总数
        List<Club> clubs = clubMapper.selectAll();
        int totalClubs = clubs.size();
        int activeClubs = (int) clubs.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        overview.put("totalClubs", totalClubs);
        overview.put("activeClubs", activeClubs);

        // 2. 成员总数（统计所有社团的在社成员）
        int totalMembers = 0;
        for (Club club : clubs) {
            totalMembers += clubMemberMapper.countActiveMembers(club.getClubId());
        }
        overview.put("totalMembers", totalMembers);

        // 3. 活动总数
        List<Event> events = eventMapper.selectAll();
        overview.put("totalEvents", events.size());
        int approvedEvents = (int) events.stream().filter(e -> e.getAuditStatus() != null && e.getAuditStatus() == 3).count();
        overview.put("approvedEvents", approvedEvents);

        // 4. 本月新增成员（通过join_time判断）
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        int newMembersThisMonth = 0;
        for (Club club : clubs) {
            List<ClubMember> members = clubMemberMapper.selectFullRoster(club.getClubId());
            newMembersThisMonth += (int) members.stream()
                    .filter(m -> m.getJoinTime() != null && m.getJoinTime().isAfter(monthStart))
                    .count();
        }
        overview.put("newMembersThisMonth", newMembersThisMonth);

        // 5. 本月活动数
        int eventsThisMonth = (int) events.stream()
                .filter(e -> e.getCreateTime() != null && e.getCreateTime().isAfter(monthStart))
                .count();
        overview.put("eventsThisMonth", eventsThisMonth);

        // 6. 招新申请数
        List<RecruitApplication> applications = recruitApplicationMapper.selectAll();
        overview.put("totalApplications", applications != null ? applications.size() : 0);
        int pendingApplications = applications != null ? 
                (int) applications.stream().filter(a -> a.getStatus() != null && a.getStatus() == 0).count() : 0;
        overview.put("pendingApplications", pendingApplications);

        return overview;
    }

    @Override
    public Map<String, Object> getMemberGrowthTrend() {
        Map<String, Object> trend = new HashMap<>();
        
        List<String> months = new ArrayList<>();
        List<Integer> newMembers = new ArrayList<>();
        List<Integer> quitMembers = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        // 获取所有社团的所有成员
        List<Club> clubs = clubMapper.selectAll();
        List<ClubMember> allMembers = new ArrayList<>();
        for (Club club : clubs) {
            allMembers.addAll(clubMemberMapper.selectFullRoster(club.getClubId()));
        }

        // 统计近12个月的数据
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthStr = monthDate.format(formatter);
            months.add(monthStr);

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            // 该月新增成员
            int newCount = (int) allMembers.stream()
                    .filter(m -> m.getJoinTime() != null 
                            && m.getJoinTime().isAfter(monthStart) 
                            && m.getJoinTime().isBefore(monthEnd))
                    .count();
            newMembers.add(newCount);

            // 该月退出成员
            int quitCount = (int) allMembers.stream()
                    .filter(m -> m.getQuitTime() != null 
                            && m.getQuitTime().isAfter(monthStart) 
                            && m.getQuitTime().isBefore(monthEnd))
                    .count();
            quitMembers.add(quitCount);
        }

        trend.put("months", months);
        trend.put("newMembers", newMembers);
        trend.put("quitMembers", quitMembers);

        return trend;
    }

    @Override
    public Map<String, Object> getEventStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Event> events = eventMapper.selectAll();
        
        // 总数
        stats.put("total", events.size());

        // 按状态分组统计
        Map<Integer, Long> statusCount = events.stream()
                .filter(e -> e.getAuditStatus() != null)
                .collect(Collectors.groupingBy(Event::getAuditStatus, Collectors.counting()));

        stats.put("pending", statusCount.getOrDefault(0, 0L) + statusCount.getOrDefault(1, 0L) + statusCount.getOrDefault(2, 0L));
        stats.put("approved", statusCount.getOrDefault(3, 0L));
        stats.put("rejected", statusCount.getOrDefault(4, 0L));

        // 状态分布（用于饼图）
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        String[] statusNames = {"待提交", "待老师审", "待院系审", "已通过", "已驳回"};
        for (int i = 0; i <= 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", statusCount.getOrDefault(i, 0L));
            statusDistribution.add(item);
        }
        stats.put("statusDistribution", statusDistribution);

        // 近6个月活动数量趋势
        List<String> months = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthStr = monthDate.format(formatter);
            months.add(monthStr);

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            int count = (int) events.stream()
                    .filter(e -> e.getCreateTime() != null 
                            && e.getCreateTime().isAfter(monthStart) 
                            && e.getCreateTime().isBefore(monthEnd))
                    .count();
            counts.add(count);
        }
        stats.put("trendMonths", months);
        stats.put("trendCounts", counts);

        return stats;
    }

    @Override
    public Map<String, Object> getFinanceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Club> clubs = clubMapper.selectAll();
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        List<Map<String, Object>> clubFinanceList = new ArrayList<>();

        for (Club club : clubs) {
            Map<String, Object> summary = financeMapper.calculateSummary(club.getClubId());
            
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            
            if (summary != null) {
                Object incomeObj = summary.get("totalIncome");
                Object expenseObj = summary.get("totalExpense");
                if (incomeObj instanceof BigDecimal) {
                    income = (BigDecimal) incomeObj;
                } else if (incomeObj != null) {
                    income = new BigDecimal(incomeObj.toString());
                }
                if (expenseObj instanceof BigDecimal) {
                    expense = (BigDecimal) expenseObj;
                } else if (expenseObj != null) {
                    expense = new BigDecimal(expenseObj.toString());
                }
            }

            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(expense);

            Map<String, Object> clubFinance = new HashMap<>();
            clubFinance.put("clubId", club.getClubId());
            clubFinance.put("clubName", club.getClubName());
            clubFinance.put("income", income);
            clubFinance.put("expense", expense);
            clubFinance.put("balance", club.getBalance() != null ? club.getBalance() : BigDecimal.ZERO);
            clubFinanceList.add(clubFinance);
        }

        stats.put("totalIncome", totalIncome);
        stats.put("totalExpense", totalExpense);
        stats.put("netBalance", totalIncome.subtract(totalExpense));
        
        // 按收入排序
        clubFinanceList.sort((a, b) -> ((BigDecimal)b.get("income")).compareTo((BigDecimal)a.get("income")));
        stats.put("clubFinanceRanking", clubFinanceList.stream().limit(10).collect(Collectors.toList()));

        // 收支类型分布（用于饼图）
        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        Map<String, Object> incomeItem = new HashMap<>();
        incomeItem.put("name", "收入");
        incomeItem.put("value", totalIncome);
        typeDistribution.add(incomeItem);
        
        Map<String, Object> expenseItem = new HashMap<>();
        expenseItem.put("name", "支出");
        expenseItem.put("value", totalExpense);
        typeDistribution.add(expenseItem);
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    @Override
    public Map<String, Object> getRecruitStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<RecruitApplication> applications = recruitApplicationMapper.selectAll();
        if (applications == null) {
            applications = new ArrayList<>();
        }

        int total = applications.size();
        int pending = (int) applications.stream().filter(a -> a.getStatus() != null && a.getStatus() == 0).count();
        int accepted = (int) applications.stream().filter(a -> a.getStatus() != null && a.getStatus() == 1).count();
        int rejected = (int) applications.stream().filter(a -> a.getStatus() != null && a.getStatus() == 2).count();

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("accepted", accepted);
        stats.put("rejected", rejected);

        // 转化率
        double conversionRate = total > 0 ? (double) accepted / total * 100 : 0;
        stats.put("conversionRate", Math.round(conversionRate * 100) / 100.0);

        // 状态分布（用于饼图）
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        String[] statusNames = {"待审核", "已录用", "已拒绝"};
        int[] statusValues = {pending, accepted, rejected};
        for (int i = 0; i < 3; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", statusValues[i]);
            statusDistribution.add(item);
        }
        stats.put("statusDistribution", statusDistribution);

        return stats;
    }

    @Override
    public Map<String, Object> getClubActivityRanking() {
        Map<String, Object> ranking = new HashMap<>();

        List<Club> clubs = clubMapper.selectAll();
        List<Event> allEvents = eventMapper.selectAll();
        List<Map<String, Object>> clubRankingList = new ArrayList<>();

        for (Club club : clubs) {
            Map<String, Object> clubData = new HashMap<>();
            clubData.put("clubId", club.getClubId());
            clubData.put("clubName", club.getClubName());

            // 成员数
            int memberCount = clubMemberMapper.countActiveMembers(club.getClubId());
            clubData.put("memberCount", memberCount);

            // 活动数
            long eventCount = allEvents.stream()
                    .filter(e -> club.getClubId().equals(e.getClubId()) && e.getAuditStatus() != null && e.getAuditStatus() == 3)
                    .count();
            clubData.put("eventCount", eventCount);

            // 计算活跃度分数 (成员数*2 + 活动数*5)
            int activityScore = memberCount * 2 + (int) eventCount * 5;
            clubData.put("activityScore", activityScore);

            clubRankingList.add(clubData);
        }

        // 按活跃度分数排序
        clubRankingList.sort((a, b) -> ((Integer)b.get("activityScore")).compareTo((Integer)a.get("activityScore")));
        
        // 取前10名
        ranking.put("rankings", clubRankingList.stream().limit(10).collect(Collectors.toList()));

        // 提取排名数据用于柱状图
        List<String> clubNames = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < Math.min(10, clubRankingList.size()); i++) {
            Map<String, Object> club = clubRankingList.get(i);
            clubNames.add((String) club.get("clubName"));
            scores.add((Integer) club.get("activityScore"));
        }
        ranking.put("clubNames", clubNames);
        ranking.put("scores", scores);

        return ranking;
    }

    @Override
    public Map<String, Object> getClubMemberDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        List<Club> clubs = clubMapper.selectAll();
        List<Map<String, Object>> pieData = new ArrayList<>();

        for (Club club : clubs) {
            int memberCount = clubMemberMapper.countActiveMembers(club.getClubId());
            if (memberCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", club.getClubName());
                item.put("value", memberCount);
                pieData.add(item);
            }
        }

        // 按成员数排序
        pieData.sort((a, b) -> ((Integer)b.get("value")).compareTo((Integer)a.get("value")));
        
        distribution.put("pieData", pieData);

        return distribution;
    }

    @Override
    public Map<String, Object> getEventParticipationRate() {
        Map<String, Object> stats = new HashMap<>();

        List<Event> events = eventMapper.selectAll();
        List<EventSignup> signups = eventSignupMapper.selectAll();
        if (signups == null) {
            signups = new ArrayList<>();
        }

        List<String> months = new ArrayList<>();
        List<Integer> eventCounts = new ArrayList<>();
        List<Integer> signupCounts = new ArrayList<>();
        List<Double> participationRates = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthStr = monthDate.format(formatter);
            months.add(monthStr);

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            // 该月活动数
            List<Event> monthEvents = events.stream()
                    .filter(e -> e.getStartTime() != null 
                            && e.getStartTime().isAfter(monthStart) 
                            && e.getStartTime().isBefore(monthEnd)
                            && e.getAuditStatus() != null && e.getAuditStatus() == 3)
                    .collect(Collectors.toList());
            eventCounts.add(monthEvents.size());

            // 该月报名数
            final List<EventSignup> finalSignups = signups;
            int signupCount = 0;
            for (Event event : monthEvents) {
                signupCount += (int) finalSignups.stream()
                        .filter(s -> event.getEventId().equals(s.getEventId()))
                        .count();
            }
            signupCounts.add(signupCount);

            // 计算参与率（平均每个活动的报名人数）
            double rate = monthEvents.size() > 0 ? (double) signupCount / monthEvents.size() : 0;
            participationRates.add(Math.round(rate * 100) / 100.0);
        }

        stats.put("months", months);
        stats.put("eventCounts", eventCounts);
        stats.put("signupCounts", signupCounts);
        stats.put("participationRates", participationRates);

        return stats;
    }
}
