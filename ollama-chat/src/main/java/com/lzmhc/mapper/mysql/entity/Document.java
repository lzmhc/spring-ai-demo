package com.lzmhc.mapper.mysql.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@DS(value = "mysql")
public class Document {
    // 文件id
    private Long fileId;
    // 文件夹
    private String folderType;
    // 文件名称
    private String fileName;
    // 文件大小
    private Long fileSize;
    // 文件下载
    private String fileKey;
    // 文件类型
    private String fileType;
    // 创建人
    private String creatorId;
    private String creatorName;
    // 创建时间
    private LocalDate createTime;
    // 是否向量化
    private boolean isEmbedding;
}
