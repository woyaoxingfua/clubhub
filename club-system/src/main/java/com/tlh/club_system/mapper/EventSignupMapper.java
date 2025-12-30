package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.EventSignup;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【tb_event_signup(活动报名签到表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.EventSignup
*/
public interface EventSignupMapper {

    int deleteByPrimaryKey(Long id);

    int insert(EventSignup record);

    int insertSelective(EventSignup record);

    EventSignup selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(EventSignup record);

    int updateByPrimaryKey(EventSignup record);

    List<EventSignup> selectAll();

}
