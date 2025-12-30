-- 为tb_club表添加honors字段
-- 执行此SQL来添加社团荣誉字段

ALTER TABLE tb_club 
ADD COLUMN honors TEXT COMMENT '社团荣誉' AFTER description;

-- 如果需要为现有社团添加示例数据（可选）
-- UPDATE tb_club SET honors = '2024年度优秀社团' WHERE club_id = 1;
