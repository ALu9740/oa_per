package com.oa_server.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置
 *
 * @author Alu
 * @date 2026-09-11
 */
@Slf4j
@Configuration
public class MinIOConfig {
    @Value("${oa_server.minio.endpoint}")
    private String endpoint;

    @Value("${oa_server.minio.access-key}")
    private String accessKey;

    @Value("${oa_server.minio.secret-key}")
    private String secretKey;

    @Value("${oa_server.minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        // 启动时自动创建 bucket
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("[MinIO] 创建 bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.warn("[MinIO] 初始化 bucket 失败: {}", e.getMessage());
        }
        return client;
    }
}
