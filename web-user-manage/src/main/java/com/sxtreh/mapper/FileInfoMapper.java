package com.sxtreh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxtreh.entity.FileInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface FileInfoMapper extends BaseMapper<FileInfo> {
    @Update("update t_net_disk_file_info set pointer_number = pointer_number - 1 where id = #{fileId}")
    void decreasePointNumber(Long fileId);
    @Update("update t_net_disk_file_info set pointer_number = pointer_number + 1 where id = #{fileId}")
    void increasePointNumber(Long fileId);

    @Options(keyProperty = "id", useGeneratedKeys = true)
    @Insert("insert into t_net_disk_file_info (file_name, file_size, file_url, pointer_number, hash_value, gmt_create, gmt_update) " +
            "values (#{fileName}, #{fileSize}, #{fileUrl}, #{pointerNumber}, #{hashValue}, #{gmtCreate}, #{gmtUpdate})")
    void insertAndGetId(FileInfo fileInfo);

    @Select("select id, file_name, file_size, file_url, pointer_number, hash_value, gmt_create, gmt_update from t_net_disk_file_info where hash_value = #{fileMD5}")
    FileInfo selectByMD5(String fileMD5);

}
