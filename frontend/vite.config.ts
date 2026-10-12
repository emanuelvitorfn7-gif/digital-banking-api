import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// O Front-End fala com o Back-End atraves do proxy de desenvolvimento do Vite
// (prefixo /api), evitando a necessidade de configurar CORS no Back-End.
// O alvo do proxy pode ser ajustado por VITE_API_TARGET (padrao: :8081).
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_API_TARGET || 'http://localhost:8081'

  return {
    plugins: [react(), tailwindcss()],
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
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/test/setup.ts'],
      environmentOptions: {
        jsdom: { url: 'http://localhost:5173' },
      },
      css: false,
    },
  }
})
