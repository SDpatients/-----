import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    // 测试环境：happy-dom 轻量且快速
    environment: 'happy-dom',

    // 全局 setup 文件
    setupFiles: ['./src/__tests__/setup.ts'],

    // 全局引入 describe/it/expect，无需每个文件手动 import
    globals: true,

    // CSS 处理：测试中跳过 CSS 解析
    css: false,

    // 报告器
    reporters: [
      'default',
      ['html', { outputFile: 'coverage/index.html' }],
      ['junit', { outputFile: 'test-results/junit.xml' }],
    ],

    // 覆盖率配置
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov', 'json-summary'],
      reportsDirectory: './coverage',
      include: ['src/**/*.{ts,vue}'],
      exclude: [
        'src/__tests__/**',
        'src/**/*.d.ts',
        'src/main.ts',
        'src/vite-env.d.ts',
      ],
      thresholds: {
        lines: 50,
        branches: 40,
        functions: 50,
        statements: 50,
      },
    },

    // 排除 e2e 目录
    exclude: ['e2e/**', 'node_modules/**'],

    // Mock 清理
    clearMocks: true,
    restoreMocks: true,
  },
})