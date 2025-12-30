package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Message;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 站内信控制器
 */
@RestController
@RequestMapping("/api/message")
@CrossOrigin
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 发送消息
     * @param message 消息对象
     * @return 操作结果
     */
    @PostMapping("/send")
    public Result<String> send(@RequestBody Message message) {
        return messageService.sendMessage(message);
    }

    /**
     * 获取收到的消息列表
     * @return 消息列表
     */
    @GetMapping("/received")
    public Result<List<Message>> getReceivedMessages() {
        try {
            User currentUser = UserContext.getCurrentUser();
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            System.out.println("Fetching received messages for user: " + currentUser.getUserId());
            List<Message> messages = messageService.getReceivedMessages(currentUser.getUserId());
            return Result.success(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取发送的消息列表
     * @return 消息列表
     */
    @GetMapping("/sent")
    public Result<List<Message>> getSentMessages() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        List<Message> messages = messageService.getSentMessages(currentUser.getUserId());
        return Result.success(messages);
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PutMapping("/read/{messageId}")
    public Result<String> markAsRead(@PathVariable("messageId") Integer messageId) {
        return messageService.markAsRead(messageId);
    }

    /**
     * 批量标记为已读
     * @param params 包含 messageIds 的请求参数
     * @return 操作结果
     */
    @PutMapping("/read/batch")
    public Result<String> batchMarkAsRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> messageIds = (List<Integer>) params.get("messageIds");
        return messageService.batchMarkAsRead(messageIds);
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{messageId}")
    public Result<String> delete(@PathVariable("messageId") Integer messageId) {
        return messageService.deleteMessage(messageId);
    }

    /**
     * 获取未读消息数量
     * @return 未读数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        try {
            User currentUser = UserContext.getCurrentUser();
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            int count = messageService.getUnreadCount(currentUser.getUserId());
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近未读消息(用于首页通知)
     * @param limit 数量限制,默认5条
     * @return 消息列表
     */
    @GetMapping("/unread/recent")
    public Result<List<Message>> getRecentUnreadMessages(@RequestParam(defaultValue = "5") int limit) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        List<Message> messages = messageService.getRecentUnreadMessages(currentUser.getUserId(), limit);
        return Result.success(messages);
    }
}
