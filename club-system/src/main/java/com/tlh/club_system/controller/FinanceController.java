package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Finance;
import com.tlh.club_system.service.FinanceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin
public class FinanceController {

    @Resource
    private FinanceService financeService;

    @GetMapping("/list")
    public Result<List<Finance>> list(@RequestParam(value = "clubId", required = false) Integer clubId) {
        return Result.success(financeService.listFinances(clubId));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Finance finance) {
        return financeService.addFinance(finance);
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Finance finance) {
        return financeService.updateFinance(finance);
    }

    @DeleteMapping("/delete/{financeId}")
    public Result<String> delete(@PathVariable("financeId") Integer financeId) {
        return financeService.deleteFinance(financeId);
    }

    @GetMapping("/summary/{clubId}")
    public Result<Map<String, Object>> getSummary(@PathVariable("clubId") Integer clubId) {
        Map<String, Object> summary = financeService.getFinanceSummary(clubId);
        if (summary.containsKey("error")) {
            return Result.error((String) summary.get("error"));
        }
        return Result.success(summary);
    }
}
