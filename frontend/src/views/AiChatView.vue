<template>
  <div class="chat-page">
    <!-- 左侧：会话列表 -->
    <aside class="session-panel">
      <div class="session-panel-header">
        <span class="session-panel-title">会话记录</span>
        <el-button type="primary" size="small" :icon="Plus" @click="startNewSession">
          新会话
        </el-button>
      </div>

      <div v-loading="sessionLoading" class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: String(s.id) === String(currentSessionId) }"
          @click="openSession(s)"
        >
          <div class="session-item-main">
            <div class="session-item-title">{{ s.title || '新会话' }}</div>
            <div class="session-item-time">{{ formatSessionTime(s.updatedAt || s.createdAt) }}</div>
          </div>
          <el-popconfirm
            title="删除后聊天记录不可恢复，确定删除？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDeleteSession(s)"
          >
            <template #reference>
              <el-icon class="session-item-del" @click.stop><Delete /></el-icon>
            </template>
          </el-popconfirm>
        </div>

        <el-empty
          v-if="!sessionLoading && !sessions.length"
          description="暂无会话"
          :image-size="64"
        />

        <div v-if="hasMoreSessions" class="session-more" @click="loadMoreSessions">
          加载更多
        </div>
      </div>
    </aside>

    <!-- 右侧：消息区 + 输入区 -->
    <section class="chat-panel">
      <div v-loading="messageLoading" class="message-list" ref="messageListRef">
        <!-- 欢迎页 -->
        <div v-if="!messages.length" class="chat-welcome">
          <div class="welcome-icon">
            <el-icon :size="26">
              <MagicStick v-if="isAgent" />
              <ChatDotRound v-else />
            </el-icon>
          </div>
          <h3 class="welcome-title">{{ isAgent ? '智能助手' : 'AI 问答' }}</h3>
          <p class="welcome-desc">{{ welcomeDesc }}</p>
          <div class="welcome-suggestions">
            <button
              v-for="q in presetQuestions"
              :key="q"
              class="suggestion-chip"
              @click="sendPreset(q)"
            >
              {{ q }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(m, i) in messages"
          :key="i"
          class="msg-row"
          :class="m.role"
        >
          <div class="msg-avatar" :class="m.role">
            <template v-if="m.role === 'user'">
              <img v-if="userAvatar" :src="userAvatar" class="msg-avatar-img" alt="头像" />
              <template v-else>{{ userInitial }}</template>
            </template>
            <el-icon v-else :size="16">
              <MagicStick v-if="isAgent" />
              <ChatDotRound v-else />
            </el-icon>
          </div>

          <div class="msg-main">
            <!-- Agent 工具调用提示 -->
            <div v-if="m.role === 'assistant' && m.toolTips.length" class="tool-block">
              <div v-for="(t, ti) in m.toolTips" :key="ti" class="tool-item">
                <el-icon :class="{ spin: t.pending }"><Loading /></el-icon>
                <span>{{ t.text }}</span>
              </div>
            </div>

            <div
              v-if="m.content"
              class="msg-bubble"
              :class="{ 'is-error': m.isError }"
            >
              <!-- AI 回复渲染 Markdown；用户消息与错误提示保持纯文本 -->
              <div
                v-if="m.role === 'assistant' && !m.isError"
                class="md-content"
                v-html="renderMarkdown(m.content)"
              ></div>
              <template v-else>{{ m.content }}</template>
              <span v-if="m.streaming" class="stream-cursor"></span>
            </div>
            <div v-else-if="m.streaming" class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          resize="none"
          :maxlength="4000"
          :placeholder="inputPlaceholder"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-actions">
          <span class="input-hint">Enter 发送，Shift + Enter 换行</span>
          <el-button v-if="!sending" type="primary" :disabled="!inputText.trim()" @click="handleSend">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
          <el-button v-else type="danger" plain @click="stopStream">
            <el-icon><VideoPause /></el-icon>
            停止
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Delete,
  Loading,
  MagicStick,
  Plus,
  Promotion,
  VideoPause,
} from '@element-plus/icons-vue'
import { deleteAiSession, getAiMessages, getAiSessions, streamChat } from '../api/ai'
import { getUser } from '../utils/auth'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const user = getUser()

// AI 回复为 Markdown 格式，消毒后渲染（v-html 前必须防 XSS）
marked.setOptions({ breaks: true, gfm: true })

function renderMarkdown(text) {
  if (!text) return ''
  return DOMPurify.sanitize(marked.parse(String(text)))
}

// AGENT-管理员智能体 / RAG-员工问答（由路由 meta 决定）
const chatType = computed(() => (route.meta.chatType === 'AGENT' ? 'AGENT' : 'RAG'))
const isAgent = computed(() => chatType.value === 'AGENT')

const PAGE_SIZE = 50

// ---------- 会话列表 ----------

const sessions = ref([])
const sessionLoading = ref(false)
const sessionTotal = ref(0)
const sessionPage = ref(1)
const hasMoreSessions = computed(() => sessions.value.length < sessionTotal.value)

async function loadSessions(reset = true) {
  if (reset) sessionPage.value = 1
  sessionLoading.value = true
  try {
    const data = await getAiSessions(chatType.value, sessionPage.value, PAGE_SIZE)
    sessions.value = reset ? data.records : sessions.value.concat(data.records || [])
    sessionTotal.value = Number(data.total || 0)
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    sessionLoading.value = false
  }
}

function loadMoreSessions() {
  sessionPage.value += 1
  loadSessions(false)
}

// ---------- 消息区 ----------

const currentSessionId = ref(null)
const messages = ref([])
const messageLoading = ref(false)
const messageListRef = ref()

async function openSession(s) {
  if (String(s.id) === String(currentSessionId.value)) return
  stopStream()
  currentSessionId.value = s.id
  messages.value = []
  messageLoading.value = true
  try {
    const list = await getAiMessages(s.id)
    messages.value = (list || []).map((m) => ({
      role: m.role,
      content: m.content,
      toolTips: [],
    }))
    scrollToBottom()
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    messageLoading.value = false
  }
}

function startNewSession() {
  stopStream()
  currentSessionId.value = null
  messages.value = []
}

async function handleDeleteSession(s) {
  try {
    await deleteAiSession(s.id)
    ElMessage.success('会话已删除')
    if (String(s.id) === String(currentSessionId.value)) {
      startNewSession()
    }
    loadSessions()
  } catch {
    // 错误提示由拦截器统一处理
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// ---------- 发送 / 流式接收 ----------

const inputText = ref('')
const sending = ref(false)
let abortController = null

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return
  inputText.value = ''
  messages.value.push({ role: 'user', content: text })

  const aiMsg = reactive({ role: 'assistant', content: '', toolTips: [], streaming: true })
  messages.value.push(aiMsg)
  sending.value = true
  abortController = new AbortController()
  scrollToBottom()

  const finishToolTips = () => aiMsg.toolTips.forEach((t) => (t.pending = false))

  await streamChat({
    chatType: chatType.value,
    sessionId: currentSessionId.value,
    content: text,
    signal: abortController.signal,
    onDelta: (t) => {
      aiMsg.content += t
      scrollToBottom()
    },
    onTool: (t) => {
      finishToolTips()
      aiMsg.toolTips.push({ text: formatToolText(t), pending: true })
    },
    onDone: (sid) => {
      finishToolTips()
      aiMsg.streaming = false
      if (sid) currentSessionId.value = sid
      loadSessions()
    },
    onError: (msg) => {
      finishToolTips()
      if (!aiMsg.content) {
        aiMsg.isError = true
        aiMsg.content = msg
      } else {
        aiMsg.content += `\n\n[${msg}]`
      }
      ElMessage.error(msg)
    },
  })

  sending.value = false
  abortController = null
  aiMsg.streaming = false
  if (!aiMsg.content && !aiMsg.isError) {
    aiMsg.isError = true
    aiMsg.content = '本次对话未收到任何返回，请检查后端接口'
  }
  scrollToBottom()
}

function stopStream() {
  abortController?.abort()
  abortController = null
}

function sendPreset(q) {
  inputText.value = q
  handleSend()
}

function formatToolText(t) {
  const text = String(t || '')
  const label = `调用工具 ${text}`
  return label.length > 90 ? `${label.slice(0, 90)}…` : label
}

// ---------- 展示辅助 ----------

const userAvatar = user?.avatar || ''

const userInitial = computed(() => {
  if (user?.name) return user.name.slice(0, 1)
  if (user?.email) return user.email.slice(0, 1).toUpperCase()
  return '我'
})

const welcomeDesc = computed(() =>
  isAgent.value
    ? '我可以帮你查询员工/部门/职位、调动员工部门、启用或禁用账号，支持多轮连续指令。'
    : '基于公司制度知识库回答问题，例如账号密码、头像上传、部门职位制度等系统使用问题。',
)

const presetQuestions = computed(() =>
  isAgent.value
    ? ['查询名叫张三的员工信息', '把张三调到技术部', '列出系统里所有的部门', '禁用张三的账号']
    : ['新员工的初始密码是什么？', '忘记密码了怎么办？', '怎么更换自己的头像？', '想换部门或职位该找谁？'],
)

const inputPlaceholder = computed(() =>
  isAgent.value ? '输入指令，例如：把张三调到技术部' : '输入问题，例如：忘记密码了怎么办？',
)

function formatSessionTime(value) {
  if (!value) return ''
  const d = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  const now = new Date()
  if (d.getFullYear() === now.getFullYear()) {
    return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${hm}`
  }
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${hm}`
}

// 同一组件实例在 /ai/agent 与 /ai/ask 间切换时重置状态
watch(
  () => route.meta.chatType,
  () => {
    stopStream()
    currentSessionId.value = null
    messages.value = []
    inputText.value = ''
    sending.value = false
    loadSessions()
  },
)

onMounted(() => loadSessions())
onBeforeUnmount(() => stopStream())
</script>

<style scoped>
.chat-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 108px);
}

/* ---------- 左侧会话栏 ---------- */

.session-panel {
  display: flex;
  flex-direction: column;
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
  overflow: hidden;
}

.session-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--oa-border);
}

.session-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--oa-text);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.session-item:hover {
  background: #f2f3f5;
}

.session-item.active {
  background: var(--el-color-primary-light-9);
}

.session-item.active .session-item-title {
  color: var(--el-color-primary);
}

.session-item-main {
  flex: 1;
  min-width: 0;
}

.session-item-title {
  font-size: 13px;
  color: var(--oa-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item-time {
  margin-top: 3px;
  font-size: 12px;
  color: #a9aeb8;
}

.session-item-del {
  flex-shrink: 0;
  font-size: 14px;
  color: #909399;
  opacity: 0;
  transition: opacity 0.15s, color 0.15s;
}

.session-item:hover .session-item-del {
  opacity: 1;
}

.session-item-del:hover {
  color: #f56c6c;
}

.session-more {
  padding: 8px 0;
  text-align: center;
  font-size: 12px;
  color: var(--el-color-primary);
  cursor: pointer;
}

/* ---------- 右侧聊天区 ---------- */

.chat-panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
}

/* 欢迎页 */

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 56px 24px 24px;
}

.welcome-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.welcome-title {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  color: var(--oa-text);
}

.welcome-desc {
  margin-top: 8px;
  max-width: 460px;
  font-size: 13px;
  line-height: 1.7;
  text-align: center;
  color: var(--oa-text-secondary);
}

.welcome-suggestions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.suggestion-chip {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--oa-text-secondary);
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s, background 0.15s;
}

.suggestion-chip:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

/* 消息气泡 */

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
}

.msg-avatar.user {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #5b8def);
}

.msg-avatar.assistant {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.msg-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.msg-main {
  display: flex;
  flex-direction: column;
  max-width: 72%;
}

.msg-row.user .msg-main {
  align-items: flex-end;
}

.msg-bubble {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 2px 12px 12px 12px;
  background: #f5f7fa;
  color: var(--oa-text);
}

.msg-row.user .msg-bubble {
  border-radius: 12px 2px 12px 12px;
  color: #fff;
  background: var(--el-color-primary);
}

.msg-bubble.is-error {
  color: #f56c6c;
  background: #fef0f0;
}

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 14px;
  margin-left: 2px;
  vertical-align: -2px;
  background: currentColor;
  animation: cursor-blink 0.8s step-end infinite;
}

@keyframes cursor-blink {
  50% {
    opacity: 0;
  }
}

/* Markdown 内容渲染样式（v-html 子元素需用 :deep） */

.md-content {
  white-space: normal;
}

.md-content :deep(p) {
  margin: 0 0 8px;
}

.md-content :deep(p:last-child) {
  margin-bottom: 0;
}

.md-content :deep(ul),
.md-content :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 20px;
}

.md-content :deep(li) {
  margin: 2px 0;
}

.md-content :deep(strong) {
  font-weight: 600;
}

.md-content :deep(h3),
.md-content :deep(h4) {
  margin: 10px 0 6px;
  font-size: 14px;
  font-weight: 600;
}

.md-content :deep(code) {
  padding: 1px 5px;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
  background: rgba(31, 35, 41, 0.07);
  border-radius: 4px;
}

.md-content :deep(pre) {
  margin: 8px 0;
  padding: 10px 12px;
  background: #f0f2f5;
  border-radius: 8px;
  overflow-x: auto;
}

.md-content :deep(pre code) {
  padding: 0;
  background: none;
}

.md-content :deep(table) {
  margin: 8px 0;
  border-collapse: collapse;
}

.md-content :deep(th),
.md-content :deep(td) {
  padding: 5px 10px;
  font-size: 13px;
  border: 1px solid var(--oa-border);
}

.md-content :deep(th) {
  background: #f5f7fa;
}

.md-content :deep(blockquote) {
  margin: 8px 0;
  padding-left: 10px;
  color: var(--oa-text-secondary);
  border-left: 3px solid var(--oa-border);
}

.md-content :deep(a) {
  color: var(--el-color-primary);
}

/* 输入中动画 */

.typing-dots {
  display: flex;
  gap: 5px;
  padding: 14px 14px;
  border-radius: 2px 12px 12px 12px;
  background: #f5f7fa;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a9aeb8;
  animation: dot-bounce 1.2s ease-in-out infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes dot-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* 工具调用提示 */

.tool-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 8px;
}

.tool-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--oa-text-secondary);
  background: #f7f9fc;
  border: 1px dashed var(--oa-border);
  border-radius: 6px;
}

.tool-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-item .spin {
  animation: tool-spin 1s linear infinite;
}

@keyframes tool-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 输入区 */

.input-area {
  padding: 14px 20px 12px;
  border-top: 1px solid var(--oa-border);
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.input-hint {
  font-size: 12px;
  color: #a9aeb8;
}

@media (max-width: 992px) {
  .session-panel {
    display: none;
  }
}
</style>
