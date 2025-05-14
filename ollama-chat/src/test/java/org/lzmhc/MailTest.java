package org.lzmhc;

import com.lzmhc.Application;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class MailTest {
    @Autowired
    private MinioClient minioClient;
    @Test
    public void testMinioConnection() throws Exception {
            // 列出所有存储桶，验证连接是否成功
            List<Bucket> buckets = minioClient.listBuckets();
            System.out.println(buckets.isEmpty());

    }
}

