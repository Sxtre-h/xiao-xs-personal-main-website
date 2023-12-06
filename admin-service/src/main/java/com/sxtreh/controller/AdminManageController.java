package com.sxtreh.controller;

import com.sxtreh.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
