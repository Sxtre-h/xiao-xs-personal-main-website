package com.sxtreh.Filter;

import com.sxtreh.constant.JwtClaimsConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.exception.NotLoginException;
import com.sxtreh.properties.JwtProperties;
import com.sxtreh.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(1)
public class AuthorizeFilter implements GlobalFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求
        ServerHttpRequest request = exchange.getRequest();
        //非登录方法直接放行
        RequestPath path = request.getPath();
        if (path.toString().equals("/users/login")
                || path.toString().equals("/users/register")
                || path.toString().equals("/admin/feedback")) {
            return chain.filter(exchange);
        }
        //登录方法
        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get(jwtProperties.getUserTokenName());
        //没有token，拦截
        if (tokens == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        //有token则解析
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), tokens.get(0));
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            if (userId != null) {
                //解析成功
                return chain.filter(exchange);
            }
        } catch (Exception ex) {
            //jwt解析失败
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        //其他情况，默认拦截
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
