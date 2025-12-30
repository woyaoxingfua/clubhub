package com.tlh.club_system.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件附件表
 * @TableName tb_file
 */
@Data
public class File {
    /**
     * 
     */
    private Integer fileId;

    /**
     * 归属ID (可以是club_id, event_id, finance_id)
     */
    private Integer ownerId;

    /**
     * 分类: LOGO, EVENT_IMG, DOC, PROOF, RESUME
     */
    private String category;

    /**
     * 
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String fileUrl;

    /**
     * 
     */
    private LocalDateTime uploadTime;
}