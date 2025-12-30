package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 活动报名签到表
 * @TableName tb_event_signup
 */
@Data
public class EventSignup {
    /**
     * 
     */
    private Integer signupId;

    /**
     * 
     */
    private Integer eventId;

    /**
     * 
     */
    private Integer userId;

    /**
     * 状态: 0已报名, 1报名成功(审核通过), 2已签到/核销
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime signupTime;
}