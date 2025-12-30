package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.ClubMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【tb_club_member(社团成员关联表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.ClubMember
*/
public interface ClubMemberMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ClubMember record);

    int insertSelective(ClubMember record);

    ClubMember selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ClubMember record);

    int updateByPrimaryKey(ClubMember record);

    // ==================== 原有方法 ====================
    
    // 1. 根据 club_id 和 user_id 查记录 (不管状态是0还是1，都查出来)
    @Select("SELECT * FROM tb_club_member WHERE club_id = #{clubId} AND user_id = #{userId} LIMIT 1")
    ClubMember selectByClubAndUser(@Param("clubId") Integer clubId, @Param("userId") Long userId);

    // 2. 重新加入 (以前退社过，现在把状态改回 1)
    @Update("UPDATE tb_club_member SET status = 1, join_time = NOW(), quit_type = NULL, quit_reason = NULL, quit_time = NULL WHERE id = #{id}")
    int reJoin(Integer id);

    // 3. 查看在社成员列表 (只查 status = 1，并关联查名字)
    @Select("SELECT m.*, u.real_name as studentName, u.real_name as userName, " +
            "u.phone, u.email " +
            "FROM tb_club_member m " +
            "LEFT JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.club_id = #{clubId} AND m.status = 1")
    List<ClubMember> selectActiveMembers(Integer clubId);

    // 4. 踢人 (软删除：状态改为 0) - 保留原有方法，但建议使用新的 dismiss 方法
    @Update("UPDATE tb_club_member SET status = 0 WHERE id = #{id}")
    int quit(Integer id);
    
    // ==================== 新增方法 ====================
    
    // 5. 查询完整成员花名册（包含所有状态，关联学生姓名和社团名称）
    @Select("SELECT m.*, u.real_name as studentName, u.real_name as userName, " +
            "u.phone, u.email, c.club_name as clubName " +
            "FROM tb_club_member m " +
            "LEFT JOIN sys_user u ON m.user_id = u.user_id " +
            "LEFT JOIN tb_club c ON m.club_id = c.club_id " +
            "WHERE m.club_id = #{clubId} " +
            "ORDER BY m.status DESC, m.join_time DESC")
    List<ClubMember> selectFullRoster(Integer clubId);
    
    // 6. 按状态筛选查询成员
    @Select("SELECT m.*, u.real_name as studentName, u.real_name as userName, " +
            "u.phone, u.email " +
            "FROM tb_club_member m " +
            "LEFT JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.club_id = #{clubId} AND m.status = #{status} " +
            "ORDER BY m.join_time DESC")
    List<ClubMember> selectByClubAndStatus(@Param("clubId") Integer clubId, @Param("status") Integer status);
    
    // 7. 按部门查询成员
    @Select("SELECT m.*, u.real_name as studentName, u.real_name as userName, " +
            "u.phone, u.email " +
            "FROM tb_club_member m " +
            "LEFT JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.club_id = #{clubId} AND m.department = #{department} AND m.status = 1 " +
            "ORDER BY m.join_time DESC")
    List<ClubMember> selectByDepartment(@Param("clubId") Integer clubId, @Param("department") String department);
    
    // 8. 按权限组查询成员
    @Select("SELECT m.*, u.real_name as studentName, u.real_name as userName, " +
            "u.phone, u.email " +
            "FROM tb_club_member m " +
            "LEFT JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.club_id = #{clubId} AND m.role_group = #{roleGroup} AND m.status = 1 " +
            "ORDER BY m.join_time DESC")
    List<ClubMember> selectByRoleGroup(@Param("clubId") Integer clubId, @Param("roleGroup") String roleGroup);
    
    // 9. 更新成员部门和职务
    @Update("UPDATE tb_club_member SET department = #{department}, position = #{position} WHERE id = #{id}")
    int updateDepartmentAndPosition(@Param("id") Integer id, @Param("department") String department, @Param("position") String position);
    
    // 10. 分配权限组
    @Update("UPDATE tb_club_member SET role_group = #{roleGroup} WHERE id = #{id}")
    int assignRoleGroup(@Param("id") Integer id, @Param("roleGroup") String roleGroup);
    
    // 11. 主动退出（状态改为0，记录退出原因和时间）
    @Update("UPDATE tb_club_member SET status = 0, quit_type = 1, quit_reason = #{reason}, quit_time = NOW() WHERE id = #{id}")
    int quitWithReason(@Param("id") Integer id, @Param("reason") String reason);
    
    // 12. 被开除（状态改为0，记录开除原因和时间）
    @Update("UPDATE tb_club_member SET status = 0, quit_type = 2, quit_reason = #{reason}, quit_time = NOW() WHERE id = #{id}")
    int dismiss(@Param("id") Integer id, @Param("reason") String reason);
    
    // 13. 统计某社团的在社成员数
    @Select("SELECT COUNT(*) FROM tb_club_member WHERE club_id = #{clubId} AND status = 1")
    int countActiveMembers(Integer clubId);
    
    // 14. 统计某社团各部门的人数
    @Select("SELECT department, COUNT(*) as count FROM tb_club_member " +
            "WHERE club_id = #{clubId} AND status = 1 " +
            "GROUP BY department")
    List<java.util.Map<String, Object>> countByDepartment(Integer clubId);
    
    // 15. 获取用户加入的所有社团（用于通讯录）
    @Select("SELECT DISTINCT c.club_id as clubId, c.club_name as clubName, c.president_id as presidentId " +
            "FROM tb_club_member m " +
            "JOIN tb_club c ON m.club_id = c.club_id " +
            "WHERE m.user_id = #{userId} AND m.status = 1")
    List<java.util.Map<String, Object>> selectMyMemberships(Integer userId);
}
