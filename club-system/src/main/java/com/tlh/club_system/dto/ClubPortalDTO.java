package com.tlh.club_system.dto;

import com.tlh.club_system.entity.Club;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团门户DTO - 用于社团黄页展示
 */
@Data
public class ClubPortalDTO {
    
    private Integer clubId;
    private String clubName;
    private String category;
    private String logoUrl;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    
    // 统计信息
    private Integer memberCount;    // 成员数量
    
    /**
     * 从Club实体构造DTO
     */
    public static ClubPortalDTO fromClub(Club club) {
        ClubPortalDTO dto = new ClubPortalDTO();
        dto.setClubId(club.getClubId());
        dto.setClubName(club.getClubName());
        // 处理category为null的情况，默认为"综合类"
        dto.setCategory(club.getCategory() != null && !club.getCategory().trim().isEmpty() 
                        ? club.getCategory() : "综合类");
        // 处理logoUrl为null的情况，设置默认图标
        dto.setLogoUrl(club.getLogoUrl() != null && !club.getLogoUrl().trim().isEmpty()
                        ? club.getLogoUrl() : "https://img.icons8.com/clouds/200/000000/community.png");
        dto.setDescription(club.getDescription());
        dto.setStatus(club.getStatus());
        dto.setCreateTime(club.getCreateTime());
        return dto;
    }
}
