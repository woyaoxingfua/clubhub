-- 社长权限数据一致性修复SQL
-- 执行时间：2025-12-26
-- 目的：确保社长在三个维度的数据一致性

USE club_system_db;

-- ============================================
-- 第一步：确保所有社长都有成员表记录
-- ============================================

-- 查看当前社长情况
SELECT 
    c.club_id,
    c.club_name,
    c.president_id,
    u.real_name AS president_name,
    m.id AS member_record_id,
    m.position,
    m.role_group,
    m.status
FROM tb_club c
LEFT JOIN sys_user u ON c.president_id = u.user_id
LEFT JOIN tb_club_member m ON c.club_id = m.club_id AND c.president_id = m.user_id
WHERE c.president_id IS NOT NULL;

-- 为没有成员记录的社长创建记录
INSERT INTO tb_club_member (club_id, user_id, position, department, role_group, join_time, status)
SELECT 
    c.club_id,
    c.president_id,
    'president',
    'admin_dept',
    'admin',
    NOW(),
    1
FROM tb_club c
WHERE c.president_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM tb_club_member m 
    WHERE m.club_id = c.club_id AND m.user_id = c.president_id
);

-- ============================================
-- 第二步：更新现有社长的信息
-- ============================================

-- 更新社长的职位为"社长"
UPDATE tb_club_member m
INNER JOIN tb_club c ON m.club_id = c.club_id AND m.user_id = c.president_id
SET 
    m.position = 'president',
    m.role_group = 'admin',
    m.department = CASE 
        WHEN m.department IS NULL OR m.department = '' 
        THEN 'admin_dept' 
        ELSE m.department 
    END,
    m.status = 1
WHERE c.president_id IS NOT NULL;

-- ============================================
-- 第三步：清理错误数据（已经不是社长但position还是"社长"）
-- ============================================

-- 将不是社长但position是"社长"的成员改为副社长或成员
UPDATE tb_club_member m
SET 
    m.position = CASE 
        WHEN m.role_group = 'admin' THEN 'vice_president'
        WHEN m.role_group = 'leader' THEN 'department_head'
        ELSE 'member'
    END
WHERE m.position = 'president'
AND NOT EXISTS (
    SELECT 1 FROM tb_club c 
    WHERE c.club_id = m.club_id AND c.president_id = m.user_id
);

-- ============================================
-- 第四步：验证数据一致性
-- ============================================

-- 检查社长数据一致性（应该没有结果，表示所有数据都一致）
SELECT 
    'missing_member_record' AS issue_type,
    c.club_id,
    c.club_name,
    c.president_id
FROM tb_club c
WHERE c.president_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM tb_club_member m 
    WHERE m.club_id = c.club_id AND m.user_id = c.president_id AND m.status = 1
)

UNION ALL

-- 检查社长的role_group是否为admin
SELECT 
    'role_group_not_admin' AS issue_type,
    c.club_id,
    c.club_name,
    c.president_id
FROM tb_club c
INNER JOIN tb_club_member m ON c.club_id = m.club_id AND c.president_id = m.user_id
WHERE c.president_id IS NOT NULL
AND m.role_group != 'admin'

UNION ALL

-- 检查社长的position是否为"社长"
SELECT 
    'position_not_president' AS issue_type,
    c.club_id,
    c.club_name,
    c.president_id
FROM tb_club c
INNER JOIN tb_club_member m ON c.club_id = m.club_id AND c.president_id = m.user_id
WHERE c.president_id IS NOT NULL
AND m.position != 'president';

-- ============================================
-- 第五步：显示修复后的社长信息
-- ============================================

SELECT 
    c.club_id,
    c.club_name,
    u.real_name AS president_name,
    m.position,
    m.department,
    m.role_group,
    CASE WHEN m.status = 1 THEN 'active' ELSE 'inactive' END AS status
FROM tb_club c
INNER JOIN sys_user u ON c.president_id = u.user_id
INNER JOIN tb_club_member m ON c.club_id = m.club_id AND c.president_id = m.user_id
WHERE c.president_id IS NOT NULL
ORDER BY c.club_id;

SELECT 'President data consistency fixed successfully!' AS message;
