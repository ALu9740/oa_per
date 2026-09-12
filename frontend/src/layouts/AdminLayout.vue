<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-mark">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M12 2 3 6.5v5C3 16.6 6.8 21.7 12 23c5.2-1.3 9-6.4 9-11.5v-5L12 2zm-1 14-3.5-3.5 1.4-1.4L11 13.2l4.6-4.6L17 10l-6 6z" />
          </svg>
        </div>
        <span class="brand-text">OA员工管理系统</span>
      </div>

      <el-menu
        class="sidebar-menu"
        :default-active="activeMenu"
        :collapse="false"
        router
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <template v-if="isAdmin">
          <el-menu-item index="/employees">
            <el-icon><User /></el-icon>
            <template #title>
              <span>员工管理</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/depts">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>
              <span>部门管理</span>
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
    </aside>

    <div class="main-area">
      <header class="topbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.path !== '/home' && route.meta.title">
            {{ route.meta.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar-right">
          <div ref="searchWrapRef" class="topbar-search">
            <button
              class="icon-btn"
              :class="{ active: searchOpen }"
              title="搜索菜单"
              @click="toggleSearch"
            >
              <el-icon :size="18"><Search /></el-icon>
            </button>

            <Transition name="search-fade">
              <div v-if="searchOpen" class="search-dropdown">
                <el-input
                  ref="searchInputRef"
                  v-model.trim="menuKeyword"
                  placeholder="搜索菜单..."
                  clearable
                  :prefix-icon="Search"
                />
                <div v-if="menuKeyword" class="search-results">
                  <template v-if="matchedMenus.length">
                    <div
                      v-for="item in matchedMenus"
                      :key="item.title"
                      class="search-result-item"
                      :class="{ disabled: item.disabled }"
                      @click="goMenu(item)"
                    >
                      <el-icon class="sr-icon"><component :is="item.icon" /></el-icon>
                      <span class="sr-title">{{ item.title }}</span>
                      <span v-if="item.disabled" class="sr-tag">开发中</span>
                    </div>
                  </template>
                  <div v-else class="search-empty">
                    <el-icon :size="24"><Search /></el-icon>
                    <span>未找到匹配菜单</span>
                  </div>
                </div>
              </div>
            </Transition>
          </div>

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
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  User,
  OfficeBuilding,
  Suitcase,
  ArrowDown,
  SwitchButton,
  Search,
} from '@element-plus/icons-vue'
import { getUser, clearLogin, getRefreshToken } from '../utils/auth'
import { logout } from '../api/auth'

const route = useRoute()
const router = useRouter()
const user = getUser()

const isAdmin = computed(() => user?.roleType === 1)
const activeMenu = computed(() => {
  if (route.path.startsWith('/employees')) return '/employees'
  if (route.path.startsWith('/depts')) return '/depts'
  return '/home'
})

const avatarText = computed(() => {
  if (user?.name) return user.name.slice(0, 1)
  if (user?.email) return user.email.slice(0, 1).toUpperCase()
  return 'OA'
})

// ---------- 顶栏菜单搜索 ----------

const searchOpen = ref(false)
const menuKeyword = ref('')
const searchWrapRef = ref()
const searchInputRef = ref()

const searchMenus = computed(() => {
  const items = [{ title: '首页', path: '/home', icon: HomeFilled }]
  if (isAdmin.value) {
    items.push(
      { title: '员工管理', path: '/employees', icon: User },
      { title: '部门管理', path: '/depts', icon: OfficeBuilding },
      { title: '职位管理', icon: Suitcase, disabled: true },
    )
  }
  return items
})

const matchedMenus = computed(() => {
  const kw = menuKeyword.value.toLowerCase()
  if (!kw) return searchMenus.value
  return searchMenus.value.filter((m) => m.title.toLowerCase().includes(kw))
})

function toggleSearch() {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    menuKeyword.value = ''
    nextTick(() => searchInputRef.value?.focus())
  }
}

function goMenu(item) {
  if (item.disabled) return
  searchOpen.value = false
  router.push(item.path)
}

function onDocClick(e) {
  if (searchOpen.value && searchWrapRef.value && !searchWrapRef.value.contains(e.target)) {
    searchOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

// ---------- 用户下拉 ----------

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
.admin-layout {
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

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.topbar-search {
  position: relative;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: #4e5969;
  background: none;
  border: none;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.icon-btn:hover {
  background: #f2f3f5;
  color: #1f2329;
}

.icon-btn.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 280px;
  padding: 12px;
  background: #fff;
  border: 1px solid var(--oa-border);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(31, 35, 41, 0.1);
  z-index: 100;
}

.search-results {
  max-height: 260px;
  margin-top: 10px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.search-result-item:hover {
  background: #f2f3f5;
}

.search-result-item.disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.search-result-item.disabled:hover {
  background: none;
}

.sr-icon {
  font-size: 16px;
  color: #4e5969;
}

.sr-title {
  flex: 1;
  font-size: 14px;
  color: #1f2329;
}

.sr-tag {
  padding: 0 6px;
  font-size: 11px;
  color: #909399;
  background: #f4f4f5;
  border-radius: 4px;
}

.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 0 16px;
  font-size: 13px;
  color: #a9aeb8;
}

.search-empty :deep(.el-icon) {
  color: #c9cdd4;
}

.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}

.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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

@media (max-width: 992px) {
  .sidebar {
    display: none;
  }
}
</style>
