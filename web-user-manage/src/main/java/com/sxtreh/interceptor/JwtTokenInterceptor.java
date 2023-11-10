package com.sxtreh.interceptor;

import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.constant.JwtClaimsConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.exception.NotLoginException;
import com.sxtreh.properties.JwtProperties;
import com.sxtreh.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验拦截
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 校验jwt
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //解决跨域问题
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //部分非登录功能直接放行
        HandlerMethod hm = (HandlerMethod) handler;
        //不包含@RequireLogin注解
        if (!hm.hasMethodAnnotation(RequireLogin.class)){
            return true;
        }
        //登录功能校验
        //1.从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());
        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：", userId);
            //将用户ID存入线程空间
            BaseContext.setCurrentId(userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            throw new NotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
    }
}