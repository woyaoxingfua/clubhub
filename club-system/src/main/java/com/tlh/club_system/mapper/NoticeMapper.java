package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Notice;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author Lenovo
* @description 针对表【tb_notice(通知公告表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.Notice
*/
public interface NoticeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Notice record);

    int insertSelective(Notice record);

    Notice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Notice record);

    int updateByPrimaryKey(Notice record);

    java.util.List<Notice> selectList(Notice notice);
    
    /**
     * 查询所有公告，关联社团名称和发布人姓名
     * 排序规则：先按置顶状态倒序，再按发布时间倒序
     */
    @Select("SELECT n.*, c.club_name as clubName, u.real_name as publisherName " +
            "FROM tb_notice n " +
            "LEFT JOIN tb_club c ON n.club_id = c.club_id " +
            "LEFT JOIN sys_user u ON n.publisher_id = u.user_id " +
            "ORDER BY COALESCE(n.is_pinned, 0) DESC, n.publish_time DESC")
    java.util.List<Notice> selectAllWithNames();
    
    /**
     * 置顶/取消置顶
     */
    @Update("UPDATE tb_notice SET is_pinned = #{isPinned} WHERE notice_id = #{noticeId}")
    int updatePinStatus(Integer noticeId, Integer isPinned);
}

