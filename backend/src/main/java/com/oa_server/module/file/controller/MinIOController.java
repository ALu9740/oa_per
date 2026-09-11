package com.oa_server.module.file.controller;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MinIO 接口
 *
 * @author Alu
 * @date 2026-09-12
 */
@Slf4j
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinIOController {

    private final MinioClient minioClient;

    @Value("${oa_server.minio.bucket}")
    private String bucket;

    @GetMapping("/**")
    public void serveFile(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        // /minio/{bucket}/{object} → {object}
        String uri = request.getRequestURI();
        String prefix = "/minio/" + bucket + "/";
        String objectName;
        if (uri.startsWith(prefix)) {
            objectName = uri.substring(prefix.length());
        } else {
            String afterMinio = uri.substring("/minio/".length());
            int slashIdx = afterMinio.indexOf('/');
            objectName = slashIdx > 0 ? afterMinio.substring(slashIdx + 1) : afterMinio;
        }

        try (GetObjectResponse obj = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build())) {
            String contentType = obj.headers().get("Content-Type");
            if (contentType == null || "application/octet-stream".equalsIgnoreCase(contentType)) {
                contentType = guessContentType(objectName);
            }
            response.setContentType(contentType);
            String contentLength = obj.headers().get("Content-Length");
            if (contentLength != null) {
                response.setContentLengthLong(Long.parseLong(contentLength));
            }
            obj.transferTo(response.getOutputStream());
        } catch (Exception e) {
            log.warn("[MinIO] 文件读取失败: object={}, err={}", objectName, e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String guessContentType(String objectName) {
        String lower = objectName == null ? "" : objectName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
