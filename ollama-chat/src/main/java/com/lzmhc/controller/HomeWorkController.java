package com.lzmhc.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lzmhc.dto.HomeWorkDTO;
import com.lzmhc.mapper.mysql.entity.CommitHomework;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.mapper.mysql.entity.LzjtuHomeworkEntity;
import com.lzmhc.mapper.mysql.mapper.LzjtuHomeworkMapper;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
public class HomeWorkController {
    @Autowired
    private LzjtuHomeworkMapper lzjtuHomeworkMapper;
    @GetMapping("/lzjtuHomework/queryPage")
    public Flux<HomeWorkDTO> queryPage(){
        List<HomeWorkDTO> lzjtuHomeworkEntities = lzjtuHomeworkMapper.queryPage(StpUtil.getLoginIdAsString());
        return Flux.fromIterable(lzjtuHomeworkEntities);
    }
    @PostMapping("/lzjtuHomework/commitHomework")
    public Mono<ResponseEntity<String>> commitHomework(@RequestBody CommitHomework homework){
        homework.setStudentId(StpUtil.getLoginIdAsString());
        homework.setCommitTime(LocalDateTime.now());
        homework.setStatus(1);
        int i = lzjtuHomeworkMapper.commitHomework(homework);
        if("1".equals(i+"")){
            return Mono.just(ResponseEntity.ok("提交成功"));
        }else{
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("提交失败"));
        }
    }
}
