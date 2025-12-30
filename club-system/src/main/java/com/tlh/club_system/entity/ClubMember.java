package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 社团成员关联表
 * @TableName tb_club_member
 */
@Data
public class ClubMember {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 社团ID
     */
    private Integer clubId;

    /**
     * 学生ID
     */
    private Integer userId;

    /**
     * 社内职位 (社长/副社长/部长/干事/会员等)
     */
    private String position;

    /**
     * 所属部门 (技术部/宣传部/组织部等)
     */
    private String department;

    /**
     * 权限组 (admin=管理员, leader=部长, member=普通成员)
     */
    private String roleGroup;

    /**
     * 入社时间
     */
    private LocalDateTime joinTime;

    /**
     * 状态: 1=在社, 0=已离社
     */
    private Integer status;

    /**
     * 退出类型：1=主动退出, 2=被开除, NULL=在社
     */
    private Integer quitType;

    /**
     * 退出/开除原因
     */
    private String quitReason;

    /**
     * 退出/开除时间
     */
    private LocalDateTime quitTime;
    
    /**
     * 学生姓名（非数据库字段，用于关联查询）
     */
    private String studentName;
    
    /**
     * 用户名（前端统一使用，非数据库字段）
     */
    private String userName;
    
    /**
     * 手机号（非数据库字段，用于关联查询）
     */
    private String phone;
    
    /**
     * 邮箱（非数据库字段，用于关联查询）
     */
    private String email;
    
    /**
     * 学号（非数据库字段，如数据库没有则为空）
     */
    private String studentId;
    
    /**
     * 性别（非数据库字段，如数据库没有则为空）
     */
    private Integer gender;
    
    /**
     * 社团名称（非数据库字段，用于关联查询）
     */
    private String clubName;
}