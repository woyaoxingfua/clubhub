package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.EventSignup;
import java.util.List;

public interface EventSignupService {
    
    /**
     * 用户报名活动
     */
    Result<String> signup(Integer eventId, Integer userId);

    /**
     * 取消报名
     */
    Result<String> cancel(Integer signupId);

    /**
     * 查询某活动的所有报名记录
     */
    List<EventSignup> listByEvent(Integer eventId);

    /**
     * 查询某用户的所有报名记录
     */
    List<EventSignup> listByUser(Integer userId);

    /**
     * 审核/签到状态变更
     * @param status 1:确认报名/通过, 2:已签到
     */
    Result<String> updateStatus(Integer signupId, Integer status);
    
    /**
     * 检查用户是否已报名
     */
    boolean checkHasSignedUp(Integer eventId, Integer userId);
}
