package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.common.UserContext;
import com.tlh.club_system.dto.ClubDetailDTO;
import com.tlh.club_system.dto.ClubPortalDTO;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.entity.ClubMember;
import com.tlh.club_system.entity.Dept;
import com.tlh.club_system.entity.Event;
import com.tlh.club_system.entity.User;
import com.tlh.club_system.mapper.ClubMapper;
import com.tlh.club_system.mapper.ClubMemberMapper;
import com.tlh.club_system.mapper.DeptMapper;
import com.tlh.club_system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/club")
@CrossOrigin
public class ClubController {

    @Resource
    private ClubMapper clubMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private DeptMapper deptMapper;
    
    @Resource
    private ClubMemberMapper clubMemberMapper;

    /**
     * 社团列表
     */
    @GetMapping("/list")
    public Result<List<Club>> list() {
        return Result.success(clubMapper.selectAll());
    }

    /**
     * 新增社团 (仅管理员)
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Club club) {
        club.setCreateTime(LocalDateTime.now());
        if (club.getStatus() == null) {
            club.setStatus(0);
        }
        if (club.getBalance() == null) {
            club.setBalance(BigDecimal.ZERO);
        }
        if (club.getDeptId() == null) {
            club.setDeptId(0); 
        }
        clubMapper.insertSelective(club);
        return Result.success("社团创建成功");
    }

    /**
     * 修改社团
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Club club) {
        clubMapper.updateByPrimaryKeySelective(club);
        return Result.success("社团信息更新成功");
    }

    /**
     * 删除社团
     */
    @DeleteMapping("/{clubId}")
    public Result<String> delete(@PathVariable("clubId") Integer clubId) {
        clubMapper.deleteByPrimaryKey(Long.valueOf(clubId));
        return Result.success("社团已解散");
    }

    /**
     * 获取我管理的社团 (我是社长)
     */
    @GetMapping("/my-clubs")
    public Result<List<Club>> getMyClubs(@RequestParam("userId") Long userId) {
        return Result.success(clubMapper.selectByPresidentId(userId));
    }
    
    /**
     * 社团门户 - 黄页接口（支持搜索和分类筛选）
     */
    @GetMapping("/portal")
    public Result<List<ClubPortalDTO>> portal(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {
        List<Club> clubs = clubMapper.selectAll();
        
        // 筛选：只返回状态不为null的社团
        Stream<Club> stream = clubs.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() >= 0);
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            final String kw = keyword.trim().toLowerCase();
            stream = stream.filter(c -> 
                (c.getClubName() != null && c.getClubName().toLowerCase().contains(kw)) ||
                (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw))
            );
        }
        
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            stream = stream.filter(c -> category.equals(c.getCategory()));
        }
        
        List<ClubPortalDTO> result = stream.map(club -> {
            ClubPortalDTO dto = ClubPortalDTO.fromClub(club);
            dto.setMemberCount(clubMapper.countMembers(club.getClubId()));
            return dto;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
    
    /**
     * 社团详情
     */
    @GetMapping("/detail/{clubId}")
    public Result<Map<String, Object>> detail(@PathVariable("clubId") Integer clubId) {
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        ClubDetailDTO clubDetail = ClubDetailDTO.fromClub(club);
        Map<String, String> presidentContact = new HashMap<>();
        
        // 查询社长信息
        if (club.getPresidentId() != null) {
            User president = userMapper.selectByPrimaryKey(Long.valueOf(club.getPresidentId()));
            if (president != null) {
                clubDetail.setPresidentName(president.getRealName());
                if (president.getPhone() != null) presidentContact.put("phone", president.getPhone());
                if (president.getEmail() != null) presidentContact.put("email", president.getEmail());
            }
        }
        
        // 查询指导老师
        if (club.getAdvisorId() != null) {
            User advisor = userMapper.selectByPrimaryKey(Long.valueOf(club.getAdvisorId()));
            if (advisor != null) {
                clubDetail.setAdvisorName(advisor.getRealName());
            }
        }
        
        // 查询院系
        if (club.getDeptId() != null && club.getDeptId() > 0) {
            Dept dept = deptMapper.selectByPrimaryKey(Long.valueOf(club.getDeptId()));
            if (dept != null) {
                clubDetail.setDeptName(dept.getDeptName());
            }
        }
        
        int memberCount = clubMapper.countMembers(clubId);
        List<Event> recentEvents = clubMapper.selectRecentEvents(clubId, 5);
        
        Map<String, Object> data = new HashMap<>();
        data.put("club", clubDetail);
        data.put("memberCount", memberCount);
        data.put("recentEvents", recentEvents);
        data.put("presidentContact", presidentContact);
        
        return Result.success(data);
    }
    
    /**
     * 社长转让（仅管理员可用）
     */
    @PostMapping("/transfer-president")
    public Result<String> transferPresident(@RequestBody Map<String, Object> params) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        String roleKey = currentUser.getRoleKey();
        if (!"SYS_ADMIN".equals(roleKey) && !"DEPT_ADMIN".equals(roleKey)) {
            return Result.error("权限不足");
        }
        
        Integer clubId = (Integer) params.get("clubId");
        Integer newPresidentId = (Integer) params.get("newPresidentId");
        
        if (clubId == null || newPresidentId == null) {
            return Result.error("参数错误");
        }
        
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        Integer oldPresidentId = club.getPresidentId();
        
        // 检查新社长是否是该社团成员
        ClubMember newPresidentMember = clubMemberMapper.selectByClubAndUser(clubId, Long.valueOf(newPresidentId));
        
        if (newPresidentMember == null) {
            return Result.error("新社长必须先加入该社团");
        }
        
        if (newPresidentMember.getStatus() == 0) {
            return Result.error("新社长已离社");
        }
        
        // 更新社长
        club.setPresidentId(newPresidentId);
        clubMapper.updateByPrimaryKeySelective(club);
        
        // 降级旧社长
        if (oldPresidentId != null && !oldPresidentId.equals(newPresidentId)) {
            ClubMember oldPresidentMember = clubMemberMapper.selectByClubAndUser(clubId, Long.valueOf(oldPresidentId));
            if (oldPresidentMember != null) {
                clubMemberMapper.updateDepartmentAndPosition(oldPresidentMember.getId(), "管理层", "副社长");
                clubMemberMapper.assignRoleGroup(oldPresidentMember.getId(), "admin");
            }
        }
        
        // 升级新社长
        clubMemberMapper.updateDepartmentAndPosition(newPresidentMember.getId(), "管理层", "社长");
        clubMemberMapper.assignRoleGroup(newPresidentMember.getId(), "admin");
        
        return Result.success("社长转让成功");
    }
}