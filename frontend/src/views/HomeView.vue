<template>
  <div class="home-layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-mark">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M12 2 3 6.5v5C3 16.6 6.8 21.7 12 23c5.2-1.3 9-6.4 9-11.5v-5L12 2zm-1 14-3.5-3.5 1.4-1.4L11 13.2l4.6-4.6L17 10l-6 6z" />
          </svg>
        </div>
        <span class="brand-text">OA员工管理系统</span>
      </div>

      <el-menu class="sidebar-menu" :default-active="'home'" :collapse="false">
        <el-menu-item index="home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <template v-if="isAdmin">
          <el-menu-item index="employees" disabled>
            <el-icon><User /></el-icon>
            <template #title>
              <span>员工管理</span>
              <span class="menu-tag">开发中</span>
            </template>
          </el-menu-item>
          <el-menu-item index="depts" disabled>
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>
              <span>部门管理</span>
              <span class="menu-tag">开发中</span>
            </template>
          </el-menu-item>
          <el-menu-item index="jobs" disabled>
            <el-icon><Suitcase /></el-icon>
            <template #title>
              <span>职位管理</span>
              <span class="menu-tag">开发中</span>
            </template>
          </el-menu-item>
        </template>
      </el-menu>

      <div class="sidebar-footer">v0.0.1</div>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        </el-breadcrumb>

        <el-dropdown @command="handleCommand">
          <span class="user-entry">
            <span class="user-avatar">
              <img v-if="user?.avatar" :src="user.avatar" class="user-avatar-img" alt="头像" />
              <template v-else>{{ avatarText }}</template>
            </span>
            <span class="user-name">{{ user?.name || user?.email || '用户' }}</span>
            <el-icon class="user-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                个人信息
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="content">
        <section class="welcome-banner">
          <div class="welcome-text">
            <h2>{{ greeting }}，{{ user?.name || '员工' }} 👋</h2>
            <p>欢迎使用 OA员工管理系统，祝您工作顺利</p>
            <div class="welcome-datetime">
              <span class="datetime-time">{{ clock.time }}</span>
              <span class="datetime-date">{{ clock.date }}</span>
            </div>
          </div>
        </section>

        <section class="profile-card">
          <div class="card-header">
            <h3>我的信息</h3>
            <span class="card-subtitle">以下为账号基本信息</span>
          </div>
          <el-descriptions :column="3" border class="profile-descriptions">
            <el-descriptions-item label="姓名">
              {{ user?.name || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ genderText }}
            </el-descriptions-item>
            <el-descriptions-item label="员工编号">
              {{ user?.empNo || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ user?.email || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ user?.phone || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </section>

        <template v-if="isAdmin">
          <section class="profile-card">
            <div class="card-header">
              <h3>快捷入口</h3>
              <span class="card-subtitle">常用管理功能快速访问</span>
            </div>
            <div class="module-cards">
              <div v-for="item in modules" :key="item.title" class="module-card">
                <div class="module-icon" :style="{ background: item.bg }">
                  <el-icon :size="22"><component :is="item.icon" /></el-icon>
                </div>
                <div class="module-info">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.desc }}</p>
                </div>
                <el-tag size="small" type="info" effect="plain">开发中</el-tag>
              </div>
            </div>
          </section>

          <section class="profile-card">
            <div class="card-header">
              <h3>系统信息</h3>
              <span class="card-subtitle">当前系统运行环境</span>
            </div>
            <el-descriptions :column="3" border class="profile-descriptions">
              <el-descriptions-item label="系统名称">OA-PER</el-descriptions-item>
              <el-descriptions-item label="系统版本">v0.0.1</el-descriptions-item>
              <el-descriptions-item label="缓存">Redis</el-descriptions-item>
              <el-descriptions-item label="前端框架">Vue 3 + Vite + Element Plus</el-descriptions-item>
              <el-descriptions-item label="后端框架">Spring Boot 3.5</el-descriptions-item>
              <el-descriptions-item label="数据库">MySQL</el-descriptions-item>
            </el-descriptions>
          </section>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  User,
  OfficeBuilding,
  Suitcase,
  ArrowDown,
  SwitchButton,
} from '@element-plus/icons-vue'
import { getUser, clearLogin, getRefreshToken } from '../utils/auth'
import { logout } from '../api/auth'

const router = useRouter()
const user = getUser()

const isAdmin = computed(() => user?.roleType === 1)

const now = ref(new Date())
let clockTimer = null

const clock = computed(() => {
  const d = now.value
  const pad = (n) => String(n).padStart(2, '0')
  const time = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const date = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
  return { time, date }
})

onMounted(() => {
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  clearInterval(clockTimer)
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const avatarText = computed(() => {
  if (user?.name) return user.name.slice(0, 1)
  if (user?.email) return user.email.slice(0, 1).toUpperCase()
  return 'OA'
})

const genderText = computed(() => {
  if (user?.gender === 1) return '男'
  if (user?.gender === 0) return '女'
  return '—'
})

const statusTag = computed(() => {
  switch (user?.accountStatus) {
    case 1:
      return { type: 'success', label: '正常' }
    case 2:
      return { type: 'warning', label: '待完善' }
    case 0:
      return { type: 'danger', label: '禁用' }
    default:
      return { type: 'info', label: '未知' }
  }
})

const modules = [
  {
    title: '员工管理',
    desc: '员工信息分页查询、新增编辑、状态管理',
    icon: User,
    bg: 'linear-gradient(135deg, #2563eb, #5b8def)',
  },
  {
    title: '部门管理',
    desc: '部门维护与组织架构管理',
    icon: OfficeBuilding,
    bg: 'linear-gradient(135deg, #0ca678, #38d9a9)',
  },
  {
    title: '职位管理',
    desc: '职位基础数据统一维护',
    icon: Suitcase,
    bg: 'linear-gradient(135deg, #d9480f, #ff922b)',
  },
]

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    const refreshToken = getRefreshToken()
    // 通知后端使 token 失效（即使失败也不阻塞本地登出）
    logout(refreshToken).catch(() => {})
    clearLogin()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 232px;
  background: #fff;
  border-right: 1px solid var(--oa-border);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 60px;
  padding: 0 18px;
  border-bottom: 1px solid var(--oa-border);
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #5b8def);
  flex-shrink: 0;
}

.brand-text {
  font-size: 14px;
  font-weight: 600;
  color: #1f2329;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 8px;
}

.menu-tag {
  margin-left: 8px;
  padding: 0 6px;
  font-size: 11px;
  color: #909399;
  background: #f4f4f5;
  border-radius: 4px;
}

.sidebar-footer {
  padding: 14px;
  font-size: 12px;
  color: #a9aeb8;
  text-align: center;
  border-top: 1px solid var(--oa-border);
}

.main-area {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--oa-border);
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  outline: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #2563eb, #5b8def);
  overflow: hidden;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  max-width: 140px;
  font-size: 14px;
  color: #1f2329;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 12px;
  color: #8f959e;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.welcome-banner {
  padding: 28px 32px;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
}

.welcome-text h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1f2329;
}

.welcome-text p {
  margin-top: 8px;
  font-size: 13px;
  color: #8f959e;
}

.welcome-datetime {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-top: 14px;
}

.datetime-time {
  font-size: 28px;
  font-weight: 700;
  color: #1f2329;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.datetime-date {
  font-size: 13px;
  color: #8f959e;
}

.profile-card {
  margin-top: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid var(--oa-border);
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 18px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
}

.card-subtitle {
  font-size: 13px;
  color: #8f959e;
}

.profile-descriptions {
  --el-descriptions-item-bordered-label-background: #fafbfc;
}

.module-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.module-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fafbfc;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  color: #fff;
  flex-shrink: 0;
}

.module-info {
  flex: 1;
  min-width: 0;
}

.module-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2329;
}

.module-info p {
  margin-top: 4px;
  font-size: 12px;
  color: #8f959e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 992px) {
  .sidebar {
    display: none;
  }
}
</style>
