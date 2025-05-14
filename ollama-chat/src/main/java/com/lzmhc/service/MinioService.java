package com.lzmhc.service;
import cn.dev33.satoken.stp.StpUtil;
import com.lzmhc.config.MinioConfig;
import com.lzmhc.dto.MinioItemDTO;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.mapper.mysql.mapper.DocumentMapper;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class MinioService {
    @Autowired
    private MinioConfig minioConfig;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private DocumentMapper documentMapper;
    //-- 自定义流转换器 --//
    private static class FluxInputStream extends InputStream {
        private final Iterator<ByteBuffer> bufferIterator;
        private ByteBuffer currentBuffer;

        public FluxInputStream(Flux<ByteBuffer> flux) {
            this.bufferIterator = flux.toIterable().iterator();
        }

        @Override
        public int read() throws IOException {
            if (!ensureBuffer()) return -1;
            return currentBuffer.get() & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (!ensureBuffer()) return -1;
            len = Math.min(len, currentBuffer.remaining());
            currentBuffer.get(b, off, len);
            return len;
        }

        private boolean ensureBuffer() {
            while ((currentBuffer == null || !currentBuffer.hasRemaining())) {
                if (!bufferIterator.hasNext()) return false;
                currentBuffer = bufferIterator.next();
            }
            return true;
        }
    }
    //-- 自定义异常 --//
    public static class FileUploadException extends RuntimeException {
        public FileUploadException(String message) {
            super(message);
        }
    }
    /**
     * 上传
     */
    public Mono<String> uploadFileReactive(FilePart filePart) {
        return Mono.defer(() -> {
            try {
                String objectName = generateObjectName(filePart);
                log.info(objectName);
                // 将FilePart内容转换为可重复使用的字节流
                Flux<ByteBuffer> byteBufferFlux = filePart.content()
                        .map(dataBuffer -> {
                            ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                            DataBufferUtils.release(dataBuffer); // 释放资源
                            return byteBuffer;
                        })
                        .cache(); // 缓存流以支持重放
                // 获取文件大小（可选，可能需要流式上传）
                Mono<Long> sizeMono = byteBufferFlux
                        .reduce(0L, (acc, buf) -> acc + buf.remaining())
                        .cache();
                // 执行上传操作
                return sizeMono.flatMap(size ->
                        Mono.fromCallable(() ->
                                minioClient.putObject(
                                        PutObjectArgs.builder()
                                                .bucket(minioConfig.getBucket())
                                                .object(objectName)
                                                .stream(new FluxInputStream(byteBufferFlux), size, -1)
                                                .contentType(filePart.headers().getContentType().toString())
                                                .build()
                                )
                        ).flatMap(response ->{
                                    Document document = new Document();
                                    document.setFolderType("1");
                                    document.setFileName(objectName);
                                    document.setFileSize(size);
                                    document.setFileKey(buildFileUrl(objectName));
                                    document.setFileType(filePart.headers().getContentType().toString());
                                    document.setCreatorId(StpUtil.getLoginIdAsString());
                                    document.setCreateTime(LocalDate.now());
                                    document.setEmbedding(false);
                                    documentMapper.insertDocument(document);
                                    return Mono.just(new String("文件上传成功"));
                                })
                        .onErrorResume(e -> {
                            log.error("文件上传失败", e);
                            return Mono.error(new FileUploadException("文件上传失败: " + e.getMessage()));
                        }));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }
    /**
     * 下载文件（保持原有逻辑，适配响应式）
     */
    public Flux<ByteBuffer> downloadFileReactive(String filename) {
        return Flux.usingWhen(
                Mono.fromCallable(() ->
                        minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(minioConfig.getBucket())
                                        .object(filename)
                                        .build()
                        )
                ),
                inputStream -> Flux.generate(() -> inputStream, (is, sink) -> {
                    try {
                        byte[] buffer = new byte[4096];
                        int read = is.read(buffer);
                        if (read == -1) {
                            sink.complete();
                        } else {
                            sink.next(ByteBuffer.wrap(buffer, 0, read));
                        }
                        return is;
                    } catch (IOException e) {
                        sink.error(e);
                        return is;
                    }
                }),
                inputStream -> Mono.fromRunnable(() -> {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("关闭流失败", e);
                    }
                })
        );
    }
    /**
     * 获取存储桶文件列表（响应式）
     * @return Flux<Item> 包含文件元数据的流
     */
    public Flux<MinioItemDTO> listFilesReactive() {
        return Mono.fromCallable(() -> minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(minioConfig.getBucket())
                                .recursive(false)
                                .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(results -> Flux.fromIterable(results)
                        .flatMap(result -> Mono.fromCallable(() -> {
                                    Item item = result.get();
                                    return new MinioItemDTO(item); // 转换为DTO
                                })
                                .onErrorResume(e -> {
                                    System.err.println("Error retrieving item: " + e.getMessage());
                                    return Mono.empty();
                                }))
                )
                .onErrorResume(e -> {
                    System.err.println("Failed to list objects: " + e.getMessage());
                    return Flux.error(new RuntimeException("Failed to list objects", e));
                });
    }

    /**
     * 获取文件列表
     * @return
     */
    public List<Document> listFiles(){
        List<Document> documentList = documentMapper.getDocumentList(StpUtil.getLoginIdAsString());
        return documentList;
    }
    // 生成预签名URL
    public Mono<String> getPreSignedUrl(String objectName) {
        return Mono.fromCallable(() -> {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(24 * 60 * 60) // URL有效期为24小时
                    .build();
            try {
                return minioClient.getPresignedObjectUrl(args);
            } catch (MinioException | IOException e) { // 捕获可能的异常
                throw new RuntimeException("Failed to generate presigned URL", e);
            }
        }).doOnError(Throwable::printStackTrace); // 错误处理
    }

    /**
     * 删除文件
     * @param id
     * @return
     */
    public Mono<Void> remove(String id) {
        documentMapper.deleteDocument(id);
        return Mono.empty();
    }
    //-- 工具方法 --//
    private String generateObjectName(FilePart filePart) {
        return  filePart.filename()
                .replace(" ", "_"); // 处理空格; // 过滤特殊字符
    }

    private String buildFileUrl(String objectName) {
        return String.format("%s/%s/%s",
                minioConfig.getEndpoint(),
                minioConfig.getBucket(),
                URLEncoder.encode(objectName, StandardCharsets.UTF_8)
        );
    }

    /**
     * 获取文件流
     */
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucket()).object(objectName).build());
        }
        catch (Exception e) {
            log.error("Get file failed", e);
            throw new RuntimeException("File retrieval failed");
        }
    }

    /**
     * 多模态文件上传
     */
    public Mono<Map<String, String>> uploadFileMultimodal(FilePart filePart) {
        return Mono.defer(() -> {
            try {
                String objectName = generateObjectName(filePart);
                log.info(objectName);
                // 将FilePart内容转换为可重复使用的字节流
                Flux<ByteBuffer> byteBufferFlux = filePart.content()
                        .map(dataBuffer -> {
                            ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                            DataBufferUtils.release(dataBuffer); // 释放资源
                            return byteBuffer;
                        })
                        .cache(); // 缓存流以支持重放
                // 获取文件大小（可选，可能需要流式上传）
                Mono<Long> sizeMono = byteBufferFlux
                        .reduce(0L, (acc, buf) -> acc + buf.remaining())
                        .cache();
                // 执行上传操作
                return sizeMono.flatMap(size ->
                        Mono.fromCallable(() ->
                                        minioClient.putObject(
                                                PutObjectArgs.builder()
                                                        .bucket(minioConfig.getBucket())
                                                        .object(objectName)
                                                        .stream(new FluxInputStream(byteBufferFlux), size, -1)
                                                        .contentType(filePart.headers().getContentType().toString())
                                                        .build()
                                        )
                                ).flatMap(response ->{
                                    Document document = new Document();
                                    document.setFolderType("1");
                                    document.setFileName(objectName);
                                    document.setFileSize(size);
                                    document.setFileKey(buildFileUrl(objectName));
                                    document.setFileType(filePart.headers().getContentType().toString());
                                    document.setCreatorId(StpUtil.getLoginIdAsString());
                                    document.setCreateTime(LocalDate.now());
                                    document.setEmbedding(false);
                                    documentMapper.insertMultimodal(document);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("objectName", objectName);
                                    map.put("fileKey", document.getFileKey());
                                    return Mono.just(map);
                                })
                                .onErrorResume(e -> {
                                    log.error("文件上传失败", e);
                                    return Mono.error(new FileUploadException("文件上传失败: " + e.getMessage()));
                                }));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    /**
     * 作业文件上传
     * @param filePart
     * @return
     */
    public Mono<Map<String, String>> uploadFileHomework(FilePart filePart) {
        return Mono.defer(() -> {
            try {
                String objectName = generateObjectName(filePart);
                log.info(objectName);
                // 将FilePart内容转换为可重复使用的字节流
                Flux<ByteBuffer> byteBufferFlux = filePart.content()
                        .map(dataBuffer -> {
                            ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                            DataBufferUtils.release(dataBuffer); // 释放资源
                            return byteBuffer;
                        })
                        .cache(); // 缓存流以支持重放
                // 获取文件大小（可选，可能需要流式上传）
                Mono<Long> sizeMono = byteBufferFlux
                        .reduce(0L, (acc, buf) -> acc + buf.remaining())
                        .cache();
                // 执行上传操作
                return sizeMono.flatMap(size ->
                        Mono.fromCallable(() ->
                                        minioClient.putObject(
                                                PutObjectArgs.builder()
                                                        .bucket(minioConfig.getBucket())
                                                        .object(objectName)
                                                        .stream(new FluxInputStream(byteBufferFlux), size, -1)
                                                        .contentType(filePart.headers().getContentType().toString())
                                                        .build()
                                        )
                                ).flatMap(response ->{
                                    Document document = new Document();
                                    document.setFolderType("1");
                                    document.setFileName(objectName);
                                    document.setFileSize(size);
                                    document.setFileKey(buildFileUrl(objectName));
                                    document.setFileType(filePart.headers().getContentType().toString());
                                    document.setCreatorId(StpUtil.getLoginIdAsString());
                                    document.setCreateTime(LocalDate.now());
                                    document.setEmbedding(false);
                                    documentMapper.insertHomework(document);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("objectName", objectName);
                                    map.put("fileKey", document.getFileKey());
                                    return Mono.just(map);
                                })
                                .onErrorResume(e -> {
                                    log.error("文件上传失败", e);
                                    return Mono.error(new FileUploadException("文件上传失败: " + e.getMessage()));
                                }));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }
    public Media getMediaFromMinio(String objectName) throws Exception {
        // 从 MinIO 获取文件流
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucket())  // 替换为你的存储桶名称
                        .object(objectName)          // resourceId 是文件路径/名称
                        .build()
        )) {
            byte[] data = stream.readAllBytes();

            // 创建 ByteArrayResource（包装二进制数据）
            ByteArrayResource resource = new ByteArrayResource(data) {
                @Override
                public String getFilename() {
                    return objectName; // 可选：设置文件名
                }
            };

            // 创建 Media 对象（需明确 MIME 类型，如 "image/jpeg"）
            return Media.builder()
                    .data(resource)
                    .mimeType(MimeTypeUtils.parseMimeType("image/jpeg")).build();
        }
    }
}
