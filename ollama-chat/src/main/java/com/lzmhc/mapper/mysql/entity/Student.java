package com.lzmhc.mapper.mysql.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.Data;

@Data
@DS(value = "mysql")
public class Student {

    private String openid; //qq标识

    private String studo; //学号

    private String nickname; //昵称

    private String username; //姓名

    private String school; //学校

    private String grade; //班级

    private String major; //专业

    private String gender; //性别

    private String email; //邮箱

    private String province; //省
    private String city; //市
}
