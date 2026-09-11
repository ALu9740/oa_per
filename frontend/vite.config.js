import fs from 'node:fs'
import { fileURLToPath } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// 复用后端的 mkcert 本地证书（覆盖 localhost / 127.0.0.1 / ::1）
const sslDir = fileURLToPath(
  new URL('../backend/src/main/resources/ssl/', import.meta.url),
)

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    https: {
      key: fs.readFileSync(`${sslDir}localhost+2-key.pem`),
      cert: fs.readFileSync(`${sslDir}localhost+2.pem`),
    },
    proxy: {
      '/api': {
        // 后端 dev 环境默认开启 HTTPS（application-dev.yml: SSL_ENABLED=true）
        // 若后端关闭了 SSL，把 target 换成 http://localhost:8097
        target: 'https://localhost:8097',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
