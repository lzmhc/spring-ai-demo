package com.lzmhc.mapper.mysql.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommitHomework {
    private Long id;
    private Long homeworkId;
    private String studentId;
    private LocalDateTime commitTime;
    private int TeacherScore;
    private String fileId;
    private String remark;
    private int status;
}
