package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Message;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.MessageMapper;
import com.tlh.club_system.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内信服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> sendMessage(Message message) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        // 参数校验
        if (message.getReceiverId() == null) {
            return Result.error("请选择收件人");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        // 设置发送人和时间
        message.setSenderId(currentUser.getUserId());
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0); // 默认未读

        // 如果没有主题，使用默认主题
        if (message.getSubject() == null || message.getSubject().trim().isEmpty()) {
            message.setSubject("新消息");
        }

        messageMapper.insertSelective(message);
        return Result.success("消息发送成功");
    }

    @Override
    public List<Message> getReceivedMessages(Integer userId) {
        return messageMapper.selectReceivedMessages(userId);
    }

    @Override
    public List<Message> getSentMessages(Integer userId) {
        return messageMapper.selectSentMessages(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> markAsRead(Integer messageId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        // 查询消息
        Message message = messageMapper.selectByPrimaryKey(Long.valueOf(messageId));
        if (message == null) {
            return Result.error("消息不存在");
        }

        // 权限校验：只有接收人才能标记为已读
        if (!message.getReceiverId().equals(currentUser.getUserId())) {
            return Result.error("权限不足：只能标记自己收到的消息");
        }

        messageMapper.markAsRead(messageId);
        return Result.success("已标记为已读");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchMarkAsRead(List<Integer> messageIds) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        if (messageIds == null || messageIds.isEmpty()) {
            return Result.error("请选择要标记的消息");
        }

        messageMapper.batchMarkAsRead(messageIds);
        return Result.success("批量标记成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteMessage(Integer messageId) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        // 查询消息
        Message message = messageMapper.selectByPrimaryKey(Long.valueOf(messageId));
        if (message == null) {
            return Result.error("消息不存在");
        }

        // 权限校验：只有发送人或接收人才能删除
        if (!message.getSenderId().equals(currentUser.getUserId()) &&
            !message.getReceiverId().equals(currentUser.getUserId())) {
            return Result.error("权限不足：只能删除自己的消息");
        }

        messageMapper.deleteMessage(messageId);
        return Result.success("消息已删除");
    }

    @Override
    public int getUnreadCount(Integer userId) {
        return messageMapper.countUnreadMessages(userId);
    }

    @Override
    public List<Message> getRecentUnreadMessages(Integer userId, int limit) {
        return messageMapper.selectRecentUnreadMessages(userId, limit);
    }
}
