package com.tlh.club_system.service.impl;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.Notice;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.NoticeMapper;
import com.tlh.club_system.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;
    
    @Resource
    private ClubMapper clubMapper;

    @Override
    public List<Notice> getList(Notice notice) {
        List<Notice> allNotices = noticeMapper.selectAllWithNames();
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null || isAdmin(currentUser)) {
            return allNotices;
        }
        
        Set<Integer> myClubIds = clubMapper.selectByPresidentId(Long.valueOf(currentUser.getUserId()))
                .stream()
                .map(Club::getClubId)
                .collect(Collectors.toSet());
        
        return allNotices.stream()
                .filter(n -> isVisible(n, myClubIds))
                .collect(Collectors.toList());
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Notice notice) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }
        
        notice.setPublisherId(currentUser.getUserId());
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        if (notice.getIsPinned() == null) {
            notice.setIsPinned(0);
        }
        
        if (notice.getTargetType() == 2) {
            if (!isAdmin(currentUser)) {
                Club club = clubMapper.selectByPrimaryKey(Long.valueOf(notice.getClubId()));
                if (club == null || !club.getPresidentId().equals(currentUser.getUserId())) {
                    throw new RuntimeException("权限不足：只有该社团社长可以发布社团公告");
                }
            }
        } else {
            if (!isAdmin(currentUser)) {
                throw new RuntimeException("权限不足：只有管理员可以发布校级或院系公告");
            }
        }
        
        noticeMapper.insertSelective(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Notice notice) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }
        
        Notice existingNotice = noticeMapper.selectByPrimaryKey(Long.valueOf(notice.getNoticeId()));
        if (existingNotice == null) {
            throw new RuntimeException("公告不存在");
        }
        
        if (!isAdmin(currentUser) && !existingNotice.getPublisherId().equals(currentUser.getUserId())) {
            throw new RuntimeException("权限不足：只能修改自己发布的公告");
        }
        
        noticeMapper.updateByPrimaryKeySelective(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }
        
        Notice existingNotice = noticeMapper.selectByPrimaryKey(id);
        if (existingNotice == null) {
            throw new RuntimeException("公告不存在");
        }
        
        if (!isAdmin(currentUser) && !existingNotice.getPublisherId().equals(currentUser.getUserId())) {
            throw new RuntimeException("权限不足：只能删除自己发布的公告");
        }
        
        noticeMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public Result<String> pin(Integer noticeId, Integer isPinned) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        if (!isAdmin(currentUser)) {
            return Result.error("权限不足：只有管理员可以置顶公告");
        }
        
        noticeMapper.updatePinStatus(noticeId, isPinned);
        return Result.success(isPinned == 1 ? "已置顶" : "已取消置顶");
    }
    
    private boolean isAdmin(User user) {
        if (user == null) return false;
        String role = user.getRoleKey();
        return "SYS_ADMIN".equals(role) || "DEPT_ADMIN".equals(role);
    }

    private boolean isVisible(Notice notice, Set<Integer> myClubIds) {
        // 全校可见
        if (notice.getTargetType() == 0) return true;
        // 社团公告且是我的社团
        return notice.getTargetType() == 2 && myClubIds.contains(notice.getClubId());
    }
}
