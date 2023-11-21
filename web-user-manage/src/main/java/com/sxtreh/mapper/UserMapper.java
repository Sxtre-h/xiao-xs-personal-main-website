package com.sxtreh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxtreh.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * MP接口
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select id, user_name, login_name, password, profile, avatar, status, is_admin, gmt_create, gmt_update from t_user_info " +
            "where login_name = #{loginName}")
    User selectByLoginName(String loginName);
}
