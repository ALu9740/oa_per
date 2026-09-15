import request from './request'
import { getToken, clearLogin } from '../utils/auth'
import { ElMessage } from 'element-plus'
import router from '../router'

// ---------- 会话管理（普通 JSON 接口，已开发） ----------

// 分页查询当前登录员工的会话列表（chatType：AGENT-管理员智能体 / RAG-员工问答）
export function getAiSessions(chatType, page = 1, size = 50) {
  return request.get('/ai/sessions/list', { params: { chatType, page, size } })
}

// 查询某个会话的消息列表（按创建时间升序）
export function getAiMessages(sessionId) {
  return request.get(`/ai/sessions/${sessionId}/messages`)
}

// 删除会话
export function deleteAiSession(sessionId) {
  return request.put(`/ai/sessions/${sessionId}/delete`)
}

// ---------- 知识库管理（管理员） ----------

// 分页查询知识库文档（fileName 模糊）
export function getKbList(params) {
  return request.get('/ai/kb/list', { params })
}

// 上传制度文档（同步解析入库，大文档耗时较长，单独放宽超时）
export function uploadKbFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/ai/kb/upload', formData, { timeout: 300000 })
}

// 删除知识库文档（同步删除向量分块）
export function deleteKbDoc(id) {
  return request.put(`/ai/kb/${id}/delete`, {}, { timeout: 120000 })
}

// ---------- 流式对话（SSE，POST + fetch 解析） ----------

const API_BASE = import.meta.env.VITE_API_BASE || '/api'

// AGENT → /api/ai/agent/chat（管理员）；RAG → /api/ai/rag/chat（全体登录员工）
export function aiChatUrl(chatType) {
  return `${API_BASE}/ai/${chatType === 'AGENT' ? 'agent' : 'rag'}/chat`
}

/**
 * SSE 流式对话。不能用 EventSource（无法携带 Authorization 头），用 fetch + ReadableStream 解析。
 * 事件协议（每行）：data:{"type":"delta|tool|done|error","data":"..."}
 *  - delta：AI 回答文本片段，按顺序拼接展示
 *  - tool ：Agent 正在调用的工具（展示用）
 *  - done ：本轮结束，data 为 sessionId（新会话需用它更新会话列表）
 *  - error：出错，data 为错误提示
 */
export async function streamChat({
  chatType,
  sessionId,
  content,
  signal,
  onDelta,
  onTool,
  onDone,
  onError,
}) {
  let res
  try {
    res = await fetch(aiChatUrl(chatType), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
      },
      body: JSON.stringify({ sessionId: sessionId ?? null, content }),
      signal,
    })
  } catch (err) {
    if (err.name === 'AbortError') return
    onError?.('网络异常，请稍后重试')
    return
  }

  // 非 2xx：后端返回的是普通 Result JSON，不是 SSE
  if (!res.ok) {
    if (res.status === 401) {
      clearLogin()
      ElMessage.error('登录已失效，请重新登录')
      router.replace('/login')
      return
    }
    const result = await res.json().catch(() => null)
    onError?.(result?.message || `请求失败（${res.status}）`)
    return
  }

  const dispatch = (payload) => {
    let event
    try {
      event = JSON.parse(payload)
    } catch {
      return
    }
    if (event.type === 'delta') onDelta?.(event.data)
    else if (event.type === 'tool') onTool?.(event.data)
    else if (event.type === 'done') onDone?.(event.data)
    else if (event.type === 'error') onError?.(event.data)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  const consume = (chunk) => {
    buffer += chunk
    const lines = buffer.split('\n')
    buffer = lines.pop() // 最后一段可能是不完整行，留到下个分片
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed.startsWith('data:')) continue
      dispatch(trimmed.slice(5).trim())
    }
  }

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      consume(decoder.decode(value, { stream: true }))
    }
    consume(decoder.decode())
    if (buffer.trim().startsWith('data:')) {
      dispatch(buffer.trim().slice(5).trim())
    }
  } catch (err) {
    if (err.name !== 'AbortError') onError?.('连接中断，请重试')
  }
}
