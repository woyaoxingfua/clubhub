package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.RecruitApplication;
import com.tlh.club_system.entity.RecruitPlan;
import com.tlh.club_system.service.RecruitService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruit")
@CrossOrigin
public class RecruitController {

    @Resource
    private RecruitService recruitService;

    // --- Plan ---

    @GetMapping("/plan/list")
    public Result<List<RecruitPlan>> listPlans(@RequestParam(value = "clubId", required = false) Integer clubId) {
        List<RecruitPlan> all = recruitService.listPlans(clubId);
        // 内存过滤，临时方案
        if (clubId != null) {
            all = all.stream().filter(p -> p.getClubId().equals(clubId)).collect(Collectors.toList());
        }
        return Result.success(all);
    }

    @PostMapping("/plan/add")
    public Result<String> addPlan(@RequestBody RecruitPlan plan) {
        return recruitService.createPlan(plan);
    }

    @PutMapping("/plan/update")
    public Result<String> updatePlan(@RequestBody RecruitPlan plan) {
        return recruitService.updatePlan(plan);
    }

    @DeleteMapping("/plan/{planId}")
    public Result<String> deletePlan(@PathVariable("planId") Integer planId) {
        return recruitService.deletePlan(planId);
    }

    // --- Application ---

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody RecruitApplication application) {
        if (application.getUserId() == null) {
            return Result.error("用户信息缺失");
        }
        return recruitService.submitApplication(application);
    }

    @GetMapping("/application/list")
    public Result<List<RecruitApplication>> listApplications(@RequestParam(value = "planId", required = false) Integer planId) {
        List<RecruitApplication> all = recruitService.listApplications(planId);
        if (planId != null) {
            all = all.stream().filter(a -> a.getPlanId().equals(planId)).collect(Collectors.toList());
        }
        return Result.success(all);
    }

    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Object> params) {
        Integer appId = (Integer) params.get("appId");
        Integer status = (Integer) params.get("status");
        return recruitService.auditApplication(appId, status);
    }
}
