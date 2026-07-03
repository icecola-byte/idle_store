import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve:{
    alias: {
      '@': path.resolve(__dirname, './src') // 当我们使用 @ 时就相当于 ./src, 比较方便, 需要安装 types/node 包
    },
  }
})
