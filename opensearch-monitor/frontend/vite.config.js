import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // Relative base: the built index.html references assets as "./assets/..."
  // instead of "/assets/...", so the same build works whether the app is
  // served at the domain root or mounted under a path prefix by a reverse
  // proxy/ALB (which forwards the full original path unchanged). Requires the
  // page to be loaded with a trailing slash (e.g. "/opensearch-monitor/"),
  // which the backend enforces via a redirect — see BASE_PATH in backend/src/index.js.
  base: './',
  server: {
    proxy: {
      '/api': 'http://localhost:3000',
    },
  },
});
