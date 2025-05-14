package com.lzmhc.Utils;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    /**
     * FilePart转CustomMultipartFile
     */
    public static Mono<File> convertFilePartToFile(FilePart filePart) {
        // 1. 创建临时文件路径
        Path tempFilePath = createTempFilePath(filePart.filename());

        // 2. 异步写入文件内容
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    try {
                        // 将 DataBuffer 转换为字节数组
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer); // 必须释放资源
                        // 3. 异步写入文件（使用弹性线程池）
                        return Mono.fromCallable(() -> {
                                    Files.write(tempFilePath, bytes,
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.TRUNCATE_EXISTING);
                                    return tempFilePath.toFile();
                                })
                                .subscribeOn(Schedulers.boundedElastic());
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                // 4. 添加清理钩子
                .doFinally(signalType -> {
                    if (Files.exists(tempFilePath)) {
                        try {
                            Files.delete(tempFilePath);
                            System.out.println("临时文件已清理: " + tempFilePath);
                        } catch (Exception e) {
                            System.err.println("临时文件清理失败: " + e.getMessage());
                        }
                    }
                });
    }
    private static Path createTempFilePath(String originalFilename) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return Path.of(tempDir, "upload_" + System.currentTimeMillis() + "_" + safeFilename);
    }
}
