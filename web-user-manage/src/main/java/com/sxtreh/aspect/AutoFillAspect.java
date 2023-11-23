package com.sxtreh.aspect;

import com.sxtreh.annotation.AutoFill;
import com.sxtreh.constant.AutoFillConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.MethodNameConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.enumeration.OperationType;
import com.sxtreh.exception.AspectException;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充用户信息表公共字段
 * 已弃置@AutoFill注解，采用MybatisPlus函数，拦截相关方法
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {
//    @Pointcut("execution(* com.sxtreh.mapper.*.*(..)) && @annotation(com.sxtreh.annotation.AutoFill)")
    @Pointcut("execution(* com.sxtreh.mapper.*.*(..))")
    public void autoFillPoint(){}
    //前置通知
    @Before("autoFillPoint()")
    public void autoFill(JoinPoint joinPoint){
        //获取方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取方法名
        String name = signature.getMethod().getName();
        //非insert和非update直接返回
        if (!name.contains(MethodNameConstant.INSERT) && !name.contains(MethodNameConstant.UPDATE)) {
            return;
        }
        log.info("自动填充时间参数");

        //获取被拦截方法的参数--实体对象，默认对象放第一个参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) return;
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();

        //通过反射为实体对象赋值
        try {
            Method setGmtCreate = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_GMT_CREATE, LocalDateTime.class);
            Method setGmtUpdate = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_GMT_UPDATE, LocalDateTime.class);
            if(name.contains(MethodNameConstant.INSERT)){
                setGmtCreate.invoke(entity, now);
            }
            setGmtUpdate.invoke(entity, now);
        } catch (Exception ex) {
            //自定义异常（非框架）
            throw new AspectException(MessageConstant.GMT_ERROR);
        }
    }
}
