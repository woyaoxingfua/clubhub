package com.tlh.club_system.dto;

import com.tlh.club_system.entity.Club;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社团详情DTO - 包含关联查询的完整信息
 */
@Data
public class ClubDetailDTO {
    
    // 基本信息（来自Club实体）
    private Integer clubId;
    private String clubName;
    private String category;
    private String logoUrl;
    private String description;
    private String honors;           // 社团荣誉
    private Integer presidentId;
    private Integer advisorId;
    private Integer deptId;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime createTime;
    
    // 关联查询的附加信息
    private String presidentName;    // 社长姓名
    private String advisorName;      // 指导老师姓名
    private String deptName;         // 所属院系名称
    
    /**
     * 从Club实体构造DTO
     */
    public static ClubDetailDTO fromClub(Club club) {
        ClubDetailDTO dto = new ClubDetailDTO();
        dto.setClubId(club.getClubId());
        dto.setClubName(club.getClubName());
        // 处理category为null的情况，默认为"综合类"
        dto.setCategory(club.getCategory() != null && !club.getCategory().trim().isEmpty() 
                        ? club.getCategory() : "综合类");
        // 处理logoUrl为null的情况，设置默认图标
        dto.setLogoUrl(club.getLogoUrl() != null && !club.getLogoUrl().trim().isEmpty()
                        ? club.getLogoUrl() : "https://img.icons8.com/clouds/200/000000/community.png");
        dto.setDescription(club.getDescription());
        dto.setHonors(club.getHonors());  // 添加荣誉字段
        dto.setPresidentId(club.getPresidentId());
        dto.setAdvisorId(club.getAdvisorId());
        dto.setDeptId(club.getDeptId());
        dto.setBalance(club.getBalance());
        dto.setStatus(club.getStatus());
        dto.setCreateTime(club.getCreateTime());
        return dto;
    }
}
