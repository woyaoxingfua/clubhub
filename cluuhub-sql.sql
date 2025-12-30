/*
SQLyog Ultimate v12.08 (32 bit)
MySQL - 8.0.39 : Database - club_system_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`club_system_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `club_system_db`;

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `dept_id` int NOT NULL AUTO_INCREMENT COMMENT '院系ID',
  `dept_name` varchar(100) NOT NULL COMMENT '院系名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学院信息表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`dept_id`,`dept_name`,`create_time`) values (1,'计算机学院','2025-12-24 23:01:46'),(2,'软件学院','2025-12-24 23:01:46'),(3,'外国语学院','2025-12-24 23:01:46');

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户主键',
  `username` varchar(50) NOT NULL COMMENT '学号/工号 (登录账号)',
  `password` varchar(100) NOT NULL COMMENT '密码 (建议BCrypt/MD5加密)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role_key` varchar(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色标识: SYS_ADMIN, DEPT_ADMIN, ADVISOR, CLUB_ADMIN, STUDENT',
  `dept_id` int DEFAULT NULL COMMENT '所属院系ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `student_id` varchar(20) DEFAULT NULL COMMENT '学号',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别:1=男,2=女',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`username`,`password`,`real_name`,`role_key`,`dept_id`,`phone`,`email`,`status`,`create_time`,`student_id`,`gender`) values (1,'admin','123456','系统管理员','SYS_ADMIN',NULL,NULL,NULL,1,'2025-12-24 23:01:48',NULL,NULL),(2,'t001','123456','李老师','DEPT_ADMIN',1,NULL,NULL,1,'2025-12-24 23:01:48',NULL,NULL),(3,'s2023001','123456','张三','STUDENT',1,'ceshi12',NULL,1,'2025-12-24 23:01:48',NULL,NULL),(4,'wu','123456','无身份','STUDENT',1,'ceshi22',NULL,1,'2025-12-25 23:29:17',NULL,NULL),(5,'zhucece','000000','注册测试','STUDENT',NULL,'17711112222',NULL,1,'2025-12-27 23:53:01',NULL,NULL);

/*Table structure for table `tb_club` */

DROP TABLE IF EXISTS `tb_club`;

CREATE TABLE `tb_club` (
  `club_id` int NOT NULL AUTO_INCREMENT COMMENT '社团ID',
  `club_name` varchar(100) NOT NULL COMMENT '社团名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类 (文化/体育/科技) [cite: 13]',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '社团Logo',
  `description` text COMMENT '社团简介/章程',
  `honors` text COMMENT '社团荣誉',
  `president_id` int DEFAULT NULL,
  `advisor_id` int DEFAULT NULL COMMENT '指导老师ID (关联User)',
  `dept_id` int NOT NULL COMMENT '所属院系ID',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '社团经费余额 [cite: 28]',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态: 0审核中, 1正常, 2注销',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`club_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社团信息表';

/*Data for the table `tb_club` */

insert  into `tb_club`(`club_id`,`club_name`,`category`,`logo_url`,`description`,`honors`,`president_id`,`advisor_id`,`dept_id`,`balance`,`status`,`create_time`) values (1,'计算机协会','科技类','http://localhost:8080/club_system_war_exploded/uploads/logos/logo_f49207c2-2a62-4da9-96ec-073283b542ee.png','学习计算机','优秀社团\n',3,2,1,'0.00',0,'2025-12-25 17:51:25'),(2,'测试二','科技类','http://localhost:8080/club_system_war_exploded/uploads/logos/logo_0188cf66-1378-4205-b4c6-5e21d57601d8.png','','社团第一名',4,1,0,'0.00',0,'2025-12-25 21:16:53');

/*Table structure for table `tb_club_member` */

DROP TABLE IF EXISTS `tb_club_member`;

CREATE TABLE `tb_club_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `club_id` int NOT NULL COMMENT '社团ID',
  `user_id` int NOT NULL COMMENT '学生ID',
  `position` varchar(50) DEFAULT '普通成员' COMMENT '社内职位 (社长/干事/会员)',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入社时间',
  `status` tinyint DEFAULT '1' COMMENT '成员状态：1=在社, 0=已离社',
  `department` varchar(50) DEFAULT '未分配' COMMENT '所属部门（如：技术部、宣传部、组织部等）',
  `role_group` varchar(30) DEFAULT 'member' COMMENT '权限组（admin=管理员, leader=部长, member=普通成员）',
  `quit_reason` varchar(200) DEFAULT NULL COMMENT '退出/开除原因',
  `quit_time` datetime DEFAULT NULL COMMENT '退出/开除时间',
  `quit_type` tinyint DEFAULT NULL COMMENT '退出类型：1=主动退出, 2=被开除, NULL=在社',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_club_user` (`club_id`,`user_id`),
  KEY `idx_club_status` (`club_id`,`status`),
  KEY `idx_department` (`department`),
  KEY `idx_role_group` (`role_group`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社团成员关联表';

/*Data for the table `tb_club_member` */

insert  into `tb_club_member`(`id`,`club_id`,`user_id`,`position`,`join_time`,`status`,`department`,`role_group`,`quit_reason`,`quit_time`,`quit_type`) values (1,1,3,'社长','2025-12-26 17:17:08',1,'未分配','admin',NULL,NULL,NULL),(2,2,3,'部长','2025-12-25 21:23:31',1,'技术部','leader',NULL,NULL,NULL),(3,1,4,'副社长','2025-12-26 23:18:23',1,'管理层','admin',NULL,NULL,NULL),(4,2,4,'社长','2025-12-26 22:24:40',1,'管理层','admin',NULL,NULL,NULL);

/*Table structure for table `tb_event` */

DROP TABLE IF EXISTS `tb_event`;

CREATE TABLE `tb_event` (
  `event_id` int NOT NULL AUTO_INCREMENT,
  `club_id` int NOT NULL COMMENT '主办社团',
  `title` varchar(100) NOT NULL COMMENT '活动名称',
  `content` text COMMENT '活动策划详情/内容',
  `location` varchar(100) DEFAULT NULL COMMENT '活动地点',
  `budget` decimal(10,2) DEFAULT '0.00' COMMENT '预算金额',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `max_people` int DEFAULT '0' COMMENT '最大报名人数 (0不限)',
  `audit_status` tinyint DEFAULT '0' COMMENT '审批状态: 0待提交, 1待指导老师审, 2待院系审, 3已通过, 4已驳回 ',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审批意见',
  `summary` text COMMENT '活动总结(活动结束后填写)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `target_type` int DEFAULT '0' COMMENT '目标范围: 0全校, 1指定院系',
  `target_dept_id` int DEFAULT NULL COMMENT '目标院系ID',
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动申请表';

/*Data for the table `tb_event` */

insert  into `tb_event`(`event_id`,`club_id`,`title`,`content`,`location`,`budget`,`start_time`,`end_time`,`max_people`,`audit_status`,`audit_remark`,`summary`,`create_time`,`create_by`,`target_type`,`target_dept_id`) values (1,1,'校园歌手大赛','这是策划案...','大礼堂','5000.00','2025-05-01 19:00:00','2025-05-01 22:00:00',0,3,'院系越级审批通过',NULL,'2025-12-25 17:03:07',NULL,0,NULL),(2,1,'12','1223','1233','600.00','2025-12-25 00:00:00','2026-01-01 00:00:00',0,4,'',NULL,'2025-12-25 17:15:53',NULL,0,NULL),(3,1,'12','222','2222','300.00','2025-12-06 00:00:00','2025-12-25 00:00:00',0,0,NULL,NULL,'2025-12-25 17:19:48',NULL,0,NULL),(4,1,'11','11111','1111111','700.00','2025-12-25 00:00:00','2025-12-27 00:00:00',0,0,NULL,NULL,'2025-12-25 17:26:09',NULL,0,NULL),(5,1,'1111','111·111111','1111111111','700.00','2025-12-25 00:00:00','2025-12-27 00:00:00',0,0,NULL,NULL,'2025-12-25 17:27:51',NULL,0,NULL),(9,1,'无','无','无','400.00','2025-12-25 00:00:00','2026-01-01 00:00:00',8,1,NULL,NULL,'2025-12-25 23:06:32',3,1,1),(10,2,'222测','22222','3333333','500.00','2025-12-25 00:00:00','2026-01-18 00:00:00',5,3,'院系越级审批通过',NULL,'2025-12-25 23:19:08',3,0,NULL);

/*Table structure for table `tb_event_signup` */

DROP TABLE IF EXISTS `tb_event_signup`;

CREATE TABLE `tb_event_signup` (
  `signup_id` int NOT NULL AUTO_INCREMENT,
  `event_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` tinyint(1) DEFAULT '0' COMMENT '状态: 0已报名, 1报名成功(审核通过), 2已签到/核销',
  `signup_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`signup_id`),
  UNIQUE KEY `uk_event_user` (`event_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名签到表';

/*Data for the table `tb_event_signup` */

insert  into `tb_event_signup`(`signup_id`,`event_id`,`user_id`,`status`,`signup_time`) values (2,1,3,0,'2025-12-25 21:46:41'),(3,1,4,0,'2025-12-25 23:29:47'),(4,10,4,0,'2025-12-25 23:37:51'),(5,10,3,0,'2025-12-26 00:08:13');

/*Table structure for table `tb_file` */

DROP TABLE IF EXISTS `tb_file`;

CREATE TABLE `tb_file` (
  `file_id` int NOT NULL AUTO_INCREMENT,
  `owner_id` int NOT NULL COMMENT '归属ID (可以是club_id, event_id, finance_id)',
  `category` varchar(20) NOT NULL COMMENT '分类: LOGO, EVENT_IMG, DOC, PROOF, RESUME',
  `file_name` varchar(100) NOT NULL,
  `file_url` varchar(255) NOT NULL COMMENT '文件存储路径',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件附件表';

/*Data for the table `tb_file` */

/*Table structure for table `tb_finance` */

DROP TABLE IF EXISTS `tb_finance`;

CREATE TABLE `tb_finance` (
  `finance_id` int NOT NULL AUTO_INCREMENT,
  `club_id` int NOT NULL COMMENT '关联社团',
  `event_id` int DEFAULT NULL COMMENT '关联活动(如果是活动报销)',
  `type` tinyint(1) NOT NULL COMMENT '1:收入 (赞助/会费) 2:支出 (活动报销/物资)',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `description` varchar(255) DEFAULT NULL COMMENT '用途说明',
  `proof_url` varchar(255) DEFAULT NULL COMMENT '发票/凭证图片URL',
  `status` tinyint(1) DEFAULT '0' COMMENT '0:审批中 1:通过 2:驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`finance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经费收支明细表';

/*Data for the table `tb_finance` */

insert  into `tb_finance`(`finance_id`,`club_id`,`event_id`,`type`,`amount`,`description`,`proof_url`,`status`,`create_time`) values (1,1,9,2,'2000.00','社团成立','http://localhost:8080/club_system_war_exploded/uploads/proofs/proof_4bcff588-dff9-4123-8aca-a41d9f15e67f.png',1,'2025-12-26 14:44:51'),(2,2,NULL,2,'1000.00','社团成立测试一','',2,'2025-12-27 00:25:13');

/*Table structure for table `tb_message` */

DROP TABLE IF EXISTS `tb_message`;

CREATE TABLE `tb_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` int NOT NULL COMMENT '发送人ID',
  `receiver_id` int NOT NULL COMMENT '接收人ID',
  `club_id` int DEFAULT NULL COMMENT '关联社团ID（可选）',
  `subject` varchar(200) DEFAULT NULL COMMENT '消息主题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读（0=未读，1=已读）',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_club` (`club_id`),
  KEY `idx_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内信表';

/*Data for the table `tb_message` */

insert  into `tb_message`(`message_id`,`sender_id`,`receiver_id`,`club_id`,`subject`,`content`,`is_read`,`send_time`,`read_time`) values (1,4,3,1,'你好','这是测试',1,'2025-12-27 14:30:11','2025-12-27 14:30:27'),(2,3,4,2,'你好','收到测试',1,'2025-12-27 14:30:49','2025-12-27 14:30:58');

/*Table structure for table `tb_notice` */

DROP TABLE IF EXISTS `tb_notice`;

CREATE TABLE `tb_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `publisher_id` int NOT NULL COMMENT '发布人ID',
  `target_type` tinyint(1) DEFAULT '0' COMMENT '范围: 0全校可见, 1本院可见, 2本社可见',
  `dept_id` int DEFAULT NULL COMMENT '如果针对院系，此处存ID',
  `club_id` int DEFAULT NULL COMMENT '如果针对社团，此处存ID',
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_pinned` tinyint DEFAULT '0' COMMENT '是否置顶: 0否, 1是',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

/*Data for the table `tb_notice` */

insert  into `tb_notice`(`notice_id`,`title`,`content`,`publisher_id`,`target_type`,`dept_id`,`club_id`,`publish_time`,`is_pinned`) values (1,'测试','123',1,0,NULL,NULL,'2025-12-25 22:01:47',1),(2,'23','0',1,1,NULL,NULL,'2025-12-25 22:04:37',0),(3,'社团可见公告','社团可见公告测试',1,2,NULL,1,'2025-12-26 22:07:21',0),(4,'本院可见内容','本院可见内容测试\n',2,1,NULL,NULL,'2025-12-30 14:17:22',1);

/*Table structure for table `tb_recruit_application` */

DROP TABLE IF EXISTS `tb_recruit_application`;

CREATE TABLE `tb_recruit_application` (
  `app_id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int NOT NULL COMMENT '关联招新计划',
  `user_id` int NOT NULL COMMENT '申请学生',
  `resume_file_url` varchar(255) DEFAULT NULL COMMENT '附件简历URL',
  `self_intro` text COMMENT '自我介绍/文字简历',
  `status` tinyint(1) DEFAULT '0' COMMENT '0待审核 1录用 2婉拒',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招新申请表';

/*Data for the table `tb_recruit_application` */

insert  into `tb_recruit_application`(`app_id`,`plan_id`,`user_id`,`resume_file_url`,`self_intro`,`status`,`apply_time`) values (1,2,3,'','嗯',1,'2025-12-25 20:51:43'),(2,2,3,'','\n',1,'2025-12-25 20:53:50'),(3,2,3,'','1',1,'2025-12-25 20:56:20'),(4,3,3,'','进',1,'2025-12-25 21:09:13'),(5,2,4,'','测试啊\n',1,'2025-12-26 14:14:52'),(6,4,4,'http://localhost:8080/club_system_war_exploded/uploads/resumes/resume_软件工程综合开发课程设计指导书_1766759102799.doc','没有啥就是嗯\n',2,'2025-12-26 22:25:04'),(7,3,4,'','222',1,'2025-12-27 00:23:05');

/*Table structure for table `tb_recruit_plan` */

DROP TABLE IF EXISTS `tb_recruit_plan`;

CREATE TABLE `tb_recruit_plan` (
  `plan_id` int NOT NULL AUTO_INCREMENT,
  `club_id` int NOT NULL COMMENT '关联社团',
  `title` varchar(100) NOT NULL COMMENT '招新标题',
  `positions` varchar(255) DEFAULT NULL COMMENT '招聘岗位(逗号分隔)',
  `requirements` text COMMENT '岗位要求',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1进行中 0已结束',
  PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社团招新计划表';

/*Data for the table `tb_recruit_plan` */

insert  into `tb_recruit_plan`(`plan_id`,`club_id`,`title`,`positions`,`requirements`,`start_time`,`end_time`,`status`) values (2,1,'招新','1','1','2025-12-25 20:51:31',NULL,1),(3,1,'招新','成员','无','2025-12-25 21:08:52',NULL,1),(4,2,'1','1','','2025-12-25 21:17:05','2026-01-25 21:17:05',1);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
