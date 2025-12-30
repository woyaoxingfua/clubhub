package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.RecruitApplication;
import com.tlh.club_system.entity.RecruitPlan;
import java.util.List;

public interface RecruitService {

    // --- 招新计划 (Plan) ---
    List<RecruitPlan> listPlans(Integer clubId);
    RecruitPlan getPlan(Integer planId);
    Result<String> createPlan(RecruitPlan plan);
    Result<String> updatePlan(RecruitPlan plan);
    Result<String> deletePlan(Integer planId);

    // --- 申请 (Application) ---
    Result<String> submitApplication(RecruitApplication application);
    List<RecruitApplication> listApplications(Integer planId);
    Result<String> auditApplication(Integer appId, Integer status);
}
