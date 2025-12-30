package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Notice;
import com.tlh.club_system.service.NoticeService;
import com.tlh.club_system.service.impl.NoticeServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 获取公告列表
     * 支持 title 模糊查询
     */
    @GetMapping("/list")
    public Result<List<Notice>> list(Notice notice) {
        List<Notice> list = noticeService.getList(notice);
        return Result.success(list);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable("id") Long id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    /**
     * 发布公告
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Notice notice) {
        try {
            noticeService.add(notice);
            return Result.success("公告发布成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改公告
     */
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Notice notice) {
        try {
            noticeService.update(notice);
            return Result.success("公告修改成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") Long id) {
        try {
            noticeService.delete(id);
            return Result.success("公告删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 置顶/取消置顶
     */
    @PutMapping("/pin")
    public Result<String> pin(@RequestBody Map<String, Integer> params) {
        Integer noticeId = params.get("noticeId");
        Integer isPinned = params.get("isPinned");
        return noticeService.pin(noticeId, isPinned);
    }
}
