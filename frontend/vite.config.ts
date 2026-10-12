import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// O Front-End fala com o Back-End pelo proxy de desenvolvimento do Vite
// (prefixo /api), evitando configurar CORS no Back-End.
// O alvo do proxy pode ser ajustado pela variavel VITE_API_TARGET (padrao: 8081).
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  const target = env.VITE_API_TARGET || 'http://localhost:8081'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  }
})
