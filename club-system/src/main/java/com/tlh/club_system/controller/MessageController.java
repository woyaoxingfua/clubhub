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

@RestController
@RequestMapping("/api/message")
@CrossOrigin
public class MessageController {

    @Resource
    private MessageService messageService;

    @PostMapping("/send")
    public Result<String> send(@RequestBody Message message) {
        return messageService.sendMessage(message);
    }

    @GetMapping("/received")
    public Result<List<Message>> getReceivedMessages() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        return Result.success(messageService.getReceivedMessages(currentUser.getUserId()));
    }

    @GetMapping("/sent")
    public Result<List<Message>> getSentMessages() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        return Result.success(messageService.getSentMessages(currentUser.getUserId()));
    }

    @PutMapping("/read/{messageId}")
    public Result<String> markAsRead(@PathVariable("messageId") Integer messageId) {
        return messageService.markAsRead(messageId);
    }

    @PutMapping("/read/batch")
    public Result<String> batchMarkAsRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> messageIds = (List<Integer>) params.get("messageIds");
        return messageService.batchMarkAsRead(messageIds);
    }

    @DeleteMapping("/delete/{messageId}")
    public Result<String> delete(@PathVariable("messageId") Integer messageId) {
        return messageService.deleteMessage(messageId);
    }

    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        return Result.success(messageService.getUnreadCount(currentUser.getUserId()));
    }

    @GetMapping("/unread/recent")
    public Result<List<Message>> getRecentUnreadMessages(@RequestParam(defaultValue = "5") int limit) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        return Result.success(messageService.getRecentUnreadMessages(currentUser.getUserId(), limit));
    }
}
