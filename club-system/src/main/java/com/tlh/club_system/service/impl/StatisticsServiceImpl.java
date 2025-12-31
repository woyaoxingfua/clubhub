package com.tlh.club_system.service.impl;

import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.EventSignup;
import com.tlh.club_system.entity.RecruitApplication;
import com.tlh.club_system.mapper.*;
import com.tlh.club_system.service.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

        List<Club> clubs = clubMapper.selectAll();
        overview.put("totalClubs", clubs.size());
        overview.put("activeClubs", (int) clubs.stream().filter(c -> Integer.valueOf(1).equals(c.getStatus())).count());

        int totalMembers = clubs.stream()
                .mapToInt(c -> clubMemberMapper.countActiveMembers(c.getClubId()))
                .sum();
        overview.put("totalMembers", totalMembers);

        List<Event> events = eventMapper.selectAll();
        overview.put("totalEvents", events.size());
        overview.put("approvedEvents", (int) events.stream().filter(e -> Integer.valueOf(3).equals(e.getAuditStatus())).count());

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        int newMembersThisMonth = clubs.stream()
                .mapToInt(c -> (int) clubMemberMapper.selectFullRoster(c.getClubId()).stream()
                        .filter(m -> m.getJoinTime() != null && m.getJoinTime().isAfter(monthStart))
                        .count())
                .sum();
        overview.put("newMembersThisMonth", newMembersThisMonth);

        overview.put("eventsThisMonth", (int) events.stream()
                .filter(e -> e.getCreateTime() != null && e.getCreateTime().isAfter(monthStart))
                .count());

        List<RecruitApplication> applications = recruitApplicationMapper.selectAll();
        overview.put("totalApplications", applications != null ? applications.size() : 0);
        overview.put("pendingApplications", applications != null ? 
                (int) applications.stream().filter(a -> Integer.valueOf(0).equals(a.getStatus())).count() : 0);

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

        List<ClubMember> allMembers = clubMapper.selectAll().stream()
                .flatMap(c -> clubMemberMapper.selectFullRoster(c.getClubId()).stream())
                .collect(Collectors.toList());

        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthStr = monthDate.format(formatter);
            months.add(monthStr);

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            newMembers.add((int) allMembers.stream()
                    .filter(m -> m.getJoinTime() != null && m.getJoinTime().isAfter(monthStart) && m.getJoinTime().isBefore(monthEnd))
                    .count());

            quitMembers.add((int) allMembers.stream()
                    .filter(m -> m.getQuitTime() != null && m.getQuitTime().isAfter(monthStart) && m.getQuitTime().isBefore(monthEnd))
                    .count());
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
        stats.put("total", events.size());

        Map<Integer, Long> statusCount = events.stream()
                .filter(e -> e.getAuditStatus() != null)
                .collect(Collectors.groupingBy(Event::getAuditStatus, Collectors.counting()));

        stats.put("pending", statusCount.getOrDefault(0, 0L) + statusCount.getOrDefault(1, 0L) + statusCount.getOrDefault(2, 0L));
        stats.put("approved", statusCount.getOrDefault(3, 0L));
        stats.put("rejected", statusCount.getOrDefault(4, 0L));

        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        String[] statusNames = {"待提交", "待老师审", "待院系审", "已通过", "已驳回"};
        for (int i = 0; i <= 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", statusCount.getOrDefault(i, 0L));
            statusDistribution.add(item);
        }
        stats.put("statusDistribution", statusDistribution);

        List<String> months = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            months.add(monthDate.format(formatter));

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            counts.add((int) events.stream()
                    .filter(e -> e.getCreateTime() != null && e.getCreateTime().isAfter(monthStart) && e.getCreateTime().isBefore(monthEnd))
                    .count());
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
                income = convertToBigDecimal(summary.get("totalIncome"));
                expense = convertToBigDecimal(summary.get("totalExpense"));
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
        
        clubFinanceList.sort((a, b) -> ((BigDecimal)b.get("income")).compareTo((BigDecimal)a.get("income")));
        stats.put("clubFinanceRanking", clubFinanceList.stream().limit(10).collect(Collectors.toList()));

        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        typeDistribution.add(createDistributionItem("收入", totalIncome));
        typeDistribution.add(createDistributionItem("支出", totalExpense));
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        return new BigDecimal(obj.toString());
    }

    private Map<String, Object> createDistributionItem(String name, Object value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    @Override
    public Map<String, Object> getRecruitStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<RecruitApplication> applications = recruitApplicationMapper.selectAll();
        if (applications == null) applications = Collections.emptyList();

        int total = applications.size();
        int pending = (int) applications.stream().filter(a -> Integer.valueOf(0).equals(a.getStatus())).count();
        int accepted = (int) applications.stream().filter(a -> Integer.valueOf(1).equals(a.getStatus())).count();
        int rejected = (int) applications.stream().filter(a -> Integer.valueOf(2).equals(a.getStatus())).count();

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("accepted", accepted);
        stats.put("rejected", rejected);
        stats.put("conversionRate", total > 0 ? Math.round((double) accepted / total * 10000) / 100.0 : 0);

        List<Map<String, Object>> dist = new ArrayList<>();
        dist.add(createDistributionItem("待审核", pending));
        dist.add(createDistributionItem("已录用", accepted));
        dist.add(createDistributionItem("已拒绝", rejected));
        stats.put("statusDistribution", dist);

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

            int memberCount = clubMemberMapper.countActiveMembers(club.getClubId());
            clubData.put("memberCount", memberCount);

            long eventCount = allEvents.stream()
                    .filter(e -> club.getClubId().equals(e.getClubId()) && Integer.valueOf(3).equals(e.getAuditStatus()))
                    .count();
            clubData.put("eventCount", eventCount);

            int activityScore = memberCount * 2 + (int) eventCount * 5;
            clubData.put("activityScore", activityScore);
            clubRankingList.add(clubData);
        }

        clubRankingList.sort((a, b) -> ((Integer)b.get("activityScore")).compareTo((Integer)a.get("activityScore")));
        ranking.put("rankings", clubRankingList.stream().limit(10).collect(Collectors.toList()));

        List<String> clubNames = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        clubRankingList.stream().limit(10).forEach(c -> {
            clubNames.add((String) c.get("clubName"));
            scores.add((Integer) c.get("activityScore"));
        });
        ranking.put("clubNames", clubNames);
        ranking.put("scores", scores);

        return ranking;
    }

    @Override
    public Map<String, Object> getClubMemberDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        List<Map<String, Object>> pieData = clubMapper.selectAll().stream()
                .map(c -> createDistributionItem(c.getClubName(), clubMemberMapper.countActiveMembers(c.getClubId())))
                .filter(item -> (int)item.get("value") > 0)
                .sorted((a, b) -> ((Integer)b.get("value")).compareTo((Integer)a.get("value")))
                .collect(Collectors.toList());
        
        distribution.put("pieData", pieData);
        return distribution;
    }

    @Override
    public Map<String, Object> getEventParticipationRate() {
        Map<String, Object> stats = new HashMap<>();
        List<Event> events = eventMapper.selectAll();
        List<EventSignup> signups = eventSignupMapper.selectAll();
        if (signups == null) signups = Collections.emptyList();

        List<String> months = new ArrayList<>();
        List<Integer> eventCounts = new ArrayList<>();
        List<Integer> signupCounts = new ArrayList<>();
        List<Double> participationRates = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            months.add(monthDate.format(formatter));

            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            List<Event> monthEvents = events.stream()
                    .filter(e -> e.getStartTime() != null && e.getStartTime().isAfter(monthStart) && e.getStartTime().isBefore(monthEnd) && Integer.valueOf(3).equals(e.getAuditStatus()))
                    .collect(Collectors.toList());
            eventCounts.add(monthEvents.size());

            final List<EventSignup> finalSignups = signups;
            int signupCount = monthEvents.stream()
                    .mapToInt(e -> (int) finalSignups.stream().filter(s -> e.getEventId().equals(s.getEventId())).count())
                    .sum();
            signupCounts.add(signupCount);

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
