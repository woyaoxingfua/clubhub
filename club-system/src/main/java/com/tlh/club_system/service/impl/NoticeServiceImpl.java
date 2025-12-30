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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;
    
    @Resource
    private ClubMapper clubMapper;

    @Override
    public List<Notice> getList(Notice notice) {
        // 使用关联查询，获取社团名称和发布人姓名
        List<Notice> allNotices = noticeMapper.selectAllWithNames();
        
        // 根据当前用户权限过滤
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return allNotices;
        }
        
        // 管理员可以看所有公告
        if (isAdmin(currentUser)) {
            return allNotices;
        }
        
        // 社长可以看：全校公告 + 自己社团的公告
        List<Club> myClubs = clubMapper.selectByPresidentId(Long.valueOf(currentUser.getUserId()));
        List<Integer> myClubIds = myClubs.stream().map(Club::getClubId).collect(Collectors.toList());
        
        return allNotices.stream()
                .filter(n -> n.getTargetType() == 0 || // 全校可见
                        (n.getTargetType() == 2 && myClubIds.contains(n.getClubId()))) // 本社可见且是我的社团
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
        
        // 设置发布人
        notice.setPublisherId(currentUser.getUserId());
        
        // 设置发布时间
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        
        // 设置默认不置顶
        if (notice.getIsPinned() == null) {
            notice.setIsPinned(0);
        }
        
        // 权限校验
        if (notice.getTargetType() == 2) {
            // 社团公告：检查是否是该社团的社长或管理员
            if (!isAdmin(currentUser)) {
                Club club = clubMapper.selectByPrimaryKey(Long.valueOf(notice.getClubId()));
                if (club == null || !club.getPresidentId().equals(currentUser.getUserId())) {
                    throw new RuntimeException("权限不足：只有该社团社长可以发布社团公告");
                }
            }
        } else {
            // 校级或院系公告：只有管理员可以发布
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
        
        // 权限校验：管理员或公告发布人
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
        
        // 权限校验
        Notice existingNotice = noticeMapper.selectByPrimaryKey(id);
        if (existingNotice == null) {
            throw new RuntimeException("公告不存在");
        }
        
        if (!isAdmin(currentUser) && !existingNotice.getPublisherId().equals(currentUser.getUserId())) {
            throw new RuntimeException("权限不足：只能删除自己发布的公告");
        }
        
        noticeMapper.deleteByPrimaryKey(id);
    }
    
    /**
     * 置顶/取消置顶
     */
    @Override
    public Result<String> pin(Integer noticeId, Integer isPinned) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        // 只有管理员可以置顶
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
}
