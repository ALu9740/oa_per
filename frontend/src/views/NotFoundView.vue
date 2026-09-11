<template>
  <div class="notfound-page">
    <div class="notfound-box">
      <div class="error-code">
        <span>4</span>
        <span>0</span>
        <span>4</span>
      </div>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除，请检查地址是否正确</p>
      <div class="error-actions">
        <template v-if="isLoggedIn">
          <el-button type="primary" size="large" @click="router.replace('/home')">
            返回首页
          </el-button>
          <el-button size="large" @click="router.replace('/login')">
            退出账号
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" size="large" @click="router.replace('/login')">
            返回登录
          </el-button>
          <el-button size="large" @click="router.replace('/register')">
            注册账号
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getToken } from '../utils/auth'

const router = useRouter()
const isLoggedIn = !!getToken()
</script>

<style scoped>
.notfound-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(37, 99, 235, 0.06), transparent 55%),
    radial-gradient(ellipse at 80% 80%, rgba(37, 99, 235, 0.08), transparent 55%),
    #f3f5fa;
}

.notfound-box {
  text-align: center;
}

.error-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 4px;
  background: linear-gradient(180deg, #2563eb 0%, #1d39c4 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
}

.error-code span:nth-child(2) {
  animation: bounce 2.4s ease-in-out infinite;
}

.error-title {
  margin-top: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2329;
}

.error-desc {
  margin-top: 12px;
  font-size: 14px;
  color: #8f959e;
}

.error-actions {
  margin-top: 36px;
  display: flex;
  justify-content: center;
  gap: 4px;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-14px);
  }
}

@media (max-width: 576px) {
  .error-code {
    font-size: 80px;
  }
}
</style>
