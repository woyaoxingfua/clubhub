package com.tlh.club_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 活动申请表
 * @TableName tb_event
 */
@Data
public class Event {
    /**
     * 
     */
    private Integer eventId;

    /**
     * 主办社团
     */
    private Integer clubId;

    /**
     * 活动名称
     */
    private String title;

    /**
     * 活动策划详情/内容
     */
    private String content;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 预算金额
     */
    private BigDecimal budget;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 最大报名人数 (0不限)
     */
    private Integer maxPeople;

    /**
     * 审批状态: 0待提交, 1待指导老师审, 2待院系审, 3已通过, 4已驳回 
     */
    private Integer auditStatus;

    /**
     * 审批意见
     */
    private String auditRemark;

    /**
     * 活动总结(活动结束后填写)
     */
    private String summary;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID (新增)
     */
    private Long createBy;

    /**
     * 目标范围: 0全校, 1指定院系
     */
    private Integer targetType;

    /**
     * 目标院系ID
     */
    private Integer targetDeptId;
}