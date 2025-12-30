package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 站内信服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     * @param message 消息对象
     * @return 操作结果
     */
    Result<String> sendMessage(Message message);

    /**
     * 获取收到的消息列表
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> getReceivedMessages(Integer userId);

    /**
     * 获取发送的消息列表
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> getSentMessages(Integer userId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 操作结果
     */
    Result<String> markAsRead(Integer messageId);

    /**
     * 批量标记为已读
     * @param messageIds 消息ID列表
     * @return 操作结果
     */
    Result<String> batchMarkAsRead(List<Integer> messageIds);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 操作结果
     */
    Result<String> deleteMessage(Integer messageId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Integer userId);

    /**
     * 获取最近未读消息（用于首页通知）
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> getRecentUnreadMessages(Integer userId, int limit);
}
