package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.RecruitApplication;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【tb_recruit_application(招新申请表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.RecruitApplication
*/
public interface RecruitApplicationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(RecruitApplication record);

    int insertSelective(RecruitApplication record);

    RecruitApplication selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RecruitApplication record);

    int updateByPrimaryKey(RecruitApplication record);

    List<RecruitApplication> selectAll();
    
    /**
     * 查询所有申请，关联用户表获取姓名
     */
    @Select("SELECT a.*, u.real_name as studentName " +
            "FROM tb_recruit_application a " +
            "LEFT JOIN sys_user u ON a.user_id = u.user_id " +
            "ORDER BY a.apply_time DESC")
    List<RecruitApplication> selectAllWithUserName();

}
