package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通知公告表
 * @TableName tb_notice
 */
@Data
public class Notice {
    /**
     * 
     */
    private Integer noticeId;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String content;

    /**
     * 发布人ID
     */
    private Integer publisherId;

    /**
     * 范围: 0全校可见, 1本院可见, 2本社可见
     */
    private Integer targetType;

    /**
     * 如果针对院系，此处存ID
     */
    private Integer deptId;

    /**
     * 如果针对社团，此处存ID
     */
    private Integer clubId;

    /**
     * 
     */
    private LocalDateTime publishTime;
    
    /**
     * 是否置顶: 0否, 1是
     */
    private Integer isPinned;
    
    /**
     * 社团名称（非数据库字段，用于关联查询）
     */
    private String clubName;
    
    /**
     * 发布人姓名（非数据库字段，用于关联查询）
     */
    private String publisherName;
}