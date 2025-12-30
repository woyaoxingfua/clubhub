package com.tlh.club_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经费收支明细表
 * @TableName tb_finance
 */
@Data
public class Finance {
    /**
     * 
     */
    private Integer financeId;

    /**
     * 关联社团
     */
    private Integer clubId;

    /**
     * 关联活动(如果是活动报销)
     */
    private Integer eventId;

    /**
     * 1:收入 (赞助/会费) 2:支出 (活动报销/物资)
     */
    private Integer type;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 用途说明
     */
    private String description;

    /**
     * 发票/凭证图片URL
     */
    private String proofUrl;

    /**
     * 0:审批中 1:通过 2:驳回
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime createTime;
    
    /**
     * 社团名称（非数据库字段，用于关联查询）
     */
    private String clubName;
    
    /**
     * 活动名称（非数据库字段，用于关联查询）
     */
    private String eventName;
}