package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import com.tlh.club_system.entity.Club;
import com.tlh.club_system.mapper.ClubMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/club")
@CrossOrigin
public class ClubController {

    // 为了赶进度，这里直接注入 Mapper (简单的 CRUD 可以跳过 Service 层，符合快速开发原则)
    @Resource
    private ClubMapper clubMapper;
    
    @Resource
    private com.tlh.club_system.mapper.UserMapper userMapper;
    
    @Resource
    private com.tlh.club_system.mapper.DeptMapper deptMapper;
    
    @Resource
    private com.tlh.club_system.mapper.ClubMemberMapper clubMemberMapper;

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
        // 如果前端没传deptId，才默认0
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
    public Result<java.util.List<com.tlh.club_system.dto.ClubPortalDTO>> portal(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {
        List<Club> clubs = clubMapper.selectAll();
        
        // 筛选：只返回状态不为null的社团（status=0为待审核，status=1为正常，都应该展示）
        java.util.stream.Stream<Club> stream = clubs.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() >= 0);
        
        // 关键词搜索（社团名称或简介）
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
        
        // 转换为DTO并添加成员数量
        java.util.List<com.tlh.club_system.dto.ClubPortalDTO> result = 
            stream.map(club -> {
                com.tlh.club_system.dto.ClubPortalDTO dto = 
                    com.tlh.club_system.dto.ClubPortalDTO.fromClub(club);
                // 统计成员数量
                dto.setMemberCount(clubMapper.countMembers(club.getClubId()));
                return dto;
            }).collect(java.util.stream.Collectors.toList());
        
        return Result.success(result);
    }
    
    /**
     * 社团详情（包含成员数量统计和关联信息）
     */
    @GetMapping("/detail/{clubId}")
    public Result<java.util.Map<String, Object>> detail(@PathVariable("clubId") Integer clubId) {
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        // 构建详情DTO
        com.tlh.club_system.dto.ClubDetailDTO clubDetail = 
            com.tlh.club_system.dto.ClubDetailDTO.fromClub(club);
        
        // 社长联系方式
        java.util.Map<String, String> presidentContact = new java.util.HashMap<>();
        
        // 查询社长姓名和联系方式
        if (club.getPresidentId() != null) {
            com.tlh.club_system.entity.User president = 
                userMapper.selectByPrimaryKey(Long.valueOf(club.getPresidentId()));
            if (president != null) {
                clubDetail.setPresidentName(president.getRealName());
                // 添加联系方式
                if (president.getPhone() != null) {
                    presidentContact.put("phone", president.getPhone());
                }
                if (president.getEmail() != null) {
                    presidentContact.put("email", president.getEmail());
                }
            }
        }
        
        // 查询指导老师姓名
        if (club.getAdvisorId() != null) {
            com.tlh.club_system.entity.User advisor = 
                userMapper.selectByPrimaryKey(Long.valueOf(club.getAdvisorId()));
            if (advisor != null) {
                clubDetail.setAdvisorName(advisor.getRealName());
            }
        }
        
        // 查询院系名称
        if (club.getDeptId() != null && club.getDeptId() > 0) {
            com.tlh.club_system.entity.Dept dept = 
                deptMapper.selectByPrimaryKey(Long.valueOf(club.getDeptId()));
            if (dept != null) {
                clubDetail.setDeptName(dept.getDeptName());
            }
        }
        
        // 统计成员数量
        int memberCount = clubMapper.countMembers(clubId);
        
        // 查询近期活动（最近5个已发布的活动）
        List<com.tlh.club_system.entity.Event> recentEvents = 
            clubMapper.selectRecentEvents(clubId, 5);
        
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("club", clubDetail);
        data.put("memberCount", memberCount);
        data.put("recentEvents", recentEvents);
        data.put("presidentContact", presidentContact);
        
        return Result.success(data);
    }
    
    /**
     * 社长转让（仅管理员可用）
     * 此功能同时更新：
     * 1. tb_club.president_id（真正的社长标识）
     * 2. 旧社长的 ClubMember 记录（降级为副社长）
     * 3. 新社长的 ClubMember 记录（升级为社长）
     */
    @PostMapping("/transfer-president")
    public Result<String> transferPresident(@RequestBody java.util.Map<String, Object> params) {
        // 1. 权限检查 - 仅管理员可用
        com.tlh.club_system.entity.User currentUser = com.tlh.club_system.common.UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        
        String roleKey = currentUser.getRoleKey();
        if (!"SYS_ADMIN".equals(roleKey) && !"DEPT_ADMIN".equals(roleKey)) {
            return Result.error("权限不足：只有系统管理员或院系管理员才能转让社长职位");
        }
        
        // 2. 获取参数
        Integer clubId = (Integer) params.get("clubId");
        Integer newPresidentId = (Integer) params.get("newPresidentId");
        
        if (clubId == null || newPresidentId == null) {
            return Result.error("参数错误：缺少社团ID或新社长ID");
        }
        
        // 3. 查询社团信息
        Club club = clubMapper.selectByPrimaryKey(Long.valueOf(clubId));
        if (club == null) {
            return Result.error("社团不存在");
        }
        
        Integer oldPresidentId = club.getPresidentId();
        
        // 4. 检查新社长是否是该社团成员
        com.tlh.club_system.entity.ClubMember newPresidentMember = 
            clubMemberMapper.selectByClubAndUser(clubId, Long.valueOf(newPresidentId));
        
        if (newPresidentMember == null) {
            return Result.error("新社长必须先加入该社团");
        }
        
        if (newPresidentMember.getStatus() == 0) {
            return Result.error("新社长已离社，无法任命");
        }
        
        // 5. 更新 tb_club 表的 president_id（核心操作）
        club.setPresidentId(newPresidentId);
        clubMapper.updateByPrimaryKeySelective(club);
        
        // 6. 更新旧社长的成员记录（如果存在）
        if (oldPresidentId != null && !oldPresidentId.equals(newPresidentId)) {
            com.tlh.club_system.entity.ClubMember oldPresidentMember = 
                clubMemberMapper.selectByClubAndUser(clubId, Long.valueOf(oldPresidentId));
            
            if (oldPresidentMember != null) {
                // 将旧社长降级为副社长，保留admin权限
                clubMemberMapper.updateDepartmentAndPosition(
                    oldPresidentMember.getId(), 
                    "管理层", 
                    "副社长"
                );
                clubMemberMapper.assignRoleGroup(oldPresidentMember.getId(), "admin");
            }
        }
        
        // 7. 更新新社长的成员记录
        clubMemberMapper.updateDepartmentAndPosition(
            newPresidentMember.getId(), 
            "管理层", 
            "社长"
        );
        clubMemberMapper.assignRoleGroup(newPresidentMember.getId(), "admin");
        
        return Result.success("社长转让成功！新社长已生效");
    }
}