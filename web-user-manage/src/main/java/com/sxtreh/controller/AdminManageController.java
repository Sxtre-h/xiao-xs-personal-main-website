package com.sxtreh.controller;

import com.sxtreh.annotation.ParameterCheck;
import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.result.Result;
import com.sxtreh.vo.UserFileVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminManageController {
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.from}")
    private String mailFrom;
    @Value("${spring.mail.to}")
    private String mailTo;

    /**
     * 接收网站用户的反馈并发送到指定邮箱
     * 部分参数固定，且无重用性，采用硬编码
     * @param message
     * @return
     */
    @PostMapping("/feedback")
    public Result getMyMessage(@RequestParam(name = "feedBackMessage") String message) {
        if (message == null || message.equals("")) return Result.error("没有填写任何内容");
        if (message.length() > 1000) return Result.error("太长了, 请将字数限制在1000字以内");
        log.info(message);
        String title = "反馈信息";
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(mailTo);
        mailMessage.setSubject(title);
        mailMessage.setText(message);
        javaMailSender.send(mailMessage);
        return Result.success();
    }
}
