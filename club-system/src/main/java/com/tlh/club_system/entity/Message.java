package com.tlh.club_system.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 站内信实体类
 * @TableName tb_message
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private Integer messageId;

    /**
     * 发送人ID
     */
    private Integer senderId;

    /**
     * 接收人ID
     */
    private Integer receiverId;

    /**
     * 关联社团ID（可选）
     */
    private Integer clubId;

    /**
     * 消息主题
     */
    private String subject;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读（0=未读，1=已读）
     */
    private Integer isRead;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    // 非数据库字段，用于关联查询
    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 社团名称
     */
    private String clubName;
}
