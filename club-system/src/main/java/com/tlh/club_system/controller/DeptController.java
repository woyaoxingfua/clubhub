package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Dept;
import com.tlh.club_system.service.DeptService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dept")
@CrossOrigin
public class DeptController {

    @Resource
    private DeptService deptService;

    @GetMapping("/list")
    public Result<List<Dept>> list() {
        return Result.success(deptService.listAll());
    }
}
