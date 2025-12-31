package com.tlh.club_system.service.impl;

import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.UserMapper;
import com.tlh.club_system.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public int deleteByPrimaryKey(Integer userId) {
        return userMapper.deleteByPrimaryKey(Long.valueOf(userId));
    }

    @Override
    public int insert(User record) {
        return userMapper.insert(record);
    }

    @Override
    public int insertSelective(User record) {
        return userMapper.insertSelective(record);
    }

    @Override
    public User selectByPrimaryKey(Integer userId) {
        return userMapper.selectByPrimaryKey(Long.valueOf(userId));
    }

    @Override
    public int updateByPrimaryKeySelective(User record) {
        return userMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(User record) {
        return userMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectAll();
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null; 
        }
        if (!user.getPassword().equals(password)) {
            return null; 
        }
        return user;
    }

    @Override
    public User register(User user) {
        if (isUsernameExists(user.getUsername())) {
            return null;
        }
        
        if (user.getRoleKey() == null || user.getRoleKey().isEmpty()) {
            user.setRoleKey("STUDENT");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        int result = userMapper.insertSelective(user);
        if (result > 0) {
            return userMapper.selectByUsername(user.getUsername());
        }
        return null;
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.countByUsername(username) > 0;
    }
}
