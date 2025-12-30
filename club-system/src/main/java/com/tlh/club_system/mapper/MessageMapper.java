package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @description 针对表【tb_message(站内信表)】的数据库操作Mapper
 */
public interface MessageMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    // ==================== 自定义方法 ====================

    /**
     * 获取我收到的消息列表（按时间倒序）
     */
    @Select("SELECT m.message_id, m.sender_id, m.receiver_id, m.club_id, m.subject, m.content, m.is_read, m.send_time, m.read_time, " +
            "sender.real_name as senderName, " +
            "receiver.real_name as receiverName, " +
            "c.club_name as clubName " +
            "FROM tb_message m " +
            "LEFT JOIN sys_user sender ON m.sender_id = sender.user_id " +
            "LEFT JOIN sys_user receiver ON m.receiver_id = receiver.user_id " +
            "LEFT JOIN tb_club c ON m.club_id = c.club_id " +
            "WHERE m.receiver_id = #{userId} " +
            "ORDER BY m.send_time DESC")
    List<Message> selectReceivedMessages(@Param("userId") Integer userId);

    /**
     * 获取我发送的消息列表（按时间倒序）
     */
    @Select("SELECT m.message_id, m.sender_id, m.receiver_id, m.club_id, m.subject, m.content, m.is_read, m.send_time, m.read_time, " +
            "sender.real_name as senderName, " +
            "receiver.real_name as receiverName, " +
            "c.club_name as clubName " +
            "FROM tb_message m " +
            "LEFT JOIN sys_user sender ON m.sender_id = sender.user_id " +
            "LEFT JOIN sys_user receiver ON m.receiver_id = receiver.user_id " +
            "LEFT JOIN tb_club c ON m.club_id = c.club_id " +
            "WHERE m.sender_id = #{userId} " +
            "ORDER BY m.send_time DESC")
    List<Message> selectSentMessages(@Param("userId") Integer userId);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE tb_message SET is_read = 1, read_time = NOW() WHERE message_id = #{messageId}")
    int markAsRead(@Param("messageId") Integer messageId);

    /**
     * 批量标记为已读
     */
    @Update("<script>" +
            "UPDATE tb_message SET is_read = 1, read_time = NOW() " +
            "WHERE message_id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchMarkAsRead(@Param("messageIds") List<Integer> messageIds);

    /**
     * 统计未读消息数量
     */
    @Select("SELECT COUNT(*) FROM tb_message WHERE receiver_id = #{userId} AND is_read = 0")
    int countUnreadMessages(@Param("userId") Integer userId);

    /**
     * 删除消息（实际删除）
     */
    @Delete("DELETE FROM tb_message WHERE message_id = #{messageId}")
    int deleteMessage(@Param("messageId") Integer messageId);

    /**
     * 获取最近的消息（用于首页通知）
     */
    @Select("SELECT m.*, sender.real_name as senderName " +
            "FROM tb_message m " +
            "LEFT JOIN sys_user sender ON m.sender_id = sender.user_id " +
            "WHERE m.receiver_id = #{userId} AND m.is_read = 0 " +
            "ORDER BY m.send_time DESC " +
            "LIMIT #{limit}")
    List<Message> selectRecentUnreadMessages(@Param("userId") Integer userId, @Param("limit") int limit);
}
