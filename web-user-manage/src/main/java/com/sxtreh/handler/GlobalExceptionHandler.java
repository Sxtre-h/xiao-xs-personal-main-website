package com.sxtreh.handler;

import com.sxtreh.constant.MessageConstant;
import com.sxtreh.exception.BaseException;
import com.sxtreh.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 程序异常处理
     * @param baseException
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException baseException){
        log.info(baseException.getMessage());
        return Result.error(baseException.getMessage());
    }
    @ExceptionHandler
    public Result exceptionHandler(HttpMessageNotReadableException ex){
        log.info("你忘了写请求参数！：" + ex.getMessage());
        return Result.error("你忘了写请求参数或者参数有语法错误！：" + ex.getMessage());
    }

    //TODO 将后端异常（如AspectException）和BaseException分开，不要向前端返回异常信息
    /**
     * 数据库异常异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }


}
