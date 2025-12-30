package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Event;

/**
* @author Lenovo
* @description 针对表【tb_event(活动申请表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.Event
*/
public interface EventMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Event record);

    int insertSelective(Event record);

    Event selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Event record);

    int updateByPrimaryKey(Event record);


    // 【新增】查询所有活动，按时间倒序排列
    @org.apache.ibatis.annotations.Select("SELECT * FROM tb_event ORDER BY start_time DESC")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "eventId", column = "event_id"),
        @org.apache.ibatis.annotations.Result(property = "clubId", column = "club_id"),
        @org.apache.ibatis.annotations.Result(property = "title", column = "title"),
        @org.apache.ibatis.annotations.Result(property = "content", column = "content"),
        @org.apache.ibatis.annotations.Result(property = "location", column = "location"),
        @org.apache.ibatis.annotations.Result(property = "budget", column = "budget"),
        @org.apache.ibatis.annotations.Result(property = "startTime", column = "start_time"),
        @org.apache.ibatis.annotations.Result(property = "endTime", column = "end_time"),
        @org.apache.ibatis.annotations.Result(property = "maxPeople", column = "max_people"),
        @org.apache.ibatis.annotations.Result(property = "auditStatus", column = "audit_status"),
        @org.apache.ibatis.annotations.Result(property = "auditRemark", column = "audit_remark"),
        @org.apache.ibatis.annotations.Result(property = "summary", column = "summary"),
        @org.apache.ibatis.annotations.Result(property = "createTime", column = "create_time"),
        @org.apache.ibatis.annotations.Result(property = "createBy", column = "create_by"),
        @org.apache.ibatis.annotations.Result(property = "targetType", column = "target_type"),
        @org.apache.ibatis.annotations.Result(property = "targetDeptId", column = "target_dept_id")
    })
    java.util.List<com.tlh.club_system.entity.Event> selectAll();




}
