package com.lzmhc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeWorkDTO {
    private String id;
    private String title;
    private String description;
    private LocalDateTime publishTime;
    private LocalDateTime deadline;
    private Integer status;
    private String file;
    private String fileName;
    private String fileKey;
}
