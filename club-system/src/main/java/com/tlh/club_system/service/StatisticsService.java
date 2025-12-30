package com.tlh.club_system.service;

import java.util.Map;

/**
 * 数据统计服务接口
 * @author AI Assistant
 * @version v0.11.0
 */
public interface StatisticsService {

    /**
     * 获取系统总览统计
     * 包括：社团总数、成员总数、活动总数、本月新增成员等
     */
    Map<String, Object> getSystemOverview();

    /**
     * 获取成员增长趋势（按月统计，近12个月）
     */
    Map<String, Object> getMemberGrowthTrend();

    /**
     * 获取活动统计数据
     * 包括：活动总数、已审批、待审批、各状态分布
     */
    Map<String, Object> getEventStatistics();

    /**
     * 获取财务统计数据
     * 包括：总收入、总支出、各社团财务排名
     */
    Map<String, Object> getFinanceStatistics();

    /**
     * 获取招新统计数据
     * 包括：申请总数、录用数、拒绝数、转化率
     */
    Map<String, Object> getRecruitStatistics();

    /**
     * 获取社团活跃度排名
     * 根据成员数、活动数、财务活跃度综合计算
     */
    Map<String, Object> getClubActivityRanking();

    /**
     * 获取各社团成员分布（饼图数据）
     */
    Map<String, Object> getClubMemberDistribution();

    /**
     * 获取活动参与率统计（近6个月）
     */
    Map<String, Object> getEventParticipationRate();
}
