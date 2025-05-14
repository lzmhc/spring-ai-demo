package com.lzmhc.controller;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.lzmhc.mapper.mysql.entity.Student;
import com.lzmhc.service.MailService;
import com.lzmhc.Utils.QQLoginUtils;
import com.lzmhc.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * QQ登陆接口
 */
@Slf4j
@Controller
//@CrossOrigin(origins = "https://lzmhc.top:18080")
public class LoginController {
    @Autowired
    private QQLoginUtils qqLoginUtils;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MailService mailService;
    @Value("${sa-token.oauth2.qq.client-id}")
    private String clientId;
    @Value("${sa-token.oauth2.qq.client-secret}")
    private String clientSecret;
    @Value("${sa-token.oauth2.qq.redirect-uri}")
    private String redirectUri;
    /**
     * 请求QQ登陆页面
     */
    @RequestMapping("/oauth2/qq/login")
    public String qqLogin() {
        String qqAuthUrl =  "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id="+clientId+"&redirect_uri="+redirectUri+"&scope=get_user_info";
        return "redirect:" + qqAuthUrl;
    }
    /**
     * QQ登陆的回调方法
     */
    @RequestMapping("/connection")
    public String handleCallback(@RequestParam("code") String code){
        Map accessTokenMap = qqLoginUtils.getAccessToken(code);
        String accessToken = (String) accessTokenMap.get("access_token");
        Map openIdMap = qqLoginUtils.getOpenId(accessToken);
        String openid = (String) openIdMap.get("openid");
        // 登陆
        StpUtil.login(openid);
        // 调用QQ api获取用户信息
        Map userInfoMap = qqLoginUtils.getUserInfo(accessToken, openid);
        String avator = (String) userInfoMap.get("figureurl_1");
        Student student = new Student();
        student.setOpenid(openid);
        student.setNickname((String)userInfoMap.get("nickname"));
        student.setGender((String)userInfoMap.get("gender"));
        student.setProvince((String) userInfoMap.get("province"));
        student.setCity((String) userInfoMap.get("city"));
        //检查数据库是否存在用户
        if(!studentService.isRegisterUser(openid)){
            //未注册
            //注册用户
            student.setSchool("兰州交通大学");
            student.setMajor("软件工程");
            student.setGrade("软件2101班");
            studentService.registerUser(student);
            log.info("用户{}注册成功", userInfoMap.get("nickname"));
        }else{
            log.info("用户{}已注册", openid);
        }
        //用户登陆
        String token = StpUtil.getTokenValue();;
//        log.info("token={}", token);
        return "redirect:https://lzmhc.top:18080?token="+token+"&avator="+avator;
    }
    /**
     * 获取用户信息
     */
    @GetMapping("/student/getinfo")
    @ResponseBody
    public Student getInfo(){
        try {
            String token = StpUtil.getTokenValue();
            Student student = studentService.getStudent(token);
            return student;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/student/update")
    @ResponseBody
    public String updateUserInfo(@RequestBody Student student){
            try {
                String token = StpUtil.getTokenValue();
                String openId = (String) StpUtil.getLoginIdByToken(token);
                Student stu = studentService.getStudent(token);
                if(stu.getEmail()==null || !stu.getEmail().equals(student.getEmail())){
                    // 更改邮箱或者首次设置邮箱，发送邮件
                    mailService.sendMail(student.getEmail(), "【智学AI】邮箱绑定成功通知",
                            "尊敬的"+student.getNickname()+":\n" +
                            "您好！感谢您使用智学AI——基于大模型的AI驱动教育平台。您的账户邮箱已成功完成 [首次绑定/修改] 操作"+"\n"+
                                    "安全提示:\n"+
                                    "1.该邮箱将作为您登录、课程订阅及安全验证的重要凭证，请妥善保管。\n" +
                                    "2.如非本人操作，请立即联系客服冻结账户，并修改相关密码。\n" +
                                    "3.青衿智学不会通过邮件索要密码或验证码，谨防诈骗。");
                }
                stu.setStudo(student.getStudo());
//                stu.setUsername(student.getUsername());
                stu.setSchool(student.getSchool());
                stu.setGrade(student.getGrade());
                stu.setMajor(student.getMajor());
//                stu.setGender(student.getGender());
                stu.setEmail(student.getEmail());
                studentService.updateStudent(openId, stu);
                log.info("更新用户{}", student);
                return "更新成功";
            }catch(Exception e){
                e.printStackTrace();
                return "Error updating student: " + e.getMessage();
            }
    }
    /**
     * 注销接口
     */
    @RequestMapping("/logout")
    @ResponseBody
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }
}
