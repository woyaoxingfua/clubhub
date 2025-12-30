package com.tlh.club_system.service.impl;

import com.tlh.club_system.entity.Dept;
import com.tlh.club_system.mapper.DeptMapper;
import com.tlh.club_system.service.DeptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<Dept> listAll() {
        return deptMapper.selectAll();
    }
}
