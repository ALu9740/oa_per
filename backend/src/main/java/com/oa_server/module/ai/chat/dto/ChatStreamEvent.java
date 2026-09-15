package com.oa_server.module.ai.chat.dto;

/**
 * SSE 流式事件（管理员 Agent 与员工 RAG 共用的出参协议）
 *
 * type 取值：
 *  - delta : AI 回答的一个文本片段，前端按顺序拼接展示
 *  - tool  : Agent 正在调用某个工具（前端可展示"正在查询员工..."）
 *  - done  : 本轮结束，data 为 sessionId（新会话时前端要用它更新会话列表）
 *  - error : 出错，data 为错误提示
 *
 * @author Alu
 * @date 2026-09-15
 */
public record ChatStreamEvent(String type, String data) {

    public static ChatStreamEvent delta(String text) {
        return new ChatStreamEvent("delta", text);
    }

    public static ChatStreamEvent tool(String text) {
        return new ChatStreamEvent("tool", text);
    }

    public static ChatStreamEvent done(String sessionId) {
        return new ChatStreamEvent("done", sessionId);
    }

    public static ChatStreamEvent error(String message) {
        return new ChatStreamEvent("error", message);
    }
}
