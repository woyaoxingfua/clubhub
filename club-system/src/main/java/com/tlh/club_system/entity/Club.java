package com.tlh.club_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 社团信息表
 * @TableName tb_club
 */
@Data
public class Club {
    /**
     * 社团ID
     */
    private Integer clubId;

    /**
     * 社团名称
     */
    private String clubName;

    /**
     * 分类 (文化/体育/科技) [cite: 13]
     */
    private String category;

    /**
     * 社团Logo
     */
    private String logoUrl;

    /**
     * 社团简介/章程
     */
    private String description;

    /**
     * 社团荣誉
     */
    private String honors;

    /**
     * 社长ID (关联User)
     */
    private Integer presidentId;

    /**
     * 指导老师ID (关联User)
     */
    private Integer advisorId;

    /**
     * 所属院系ID
     */
    private Integer deptId;

    /**
     * 社团经费余额 [cite: 28]
     */
    private BigDecimal balance;

    /**
     * 状态: 0审核中, 1正常, 2注销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}