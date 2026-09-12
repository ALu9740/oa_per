<template>
  <div class="home-page">
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
        </div>
        <div class="module-cards">
              <div
                v-for="item in modules"
                :key="item.title"
                class="module-card"
                :class="{ 'is-link': item.path }"
                @click="item.path && router.push(item.path)"
              >
                <el-icon class="module-icon" :size="18">
                  <component :is="item.icon" />
                </el-icon>
                <div class="module-info">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.desc }}</p>
                </div>
                <el-tag v-if="!item.path" size="small" type="info" effect="plain">开发中</el-tag>
              </div>
            </div>
      </section>

      <section class="profile-card">
        <div class="card-header">
          <h3>系统信息</h3>
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
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, OfficeBuilding, Suitcase } from '@element-plus/icons-vue'
import { getUser } from '../utils/auth'

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
    path: '/employees',
  },
  {
    title: '部门管理',
    desc: '部门维护与组织架构管理',
    icon: OfficeBuilding,
    path: '/depts',
  },
  {
    title: '职位管理',
    desc: '职位基础数据统一维护',
    icon: Suitcase,
  },
]
</script>

<style scoped>
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
  transition: border-color 0.2s, box-shadow 0.2s;
}

.module-card.is-link {
  cursor: pointer;
}

.module-card.is-link:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
}

.module-card.is-link:hover .module-icon {
  color: var(--el-color-primary);
}

.module-icon {
  flex-shrink: 0;
  color: #4e5969;
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
</style>
