package com.oa_server.module.admin.kb.controller;

import com.oa_server.common.result.Result;
import com.oa_server.module.admin.kb.service.KbService;
import com.oa_server.module.admin.kb.vo.KbDocumentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理接口
 *
 * @author Alu
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/api/ai/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    /**
     * 上传制度文档
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<KbDocumentVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success("上传成功", kbService.upload(file));
    }
}
