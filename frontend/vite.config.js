import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In production, set VITE_API_BASE_URL=https://your-api-domain.com in your .env file.
// In dev, the proxy below forwards /api → localhost:8080, so VITE_API_BASE_URL can be empty.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
