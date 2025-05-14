package com.lzmhc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.lzmhc.mapper.mysql.entity.Student;
import com.lzmhc.mapper.mysql.mapper.StuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    @Autowired
    private StuMapper stuMapper;
    /**
     * 注册
     * @param student
     * @return
     */
    /**
     * 注册用户
     * @param student
     */
    public void registerUser(Student student){
        if(!isRegisterUser(student.getOpenid())){
            //注册
            stuMapper.initStudent(student);
        }else{
            //已注册
        }
    }

    /**
     * 判断用户是否注册
     * @param openid
     * @return
     */
    public boolean isRegisterUser(String openid){
        return stuMapper.findByOpenid(openid) != null;
    }
    /**
     * 获取用户信息
     */
    public Student getStudent(String token){
        try {
            String openid = (String) StpUtil.getLoginIdByToken(token);;
            Student student = stuMapper.getStudentByOpenId(openid);
            return student;
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }
    /**
     * 生成token
     */
//    public String generateToken( Student student){
//        String token = jwtUtil.generateToken(student);
//        return token;
//    }

    /**
     * 更新信息
     * @param openid
     * @param student
     */
    public void updateStudent(String openid, Student student){
        stuMapper.updateStudent(openid, student);
    }

//    public String getOpenIdByToken(String token) throws Exception {
//        Claims claims = jwtUtil.parseToken(token);
//        return claims.getSubject();
//    }
}
