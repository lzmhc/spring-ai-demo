package com.lzmhc.mapper.mysql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.lzmhc.mapper.mysql.entity.Student;
import org.apache.ibatis.annotations.*;

@Mapper
@DS(value = "mysql")
public interface StuMapper {
    /**
     * 注册用户
     * @param student
     * @return
     */
    @Insert("INSERT INTO lzjtu_student(openid, nickname, gender, province, city) values(#{openid} ,#{nickname}, #{gender}, #{province}, #{city}) ")
    @Options(useGeneratedKeys = false)
    void initStudent(Student student);
    /**
     * 用户是否注册
     * @param openid
     * @return
     */
    @Select("SELECT * FROM lzjtu_student WHERE openid = #{openid}")
    Student findByOpenid(@Param("openid") String openid);

    /**
     * 获取用户信息
     * @param openid
     * @return
     */
    @Select("SELECT studo,nickname,username,school,grade,major,gender,email,province,city FROM lzjtu_student WHERE openid = #{openid}")
    Student getStudentByOpenId(@Param("openid") String openid);

    /**
     * 更新用户信息
     * @param openid
     * @param student
     */
    @Update("UPDATE lzjtu_student SET " +
            "studo=#{student.studo}, " +
            "username=#{student.username}, " +
            "school=#{student.school}, " +
            "grade=#{student.grade}, " +
            "major=#{student.major}, " +
            "gender=#{student.gender}, " +
            "email=#{student.email}, " +
            "province=#{student.province}, " +
            "city=#{student.city} " +
            "WHERE openid=#{openid}")
    void updateStudent(@Param("openid") String openid, @Param("student") Student student);
}
