package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Finance;

import java.util.List;
import java.util.Map;

/**
 * 财务管理服务接口
 */
public interface FinanceService {
    
    /**
     * 查询财务列表（根据权限过滤）
     * @param clubId 社团ID，可选。如果为null则根据当前用户权限返回
     * @return 财务记录列表
     */
    List<Finance> listFinances(Integer clubId);
    
    /**
     * 添加财务记录
     * @param finance 财务记录
     * @return 操作结果
     */
    Result<String> addFinance(Finance finance);
    
    /**
     * 修改财务记录
     * @param finance 财务记录
     * @return 操作结果
     */
    Result<String> updateFinance(Finance finance);
    
    /**
     * 删除财务记录
     * @param financeId 财务记录ID
     * @return 操作结果
     */
    Result<String> deleteFinance(Integer financeId);
    
    /**
     * 获取财务统计信息
     * @param clubId 社团ID
     * @return 统计信息（总收入、总支出、余额）
     */
    Map<String, Object> getFinanceSummary(Integer clubId);
}
