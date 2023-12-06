package com.sxtreh.note.handler;

import com.sxtreh.constant.MessageConstant;
import com.sxtreh.exception.ProcedureException;
import com.sxtreh.exception.RequestException;
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
     * 前端请求异常处理，包括参数错误，访问非法等
     * @param requestException
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(RequestException requestException){
        log.error(requestException.getMessage());
        return Result.error(requestException.getMessage());
    }

    /**
     *后端程序出现异常
     * @param procedureException
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(ProcedureException procedureException){
        log.error(procedureException.getMessage());
        return Result.error(MessageConstant.PROCEDURE_ERROR);
    }

    /**
     * HTTP请求异常，指访问接口的方式不对
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(HttpMessageNotReadableException ex){
        log.error("你忘了写请求参数！：" + ex.getMessage());
        return Result.error("你忘了写请求参数或者参数有语法错误！：" + ex.getMessage());
    }

    /**
     * 数据库异常
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
