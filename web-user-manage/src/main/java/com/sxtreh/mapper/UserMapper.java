package com.sxtreh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxtreh.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * MP接口
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from t_user_info where login_name = #{loginName}")
    User selectByLoginName(String loginName);
}
