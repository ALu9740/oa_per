package com.oa_server.module.admin.kb.controller;

import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.kb.service.KbService;
import com.oa_server.module.admin.kb.vo.KbDocumentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    /**
     * 分页查询知识库文档
     */
    @GetMapping("/list")
    public Result<PageResult<KbDocumentVO>> list(@RequestParam(required = false) String fileName,
                                                 @RequestParam(required = false) Long page,
                                                 @RequestParam(required = false) Long size) {
        return Result.success(kbService.list(fileName, page, size));
    }

    /**
     * 删除文档
     */
    @PutMapping("/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        kbService.delete(id);
        return Result.success();
    }
}
