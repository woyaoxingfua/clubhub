-- 成员管理功能增强 - 数据库升级脚本
-- 执行时间：2025-12-26
-- 版本：v0.8.0

USE club_system;

-- 1. 为成员表添加部门字段
ALTER TABLE tb_club_member 
ADD COLUMN department VARCHAR(50) DEFAULT '未分配' COMMENT '所属部门（如：技术部、宣传部、组织部等）';

-- 2. 为成员表添加权限组字段
ALTER TABLE tb_club_member
ADD COLUMN role_group VARCHAR(30) DEFAULT 'member' COMMENT '权限组（admin=管理员, leader=部长, member=普通成员）';

-- 3. 为成员表添加退出原因字段
ALTER TABLE tb_club_member
ADD COLUMN quit_reason VARCHAR(200) DEFAULT NULL COMMENT '退出/开除原因';

-- 4. 为成员表添加退出时间字段
ALTER TABLE tb_club_member
ADD COLUMN quit_time DATETIME DEFAULT NULL COMMENT '退出/开除时间';

-- 5. 为成员表添加退出类型字段（区分主动退出和被开除）
ALTER TABLE tb_club_member
ADD COLUMN quit_type TINYINT DEFAULT NULL COMMENT '退出类型：1=主动退出, 2=被开除, NULL=在社';

-- 6. 更新 status 字段注释，使其更清晰
ALTER TABLE tb_club_member 
MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '成员状态：1=在社, 0=已离社';

-- 7. 创建索引以提升查询性能
CREATE INDEX idx_club_status ON tb_club_member(club_id, status);
CREATE INDEX idx_department ON tb_club_member(department);
CREATE INDEX idx_role_group ON tb_club_member(role_group);

-- 8. 为现有数据补充默认值
UPDATE tb_club_member SET department = '未分配' WHERE department IS NULL;
UPDATE tb_club_member SET role_group = 'member' WHERE role_group IS NULL;

-- 9. 更新社长的权限组为 admin
UPDATE tb_club_member 
SET role_group = 'admin' 
WHERE position = '社长';

-- 10. 更新副社长和部长的权限组为 leader
UPDATE tb_club_member 
SET role_group = 'leader' 
WHERE position IN ('副社长', '部长', '副部长', '会长');

-- 验证SQL执行结果
SELECT '数据库升级完成！' AS message;
SELECT COUNT(*) AS total_members FROM tb_club_member;
SELECT status, COUNT(*) AS count FROM tb_club_member GROUP BY status;
