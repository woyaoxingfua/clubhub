package com.tlh.club_system.service;

import com.tlh.club_system.entity.User;
import java.util.List;

/**
 * 用户业务接口
 */
public interface UserService {

    // ==========================================
    // 1. MyBatisX 生成的基础 CRUD 方法
    // (如果你的Mapper生成时ID是Integer，这里保持Integer；如果是Long请自行对应)
    // ==========================================

    int deleteByPrimaryKey(Integer userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    // ==========================================
    // 2. 我们手动新增的业务方法
    // ==========================================
    
    List<User> listAll();

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，失败返回 null
     */
    User login(String username, String password);

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功的用户对象，失败返回 null
     */
    User register(User user);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true=已存在, false=不存在
     */
    boolean isUsernameExists(String username);

}