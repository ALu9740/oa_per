<template>
  <AuthLayout>
    <div class="register-card">
      <header class="auth-header">
        <h2 class="auth-title">注册账号</h2>
        <p class="auth-subtitle">已有账号？<router-link to="/login">去登录</router-link></p>
      </header>

      <el-steps :active="activeStep" align-center class="register-steps">
        <el-step title="填写邮箱" />
        <el-step title="验证邮箱" />
        <el-step title="设置密码" />
      </el-steps>

      <!-- 第一步：填写邮箱 -->
      <el-form
        v-show="activeStep === 0"
        ref="emailFormRef"
        :model="form"
        :rules="emailRules"
        class="auth-form"
        size="large"
        @submit.prevent="nextFromEmail"
      >
        <el-form-item prop="email">
          <el-input
            v-model.trim="form.email"
            placeholder="请输入邮箱"
            clearable
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="block-button" native-type="submit">
            下一步
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 第二步：验证邮箱验证码（仅校验，不消费） -->
      <el-form
        v-show="activeStep === 1"
        ref="codeFormRef"
        :model="form"
        :rules="codeRules"
        class="auth-form"
        size="large"
        @submit.prevent="nextFromCode"
      >
        <div class="step-hint">
          验证码已发送至 <span class="hint-email">{{ form.email }}</span>
          <a class="hint-link" @click.prevent="goEmailStep">重新填写</a>
        </div>

        <el-form-item prop="code">
          <el-input
            v-model.trim="form.code"
            placeholder="请输入6位邮箱验证码"
            maxlength="6"
            clearable
            :prefix-icon="Key"
          >
            <template #append>
              <el-button
                :disabled="countdown > 0 || sending"
                :loading="sending"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <div class="button-group">
            <el-button class="group-button" @click="goEmailStep">上一步</el-button>
            <el-button
              type="primary"
              class="group-button"
              native-type="submit"
              :loading="loading"
            >
              下一步
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 第三步：设置密码（提交时后端再次校验并删除验证码） -->
      <el-form
        v-show="activeStep === 2"
        ref="passwordFormRef"
        :model="form"
        :rules="passwordRules"
        class="auth-form"
        size="large"
        @submit.prevent="handleRegister"
      >
        <div class="step-hint">
          正在为 <span class="hint-email">{{ form.email }}</span> 设置登录密码
        </div>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item>
          <div class="button-group">
            <el-button class="group-button" @click="activeStep = 1">上一步</el-button>
            <el-button
              type="primary"
              class="group-button"
              native-type="submit"
              :loading="loading"
            >
              注 册
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import AuthLayout from '../components/AuthLayout.vue'
import { sendCode, verifyCode, register } from '../api/auth'
import { saveLoginVO } from '../utils/auth'

const router = useRouter()

const activeStep = ref(0)
const loading = ref(false)
const sending = ref(false)
const form = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
})

const emailFormRef = ref()
const codeFormRef = ref()
const passwordFormRef = ref()

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

const codeRules = {
  code: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位字符', trigger: 'blur' },
  ],
}

const passwordRules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const countdown = ref(0)
let countdownTimer = null

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

onUnmounted(stopCountdown)

function handleSendCode() {
  if (countdown.value > 0 || sending.value) return
  sending.value = true
  sendCode(form.email)
    .then(() => {
      ElMessage.success(`验证码已发送至 ${form.email}，请查收`)
      startCountdown()
    })
    .catch(() => {})
    .finally(() => {
      sending.value = false
    })
}

function goEmailStep() {
  stopCountdown()
  form.code = ''
  activeStep.value = 0
}

async function nextFromEmail() {
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return

  activeStep.value = 1
  // 进入验证步骤后自动发送验证码（若不在冷却中）
  if (countdown.value === 0) {
    handleSendCode()
  }
}

async function nextFromCode() {
  const valid = await codeFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 只做校验，不消费验证码（真正的删除发生在第三步注册成功后）
    const matched = await verifyCode(form.email, form.code)
    if (!matched) {
      ElMessage.error('验证码不正确，请重新输入')
      return
    }
    activeStep.value = 2
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await register({
      email: form.email,
      password: form.password,
      code: form.code,
    })
    saveLoginVO(data)
    stopCountdown()
    ElMessage.success('注册成功，请完善个人资料')
    router.replace('/complete-profile')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-card {
  width: 100%;
}

.auth-header {
  margin-bottom: 28px;
}

.auth-title {
  font-size: 26px;
  font-weight: 600;
  color: #1f2329;
}

.auth-subtitle {
  margin-top: 10px;
  font-size: 14px;
  color: #8f959e;
}

.register-steps {
  margin-bottom: 32px;
}

.step-hint {
  margin-bottom: 20px;
  padding: 10px 14px;
  font-size: 13px;
  color: #4e5969;
  background: #f2f6ff;
  border-radius: 6px;
}

.hint-email {
  color: #2563eb;
  font-weight: 500;
}

.hint-link {
  float: right;
  cursor: pointer;
}

.block-button {
  width: 100%;
}

.button-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.group-button {
  flex: 1;
  height: 42px;
  font-size: 15px;
  letter-spacing: 2px;
}

:deep(.el-input-group__append) {
  padding: 0 8px;
  background: #fff;
}

:deep(.el-input-group__append .el-button) {
  height: 32px;
  font-size: 13px;
  letter-spacing: 0;
}
</style>
