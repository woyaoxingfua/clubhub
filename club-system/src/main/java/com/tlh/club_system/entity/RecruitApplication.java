package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 招新申请表
 * @TableName tb_recruit_application
 */
@Data
public class RecruitApplication {
    /**
     * 
     */
    private Integer appId;

    /**
     * 关联招新计划
     */
    private Integer planId;

    /**
     * 申请学生
     */
    private Integer userId;

    /**
     * 附件简历URL
     */
    private String resumeFileUrl;

    /**
     * 自我介绍/文字简历
     */
    private String selfIntro;

    /**
     * 0待审核 1录用 2婉拒
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime applyTime;
    
    /**
     * 学生姓名（非数据库字段，用于关联查询）
     */
    private String studentName;
}