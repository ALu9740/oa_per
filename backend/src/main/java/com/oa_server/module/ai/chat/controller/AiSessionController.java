package com.oa_server.module.ai.chat.controller;

import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.ai.chat.service.ChatSessionService;
import com.oa_server.module.ai.chat.vo.AiChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 会话管理接口
 *
 * @author Alu
 * @date 2026-09-14
 */
@RestController
@RequestMapping("/api/ai/sessions")
@RequiredArgsConstructor
public class AiSessionController {
    private final ChatSessionService chatSessionService;

    /**
     * 分页查询会话列表
     */
    @GetMapping("/list")
    public Result<PageResult<AiChatSessionVO>> listSessions(@RequestParam String chatType,
                                                    @RequestParam(required = false) Long page,
                                                    @RequestParam(required = false) Long size) {
        return Result.success(chatSessionService.listSessions(chatType, page, size));
    }

}
