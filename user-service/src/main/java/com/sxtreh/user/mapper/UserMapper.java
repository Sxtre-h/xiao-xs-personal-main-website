package com.sxtreh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxtreh.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * MP接口
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select id, user_name, login_name, password, profile, avatar, status, is_admin, gmt_create, gmt_update, user_net_disk_root_id, user_space_remain, user_space_total" +
            " from t_user_info where login_name = #{loginName}")
    User selectByLoginName(String loginName);

    @Options(keyProperty = "id", useGeneratedKeys = true)
    @Insert("insert into t_user_info (user_name, login_name, password, profile, avatar, gmt_create, gmt_update, user_net_disk_root_id, user_space_remain, user_space_total) " +
            "values (#{userName},#{loginName},#{password},#{profile},#{avatar},#{gmtCreate},#{gmtUpdate},#{userNetDiskRootId},#{userSpaceRemain},#{userSpaceTotal})")
    void insertAndGetId(User user);


    @Update("update t_user_info set user_space_remain = user_space_remain + #{increaseSpace} where id = #{userId}")
    void increaseUserNetDiskSpace(Long userId, Long increaseSpace);

    @Update("update t_user_info set user_space_remain = user_space_remain - #{decreaseSpace} where id = #{userId}")
    void decreaseUserNetDiskSpace(Long userId, Long decreaseSpace);
}
