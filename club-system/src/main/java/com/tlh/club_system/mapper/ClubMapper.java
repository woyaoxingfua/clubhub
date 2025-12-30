package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Club;

/**
* @author Lenovo
* @description 针对表【tb_club(社团信息表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.Club
*/
public interface ClubMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Club record);

    int insertSelective(Club record);

    Club selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Club record);

    int updateByPrimaryKey(Club record);


    /**
     * 查询所有社团
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM tb_club")
    java.util.List<com.tlh.club_system.entity.Club> selectAll();

    @org.apache.ibatis.annotations.Select("SELECT * FROM tb_club WHERE president_id = #{presidentId}")
    java.util.List<com.tlh.club_system.entity.Club> selectByPresidentId(Long presidentId);
    
    /**
     * 统计社团成员数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM tb_club_member WHERE club_id = #{clubId} AND status = 1")
    int countMembers(Integer clubId);
    
    /**
     * 查询社团近期活动
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM tb_event WHERE club_id = #{clubId} AND audit_status = 3 ORDER BY start_time DESC LIMIT #{limit}")
    java.util.List<com.tlh.club_system.entity.Event> selectRecentEvents(@org.apache.ibatis.annotations.Param("clubId") Integer clubId, @org.apache.ibatis.annotations.Param("limit") int limit);
}
