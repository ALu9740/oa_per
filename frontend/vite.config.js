import fs from 'node:fs'
import path from 'node:path'
import vue from '@vitejs/plugin-vue'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 读取 .env / .env.development / .env.production
  // prefixes 传 '' 以同时拿到 VITE_* 与服务端用变量（SSL_ENABLED/SSL_CERT_PATH 等）
  const env = loadEnv(mode, process.cwd(), '')

  // ---- SSL（开发环境 HTTPS）----
  const sslEnabled = env.SSL_ENABLED === 'true'
  const sslCertPath = path.resolve(env.SSL_CERT_PATH || '')
  const sslKeyPath = path.resolve(env.SSL_KEY_PATH || '')
  const hasCert =
    sslEnabled &&
    fs.existsSync(sslCertPath) &&
    fs.existsSync(sslKeyPath)

  const https = hasCert
    ? {
        key: fs.readFileSync(sslKeyPath),
        cert: fs.readFileSync(sslCertPath),
      }
    : undefined

  // ---- 代理目标（开发环境转发给后端）----
  const proxyTarget = env.VITE_PROXY_TARGET || 'https://localhost:8097'

  return {
    plugins: [vue()],
    server: {
      port: 5173,
      host: env.VITE_DEV_HOST || 'localhost',
      https,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          secure: false,
        },
        // MinIO 文件访问：由后端 MinIOController 代理读取（bucket 保持私有）
        // 注意：不要 rewrite，后端 Controller 需要完整路径 /minio/{bucket}/{object}
        '/minio': {
          target: proxyTarget,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})