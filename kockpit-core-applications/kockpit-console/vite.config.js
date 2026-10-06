import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:9080',
        changeOrigin: true,
        secure: false,
        // Local dev has no Cognito login: when the backend requires one (Cognito issuer
        // configured), pass a real ID token copied from the deployed console:
        //   KOCKPIT_ID_TOKEN=eyJ... npm run dev
        headers: process.env.KOCKPIT_ID_TOKEN
          ? { Authorization: `Bearer ${process.env.KOCKPIT_ID_TOKEN}` }
          : {},
      }
    }
  },
  define: {
    __BUILD_TIME__: JSON.stringify(process.env.VITE_BUILD_TIME || new Date().toISOString()),
  },
})
