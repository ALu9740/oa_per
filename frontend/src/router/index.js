import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册账号' },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPasswordView.vue'),
    meta: { title: '忘记密码' },
  },
  {
    path: '/complete-profile',
    name: 'CompleteProfile',
    component: () => import('../views/CompleteProfileView.vue'),
    meta: { title: '完善资料', requiresAuth: true },
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { title: '个人信息', requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - OA员工管理系统`
    : 'OA员工管理系统'

  const token = getToken()
  const user = getUser()

  // 未登录禁止访问需认证页面
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 账号待完善（accountStatus === 2）时，强制进入完善资料页
  if (token && user?.accountStatus === 2 && to.path !== '/complete-profile') {
    return '/complete-profile'
  }

  // 已登录用户访问登录/注册页时按状态跳转
  if (token && (to.path === '/login' || to.path === '/register')) {
    return user?.accountStatus === 2 ? '/complete-profile' : '/home'
  }

  return true
})

export default router
