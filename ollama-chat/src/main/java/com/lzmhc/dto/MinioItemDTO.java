package com.lzmhc.dto;

import io.minio.messages.Item;
import lombok.Data;

import java.util.Date;

@Data
public class MinioItemDTO {
    private String objectName;
    private boolean isDir;
    private long size;
    private String etag;
    private Date lastModified;

    public MinioItemDTO(Item item) {
        this.objectName = item.objectName();
        this.isDir = item.isDir();
        this.size = item.size();
        this.etag = item.etag();
        this.lastModified = Date.from(item.lastModified().toInstant());
    }
}