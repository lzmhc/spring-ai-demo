package com.lzmhc.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {
    /**
     * Springboot提供发送邮件
     */
    @Autowired
    private JavaMailSender mailSender;
    /**
     * 配置文件中的发送邮箱
     */
    @Value("${spring.mail.from}")
    private String from;
    public void sendMail(String to, String subject, String content){
        //创建邮箱消息对象
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
        log.info("邮件发送成功:", message.toString());
    }
}
