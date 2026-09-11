<template>
  <AuthLayout>
    <div class="login-card">
      <header class="auth-header">
        <h2 class="auth-title">欢迎回来</h2>
        <p class="auth-subtitle">登录 OA员工管理系统，开始高效协作</p>
      </header>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="email">
          <el-input
            v-model.trim="form.email"
            placeholder="请输入邮箱"
            clearable
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <div class="login-options">
          <router-link to="/forgot-password">忘记密码？</router-link>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            native-type="submit"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <footer class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </footer>
    </div>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import AuthLayout from '../components/AuthLayout.vue'
import { login } from '../api/auth'
import { saveLoginVO } from '../utils/auth'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  email: '',
  password: '',
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login({ email: form.email, password: form.password })
    saveLoginVO(data)

    if (data.empVO?.accountStatus === 2) {
      ElMessage.warning('账号资料尚未完善，请先完善资料')
      router.replace('/complete-profile')
    } else {
      ElMessage.success('登录成功')
      router.replace(route.query.redirect || '/home')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card {
  width: 100%;
}

.auth-header {
  margin-bottom: 36px;
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

.login-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
  font-size: 13px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.auth-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 14px;
  color: #8f959e;
}
</style>
