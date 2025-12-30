package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 社团招新计划表
 * @TableName tb_recruit_plan
 */
@Data
public class RecruitPlan {
    /**
     * 
     */
    private Integer planId;

    /**
     * 关联社团
     */
    private Integer clubId;

    /**
     * 招新标题
     */
    private String title;

    /**
     * 招聘岗位(逗号分隔)
     */
    private String positions;

    /**
     * 岗位要求
     */
    private String requirements;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 1进行中 0已结束
     */
    private Integer status;
}