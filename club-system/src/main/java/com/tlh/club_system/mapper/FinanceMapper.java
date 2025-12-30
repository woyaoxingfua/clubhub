package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Finance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【tb_finance(经费收支明细表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.Finance
*/
public interface FinanceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Finance record);

    int insertSelective(Finance record);

    Finance selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Finance record);

    int updateByPrimaryKey(Finance record);
    
    /**
     * 查询所有财务记录，关联社团名称和活动名称
     */
    @Select("SELECT f.finance_id, f.club_id, f.event_id, f.type, f.amount, f.description, f.proof_url, f.status, f.create_time, " +
            "c.club_name as clubName, e.title as eventName " +
            "FROM tb_finance f " +
            "LEFT JOIN tb_club c ON f.club_id = c.club_id " +
            "LEFT JOIN tb_event e ON f.event_id = e.event_id " +
            "ORDER BY f.create_time DESC")
    List<Finance> selectAllWithNames();
    
    /**
     * 根据社团ID查询财务记录
     */
    @Select("SELECT f.finance_id, f.club_id, f.event_id, f.type, f.amount, f.description, f.proof_url, f.status, f.create_time, " +
            "c.club_name as clubName, e.title as eventName " +
            "FROM tb_finance f " +
            "LEFT JOIN tb_club c ON f.club_id = c.club_id " +
            "LEFT JOIN tb_event e ON f.event_id = e.event_id " +
            "WHERE f.club_id = #{clubId} " +
            "ORDER BY f.create_time DESC")
    List<Finance> selectByClubId(@Param("clubId") Integer clubId);
    
    /**
     * 计算某社团的财务统计（总收入、总支出）
     */
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN type = 1 AND status = 1 THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 2 AND status = 1 THEN amount ELSE 0 END), 0) as totalExpense " +
            "FROM tb_finance " +
            "WHERE club_id = #{clubId}")
    Map<String, Object> calculateSummary(@Param("clubId") Integer clubId);

}
