package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Notice;
import com.tlh.club_system.service.NoticeService;
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

    @GetMapping("/list")
    public Result<List<Notice>> list(Notice notice) {
        return Result.success(noticeService.getList(notice));
    }

    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable("id") Long id) {
        return Result.success(noticeService.getById(id));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Notice notice) {
        try {
            noticeService.add(notice);
            return Result.success("公告发布成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Notice notice) {
        try {
            noticeService.update(notice);
            return Result.success("公告修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") Long id) {
        try {
            noticeService.delete(id);
            return Result.success("公告删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/pin")
    public Result<String> pin(@RequestBody Map<String, Integer> params) {
        Integer noticeId = params.get("noticeId");
        Integer isPinned = params.get("isPinned");
        return noticeService.pin(noticeId, isPinned);
    }
}
