package com.lzmhc.config;

import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@Slf4j
public class MinioConfig {
    @Value(value="${minio.bucketName}")
    private String bucket;
    @Value(value="${minio.endpoint}")
    private String endpoint;
    @Value(value="${minio.accessKey}")
    private String accessKey;
    @Value(value="${minio.secretKey}")
    private String secretKey;
    @PostConstruct
    public void init() {
        log.info("MinIO配置加载成功 - Bucket: {}", bucket);
    }
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(this.endpoint)
                .credentials(this.accessKey, this.secretKey)
                .build();
    }
}
