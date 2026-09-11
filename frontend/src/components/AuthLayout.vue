<template>
  <div class="auth-page">
    <aside class="brand-panel">
      <div class="brand-decoration brand-decoration--1"></div>
      <div class="brand-decoration brand-decoration--2"></div>

      <div class="brand-header">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
            <path d="M12 2 3 6.5v5C3 16.6 6.8 21.7 12 23c5.2-1.3 9-6.4 9-11.5v-5L12 2zm-1 14-3.5-3.5 1.4-1.4L11 13.2l4.6-4.6L17 10l-6 6z" />
          </svg>
        </div>
        <span class="brand-name">OA员工管理系统</span>
      </div>

      <div class="brand-body">
        <h1 class="brand-title">轻量化企业协作平台</h1>
        <p class="brand-desc">组织人事一体化管理，助力团队高效协作</p>
        <ul class="brand-features">
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>员工信息统一管理，增删改查一步到位</span>
          </li>
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>部门职位体系化配置，组织架构清晰可见</span>
          </li>
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>管理员与员工双角色，权限控制精细安全</span>
          </li>
        </ul>
      </div>

      <div class="brand-footer">© 2026 OA员工管理系统 · 让协作更简单</div>
    </aside>

    <main class="form-panel">
      <div class="form-container">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
import { CircleCheckFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  background: #fff;
}

.brand-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 46%;
  min-height: 100vh;
  padding: 48px 56px;
  color: #fff;
  background: linear-gradient(135deg, #1d39c4 0%, #2563eb 55%, #4096ff 100%);
  overflow: hidden;
}

.brand-decoration {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.brand-decoration--1 {
  width: 420px;
  height: 420px;
  top: -140px;
  right: -140px;
}

.brand-decoration--2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  left: -80px;
  background: rgba(255, 255, 255, 0.06);
}

.brand-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.brand-body {
  position: relative;
  max-width: 420px;
}

.brand-title {
  font-size: 34px;
  font-weight: 600;
  line-height: 1.4;
  letter-spacing: 1px;
}

.brand-desc {
  margin-top: 14px;
  font-size: 15px;
  opacity: 0.85;
}

.brand-features {
  margin-top: 44px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  list-style: none;
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.92;
}

.brand-footer {
  position: relative;
  font-size: 13px;
  opacity: 0.7;
}

.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

@media (max-width: 992px) {
  .brand-panel {
    display: none;
  }
}
</style>
