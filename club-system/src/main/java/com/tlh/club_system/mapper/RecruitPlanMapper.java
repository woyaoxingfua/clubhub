package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.RecruitPlan;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【tb_recruit_plan(社团招新计划表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.RecruitPlan
*/
public interface RecruitPlanMapper {

    int deleteByPrimaryKey(Long id);

    int insert(RecruitPlan record);

    int insertSelective(RecruitPlan record);

    RecruitPlan selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RecruitPlan record);

    int updateByPrimaryKey(RecruitPlan record);

    List<RecruitPlan> selectAll();

}
