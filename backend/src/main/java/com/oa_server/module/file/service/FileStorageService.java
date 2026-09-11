package com.oa_server.module.file.service;

/**
 * 文件存储服务接口
 *
 * @author Alu
 * @date 2026-09-11
 */
public interface FileStorageService {

    /**
     * 上传字节数组
     *
     * @param bytes   文件字节
     * @param subDir  子目录
     * @param fileName 文件名
     * @param contentType MIME 类型
     * @return 可访问的 URL
     */
    String upload(byte[] bytes, String subDir, String fileName, String contentType);
}
