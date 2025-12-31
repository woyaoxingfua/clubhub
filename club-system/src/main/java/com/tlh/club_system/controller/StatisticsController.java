package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.service.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getSystemOverview());
    }

    @GetMapping("/member-growth")
    public Result<Map<String, Object>> getMemberGrowth() {
        return Result.success(statisticsService.getMemberGrowthTrend());
    }

    @GetMapping("/events")
    public Result<Map<String, Object>> getEventStatistics() {
        return Result.success(statisticsService.getEventStatistics());
    }

    @GetMapping("/finance")
    public Result<Map<String, Object>> getFinanceStatistics() {
        return Result.success(statisticsService.getFinanceStatistics());
    }

    @GetMapping("/recruit")
    public Result<Map<String, Object>> getRecruitStatistics() {
        return Result.success(statisticsService.getRecruitStatistics());
    }

    @GetMapping("/club-ranking")
    public Result<Map<String, Object>> getClubRanking() {
        return Result.success(statisticsService.getClubActivityRanking());
    }

    @GetMapping("/member-distribution")
    public Result<Map<String, Object>> getMemberDistribution() {
        return Result.success(statisticsService.getClubMemberDistribution());
    }

    @GetMapping("/participation-rate")
    public Result<Map<String, Object>> getParticipationRate() {
        return Result.success(statisticsService.getEventParticipationRate());
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("overview", statisticsService.getSystemOverview());
        dashboard.put("memberGrowth", statisticsService.getMemberGrowthTrend());
        dashboard.put("eventStats", statisticsService.getEventStatistics());
        dashboard.put("financeStats", statisticsService.getFinanceStatistics());
        dashboard.put("recruitStats", statisticsService.getRecruitStatistics());
        dashboard.put("clubRanking", statisticsService.getClubActivityRanking());
        dashboard.put("memberDistribution", statisticsService.getClubMemberDistribution());
        dashboard.put("participationRate", statisticsService.getEventParticipationRate());
        return Result.success(dashboard);
    }
}
