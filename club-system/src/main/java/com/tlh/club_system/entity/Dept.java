package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 学院信息表
 * @TableName sys_dept
 */
@Data
public class Dept {
    /**
     * 院系ID
     */
    private Integer deptId;

    /**
     * 院系名称
     */
    private String deptName;

    /**
     * 
     */
    private LocalDateTime createTime;
}