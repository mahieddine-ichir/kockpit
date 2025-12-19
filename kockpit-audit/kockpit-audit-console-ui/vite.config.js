import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
  },
  define: {
    __BUILD_TIME__: JSON.stringify(process.env.VITE_BUILD_TIME || new Date().toISOString()),
  },
})
