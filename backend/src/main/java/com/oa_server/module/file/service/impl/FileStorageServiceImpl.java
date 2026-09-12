package com.oa_server.module.file.service.impl;

import com.oa_server.module.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * 文件存储服务实现
 *
 * @author Alu
 * @date 2026-09-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${oa_server.minio.bucket}")
    private String bucket;

    @Value("${oa_server.minio.url-prefix:/minio}")
    private String urlPrefix;

    @Override
    public String uploadMinIO(byte[] bytes, String subDir, String fileName, String contentType) {
        try {
            String objectName = subDir + "/" + fileName;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            String url = buildUrl(objectName);
            log.info("[MinIO] 字节上传成功: object={}, size={}", objectName, bytes.length);
            return url;
        } catch (Exception e) {
            log.error("[MinIO] 字节上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 构建文件访问 URL
     */
    private String buildUrl(String objectName) {
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        return prefix + bucket + "/" + objectName;
    }
}
