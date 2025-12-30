package com.tlh.club_system.mapper;

import com.tlh.club_system.entity.File;

/**
* @author Lenovo
* @description 针对表【tb_file(文件附件表)】的数据库操作Mapper
* @createDate 2025-12-25 15:09:38
* @Entity com.tlh.club_system.entity.File
*/
public interface FileMapper {

    int deleteByPrimaryKey(Long id);

    int insert(File record);

    int insertSelective(File record);

    File selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(File record);

    int updateByPrimaryKey(File record);

}
