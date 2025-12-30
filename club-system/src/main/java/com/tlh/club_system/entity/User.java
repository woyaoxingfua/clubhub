package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户信息表
 * @TableName sys_user
 */
@Data
public class User {
    /**
     * 用户主键
     */
    private Integer userId;

    /**
     * 学号/工号 (登录账号)
     */
    private String username;

    /**
     * 密码 (建议BCrypt/MD5加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色标识: SYS_ADMIN, DEPT_ADMIN, ADVISOR, CLUB_ADMIN, STUDENT
     */
    private String roleKey;

    /**
     * 所属院系ID
     */
    private Integer deptId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态: 1启用 0禁用
     */
    private Integer status;

    /**
     * 学号（独立字段）
     */
    private String studentId;

    /**
     * 性别: 1=男, 2=女
     */
    private Integer gender;

    /**
     * 
     */
    private LocalDateTime createTime;
}