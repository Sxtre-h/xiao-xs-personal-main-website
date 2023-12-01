package com.sxtreh.aspect;

import com.sxtreh.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 记录用户操作记录
 */
@Slf4j
@Component
@Aspect
public class LogAspect {
    //正常操作只记录controller
    @Pointcut("execution(* com.sxtreh.controller.*.*(..))")
    public void normalPoint() {
    }
    //异常记录全操作
    @Pointcut("execution(* com.sxtreh.*.*.*(..))")
    public void exceptionPoint() {
    }

    @AfterThrowing("exceptionPoint()")
    public void exceptionLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        //获取当前操作用户id
        String userName = BaseContext.getCurrentId() == null ? "未登录用户" : "用户" + BaseContext.getCurrentId().toString();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        //记录日志
        String errorMessage = userName + "访问方法 " + methodName + ",参数为: " + args.toString();
        log.error(errorMessage);
    }

    @AfterReturning("normalPoint()")
    public void successLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        //获取当前操作用户id
        String userName = BaseContext.getCurrentId() == null ? "未登录用户" : "用户" + BaseContext.getCurrentId().toString();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        //记录日志
        String opMessage = userName + "访问方法 " + methodName + ",参数为: " + args.toString();
        log.info(opMessage);
    }


}
