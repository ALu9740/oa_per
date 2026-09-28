<template>
  <el-dialog
    v-model="visible"
    title="安全验证"
    width="366px"
    align-center
    append-to-body
    :close-on-click-modal="false"
    class="captcha-dialog"
    @opened="handleOpened"
  >
    <div :id="boxId" class="captcha-box"></div>
  </el-dialog>
</template>

<script setup>
import { ref, onBeforeUnmount, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['success'])

const visible = ref(false)
let tac = null
// 每个组件实例独立的挂载点 id，避免多实例时 bindEl 选择器冲突
const boxId = `tac-box-${getCurrentInstance().uid}`

function open() {
  visible.value = true
}

function ensureLoader() {
  if (window.loadTAC) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `${import.meta.env.BASE_URL}tac/load.js`
    script.onload = resolve
    script.onerror = () => reject(new Error('验证码脚本加载失败'))
    document.head.appendChild(script)
  })
}

async function handleOpened() {
  try {
    await ensureLoader()
    // 已初始化过：刷新一张新图（旧 token 一次性，避免复用过期凭证）
    if (tac) {
      tac.reloadCaptcha()
      return
    }
    tac = await window.loadTAC(
      { url: `${import.meta.env.BASE_URL}tac/` },
      {
        bindEl: `#${boxId}`,
        requestCaptchaDataUrl: '/api/captcha/generation',
        validCaptchaUrl: '/api/captcha/check',
        validSuccess: (res) => {
          const token = res?.data?.token
          if (token) {
            visible.value = false
            emit('success', token)
          } else {
            tac?.reloadCaptcha()
          }
        },
        // 校验失败时不覆写 validFail，走 SDK 默认的自动刷新重试
      },
      { logoUrl: null },
    )
    tac.init()
  } catch (e) {
    console.error('[CaptchaSlider]', e)
    ElMessage.error('验证码加载失败，请稍后重试')
    visible.value = false
  }
}

onBeforeUnmount(() => {
  try {
    tac?.destroyWindow()
  } catch (e) {
    // 忽略卸载清理异常
  }
  tac = null
})

defineExpose({ open })
</script>

<style scoped>
.captcha-box {
  width: 318px;
  margin: 0 auto;
}

:deep(.captcha-dialog .el-dialog__body) {
  padding: 12px 16px 20px;
}
</style>
