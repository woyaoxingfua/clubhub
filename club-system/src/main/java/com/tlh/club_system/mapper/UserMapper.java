package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.User;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【sys_user(用户信息表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.User
*/
public interface UserMapper {

    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);


    /**
     * 【新增】根据用户名查询用户
     * 使用注解方式，无需修改 XML
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM sys_user WHERE username = #{username}")
    com.tlh.club_system.entity.User selectByUsername(@org.apache.ibatis.annotations.Param("username") String username);

    /**
     * 【新增】检查用户名是否存在
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsername(@org.apache.ibatis.annotations.Param("username") String username);

    List<User> selectAll();

}
