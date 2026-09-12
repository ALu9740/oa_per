import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearLogin } from '../utils/auth'

const request = axios.create({
  // 优先使用环境变量 VITE_API_BASE，未配置时退回 /api（走 vite proxy）
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      return result.data
    }
    ElMessage.error(result.message || '操作失败')
    return Promise.reject(new Error(result.message || '操作失败'))
  },
  (error) => {
    if (error.config?.skipAuthRedirect) {
      // 调用方自行处理错误（如登出：401 也算正常结局）
      return Promise.reject(error)
    }
    const result = error.response?.data
    if (error.response?.status === 401) {
      clearLogin()
      ElMessage.error('登录已失效，请重新登录')
      window.location.replace('/login')
    } else {
      ElMessage.error(result?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default request
