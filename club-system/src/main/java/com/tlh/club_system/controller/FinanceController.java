package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Finance;
import com.tlh.club_system.service.FinanceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财务管理控制器
 */
@RestController
@RequestMapping("/api/finance")
@CrossOrigin
public class FinanceController {

    @Resource
    private FinanceService financeService;

    /**
     * 查询财务列表
     * @param clubId 社团ID（可选，用于筛选）
     * @return 财务记录列表
     */
    @GetMapping("/list")
    public Result<List<Finance>> list(@RequestParam(value = "clubId", required = false) Integer clubId) {
        try {
            System.out.println("FinanceController.list: clubId=" + clubId);
            List<Finance> finances = financeService.listFinances(clubId);
            return Result.success(finances);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取财务列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加财务记录
     * @param finance 财务记录
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Finance finance) {
        return financeService.addFinance(finance);
    }

    /**
     * 修改财务记录
     * @param finance 财务记录
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Finance finance) {
        return financeService.updateFinance(finance);
    }

    /**
     * 删除财务记录
     * @param financeId 财务记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{financeId}")
    public Result<String> delete(@PathVariable("financeId") Integer financeId) {
        return financeService.deleteFinance(financeId);
    }

    /**
     * 获取财务统计
     * @param clubId 社团ID
     * @return 统计信息（总收入、总支出、余额）
     */
    @GetMapping("/summary/{clubId}")
    public Result<Map<String, Object>> getSummary(@PathVariable("clubId") Integer clubId) {
        try {
            Map<String, Object> summary = financeService.getFinanceSummary(clubId);
            if (summary.containsKey("error")) {
                return Result.error((String) summary.get("error"));
            }
            return Result.success(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取财务统计失败: " + e.getMessage());
        }
    }
}
