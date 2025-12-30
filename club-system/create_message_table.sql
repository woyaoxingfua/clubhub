-- 创建站内信表
CREATE TABLE IF NOT EXISTS `tb_message` (
  `message_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` INT(11) NOT NULL COMMENT '发送人ID',
  `receiver_id` INT(11) NOT NULL COMMENT '接收人ID',
  `club_id` INT(11) DEFAULT NULL COMMENT '关联社团ID（可选）',
  `subject` VARCHAR(200) DEFAULT NULL COMMENT '消息主题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读（0=未读，1=已读）',
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_club` (`club_id`),
  KEY `idx_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';
