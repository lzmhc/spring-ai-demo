package com.lzmhc.mapper.mysql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.lzmhc.dto.HomeWorkDTO;
import com.lzmhc.mapper.mysql.entity.CommitHomework;
import com.lzmhc.mapper.mysql.entity.LzjtuHomeworkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@DS(value = "mysql")
public interface LzjtuHomeworkMapper {
    List<HomeWorkDTO> queryPage(String openid);
    int commitHomework(@Param("params")CommitHomework commitHomework);
}
