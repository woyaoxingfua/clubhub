package com.tlh.club_system.service;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Notice;
import java.util.List;

/**
 * NoticeService interface
 */
public interface NoticeService {
    List<Notice> getList(Notice notice);
    
    Notice getById(Long id);

    void add(Notice notice);

    void update(Notice notice);

    void delete(Long id);
    
    Result<String> pin(Integer noticeId, Integer isPinned);
}
