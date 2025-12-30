package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.service.StatisticsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器
 * 提供Dashboard所需的各类统计数据API
 * @author AI Assistant
 * @version v0.11.0
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 测试接口 - 检查服务是否正常
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("统计服务正常运行");
    }

    /**
     * 获取系统总览数据
     * 包括：社团总数、成员总数、活动总数、本月新增等
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> data = statisticsService.getSystemOverview();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取系统总览失败: " + e.getMessage());
        }
    }

    /**
     * 获取成员增长趋势（近12个月）
     */
    @GetMapping("/member-growth")
    public Result<Map<String, Object>> getMemberGrowth() {
        try {
            Map<String, Object> data = statisticsService.getMemberGrowthTrend();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取成员增长趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动统计数据
     */
    @GetMapping("/events")
    public Result<Map<String, Object>> getEventStatistics() {
        try {
            Map<String, Object> data = statisticsService.getEventStatistics();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取活动统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取财务统计数据
     */
    @GetMapping("/finance")
    public Result<Map<String, Object>> getFinanceStatistics() {
        try {
            Map<String, Object> data = statisticsService.getFinanceStatistics();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取财务统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取招新统计数据
     */
    @GetMapping("/recruit")
    public Result<Map<String, Object>> getRecruitStatistics() {
        try {
            Map<String, Object> data = statisticsService.getRecruitStatistics();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取招新统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取社团活跃度排名
     */
    @GetMapping("/club-ranking")
    public Result<Map<String, Object>> getClubRanking() {
        try {
            Map<String, Object> data = statisticsService.getClubActivityRanking();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取社团排名失败: " + e.getMessage());
        }
    }

    /**
     * 获取社团成员分布（饼图数据）
     */
    @GetMapping("/member-distribution")
    public Result<Map<String, Object>> getMemberDistribution() {
        try {
            Map<String, Object> data = statisticsService.getClubMemberDistribution();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取成员分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动参与率统计
     */
    @GetMapping("/participation-rate")
    public Result<Map<String, Object>> getParticipationRate() {
        try {
            Map<String, Object> data = statisticsService.getEventParticipationRate();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取参与率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取Dashboard全部数据（一次性获取所有统计）
     * 减少前端请求次数，提升加载速度
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();  // 打印详细错误日志
            return Result.error("获取Dashboard数据失败: " + e.getMessage());
        }
    }
}
