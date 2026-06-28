import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [
        ElementPlusResolver({
          importStyle: 'sass',
        }),
      ],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/variables.scss" as *;`,
      },
    },
  },
  server: {
    port: 5173,
    host: true,
    proxy: {
      // 统一走网关（注入 X-User-Id）；未启网关时可改回 8081 直连（需 auth 内 JwtUserIdHeaderFilter）
      '/api': {
        target: 'http://localhost:9001',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id: string) {
          const normalizedId = id.replace(/\\/g, '/')
          if (normalizedId.includes('node_modules')) {
            if (normalizedId.includes('@element-plus/icons-vue')) {
              return 'element-icons'
            }
            if (normalizedId.includes('element-plus')) {
              if (normalizedId.includes('element-plus/es/components/')) {
                if (/\/(table|table-column|pagination|tree|virtual-list)\//.test(normalizedId)) {
                  return 'element-plus-data'
                }
                if (
                  /\/(form|form-item|input|input-number|select|option|option-group|radio|radio-button|radio-group|checkbox|checkbox-group|switch|slider|date-picker|upload)\//.test(normalizedId)
                ) {
                  return 'element-plus-form'
                }
                if (
                  /\/(dialog|drawer|popover|popper|tooltip|dropdown|dropdown-item|dropdown-menu|message|message-box|notification|loading|overlay)\//.test(normalizedId)
                ) {
                  return 'element-plus-overlay'
                }
                if (
                  /\/(menu|menu-item|sub-menu|tabs|tab-pane|breadcrumb|breadcrumb-item|steps|step|timeline|timeline-item|collapse|collapse-item)\//.test(normalizedId)
                ) {
                  return 'element-plus-nav'
                }
                return 'element-plus-components'
              }
              if (normalizedId.includes('element-plus/theme-chalk')) {
                return 'element-plus-theme'
              }
              if (normalizedId.includes('element-plus/es/locale')) {
                return 'element-plus-locale'
              }
              return 'element-plus-core'
            }
            if (normalizedId.includes('vue') || normalizedId.includes('vue-router') || normalizedId.includes('pinia')) {
              return 'vue-core'
            }
            if (normalizedId.includes('pdfjs-dist')) {
              return 'pdfjs'
            }
            if (normalizedId.includes('motion-v')) {
              return 'motion'
            }
            if (normalizedId.includes('mammoth')) {
              return 'docx-preview'
            }
            if (normalizedId.includes('@tanstack')) {
              return 'tanstack'
            }
            if (normalizedId.includes('axios')) {
              return 'http'
            }
            return 'vendor'
          }
        },
      },
    },
  },
})
