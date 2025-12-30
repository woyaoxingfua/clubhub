package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Dept;

/**
* @author Lenovo
* @description 针对表【sys_dept(学院信息表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.Dept
*/
public interface DeptMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Dept record);

    int insertSelective(Dept record);

    Dept selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Dept record);

    int updateByPrimaryKey(Dept record);

    @org.apache.ibatis.annotations.Select("SELECT * FROM sys_dept")
    java.util.List<Dept> selectAll();
}
