import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearLogin } from '../utils/auth'

const request = axios.create({
  baseURL: '/api',
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
