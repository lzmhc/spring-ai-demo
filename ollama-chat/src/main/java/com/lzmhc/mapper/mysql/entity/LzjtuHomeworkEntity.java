package com.lzmhc.mapper.mysql.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LzjtuHomeworkEntity {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 作业标题
     */
    private String title;

    /**
     * 作业描述
     */
    private String description;

    /**
     * 所属班级ID
     */
    private Long classId;

    /**
     * 布置作业的教师ID
     */
    private Long teacherId;

    /**
     * 布置时间
     */
    private LocalDateTime publishTime;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 状态：1=正常，0=已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注文件
     */
    private String file;
}
