package com.lzmhc.controller;

import com.lzmhc.config.MinioConfig;
import com.lzmhc.dto.ApiResponse;
import com.lzmhc.dto.MinioItemDTO;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.mapper.mysql.mapper.DocumentMapper;
import com.lzmhc.service.DocumentService;
import com.lzmhc.service.MinioService;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

@RestController
//@CrossOrigin
@RequestMapping("/file")
@Slf4j
public class MinioController {
    @Resource
    private MinioService minioService;
    @Autowired
    private DocumentService documentService;
    /**
     * 上传文件
     * @param filePart
     * @return
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse>> uploadFile(@RequestPart("file") FilePart filePart){
        log.info("上传文件{}", filePart);
        return minioService.uploadFileReactive(filePart)
                .map(content -> ResponseEntity.ok(
                        ApiResponse.success("success", content)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(e.getMessage()))
                ));
    }

    /**
     * 获取文件列表minio
     * @return
     */
    @GetMapping("/listReactive")
    public Flux<MinioItemDTO> getFileList(){
        return minioService.listFilesReactive();
    }

    /**
     * 获取文件列表mysql
     * @return
     */
    @GetMapping("/list")
    public List<Document> getDocumentList(){
        return minioService.listFiles();
    }
    /**
     * 下载文件
     */
    @GetMapping("/download")
    public Flux<ByteBuffer> downloadFile(String file){
        return minioService.downloadFileReactive(file)
                .onErrorResume(throwable -> {
                    if(throwable instanceof FileNotFoundException){
                        return Flux.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "The requested file was not found.", throwable
                        ));
                    }else{
                        return Flux.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", throwable
                        ));
                    }
                });
    }
    /**
     * 预览文件
     */
    @GetMapping("/preview")
    public Mono<String> previewFile(String file){
        return minioService.getPreSignedUrl(file)
                .onErrorResume(throwable -> {
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate preview URL", throwable));
                });
    }

    /**
     * 删除文件
     * @param file
     * @return
     */
    @GetMapping("/delete")
    public Mono<Void> deleteFile(String file){
        return minioService.remove(file);
    }
    /**
     * RAG检索增强
     */
//    @GetMapping("/ragFile")
//    public Mono<ResponseEntity<String>> ragFile(String file){
//        // 创建临时文件
//        log.info("RAG文档{}", file);
//        return Mono.fromCallable(()-> {
//                    File tempFile = File.createTempFile("minio-", ".tmp");
//                    if (tempFile.exists()) {
//                        // 尝试删除已存在的文件
//                        boolean deleted = tempFile.delete();
//                        if (!deleted) {
//                            throw new IOException("Failed to delete existing temporary file: " + tempFile.getAbsolutePath());
//                        }
//                    }
//                    return tempFile;
//                })
//                .subscribeOn(Schedulers.boundedElastic()) // 避免阻塞事件循环
//                .flatMap(tempFile -> {
//                    // 从Minio下载文件
//                    return Mono.fromCallable(() -> {
//                                minioClient.downloadObject(
//                                        DownloadObjectArgs.builder()
//                                                .bucket(minioConfig.getBucket())
//                                                .object(file)
//                                                .filename(tempFile.getAbsolutePath())
//                                                .build());
//                                return tempFile;
//                            })
//                            .timeout(Duration.ofSeconds(30)) // 下载超时控制
//                            .doFinally(signalType -> {
//                                // 处理完成后删除临时文件
//                                if (tempFile.exists()) {
//                                    try {
//                                        Files.delete(tempFile.toPath());
//                                    } catch (IOException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//                            })
//                            .flatMap(downloadedFile -> {
//                                try {
//                                    documentService.paragraphTextReader(downloadedFile, file);
//                                    return Mono.just(ResponseEntity.ok("文件处理成功"));
//                                } catch (Exception e) {
//                                    return Mono.error(e);
//                                }
//                            });
//                })
//                .onErrorResume(e -> {
//                    e.printStackTrace();
//                    log.error("文件处理失败: {}", e.getMessage());
//                    return Mono.just(ResponseEntity
//                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body("处理失败: " + e.getMessage()));
//                });
////        return convertFilePartToFile(filePart).flatMap(tempFile -> {
////                    // 这里可以处理 File 对象
////                    documentService.paragraphTextReader(tempFile);
////                    System.out.println("处理文件: " + tempFile.getAbsolutePath()+tempFile.length() + " bytes");
////                    return Mono.empty();
////                })
////                .onErrorResume(e -> {
////                    e.printStackTrace();
////                    System.err.println("文件处理失败: "+e.getMessage());
////                    return Mono.error(e);
////                });
//    }

    @GetMapping("/ragKnowledge")
    public Mono<ResponseEntity<String>> ragKnowledge(String file){
        return documentService.documentStore(file)
                .thenReturn(ResponseEntity.ok("文件处理成功"))
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("文件处理失败"+e.getMessage()));
                });
    }
    @GetMapping("/getFiles")
    public Flux<Document> getFiles(){
        return documentService.getFiles();
    }

    /**
     * 多模态文件上传
     */
    @PostMapping(value = "/multimodal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse>> uploadFileMultimodal(@RequestPart("file") FilePart filePart){
        log.info("上传文件{}", filePart);
        return minioService.uploadFileMultimodal(filePart)
                .map(content -> ResponseEntity.ok(
                        ApiResponse.success("success", content)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(e.getMessage()))
                ));
    }

    /**
     * 作业文件上传
     * @param filePart
     * @return
     */
    @PostMapping(value = "/homework", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse>> uploadFileHomework(@RequestPart("file") FilePart filePart){
        log.info("上传文件{}", filePart);
        return minioService.uploadFileHomework(filePart)
                .map(content -> ResponseEntity.ok(
                        ApiResponse.success("success", content)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(e.getMessage()))
                ));
    }
}


