package com.sxtreh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxtreh.entity.FileInfo;
import com.sxtreh.entity.UserFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface NetDiskMapper extends BaseMapper<UserFile> {
//    @Options(keyProperty = "id", useGeneratedKeys = true)
//    @Insert("insert into t_net_disk_user_file (user_id, file_pid, file_name, file_type, gmt_create, gmt_update) " +
//            "VALUES (#{userId}, #{filePid}, #{fileName}, #{fileType}, #{gmtCreate}, #{gmtUpdate})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    @Insert("insert into t_net_disk_user_file (user_id, file_id, file_pid, file_name, file_size, file_type, file_url, gmt_create, gmt_update) " +
            "VALUES (#{userId}, #{fileId}, #{filePid}, #{fileName}, #{fileSize}, #{fileType}, #{fileUrl}, #{gmtCreate}, #{gmtUpdate})")
    void insertAndGetId(UserFile userFile);
}
