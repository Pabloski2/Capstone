import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// Política de seguridad de contenido (CSP): solo se agrega al compilar (producción y app móvil),
// porque en desarrollo Vite necesita scripts en línea para recargar la página.
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  const api = env.VITE_API_URL ? new URL(env.VITE_API_URL).origin : ''

  return {
    plugins: [
      react(),
      {
        name: 'csp-produccion',
        transformIndexHtml(html) {
          if (command !== 'build') return html
          const csp = [
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' data: https://fonts.gstatic.com",
            "img-src 'self' data: blob:",
            `connect-src 'self' ${api}`,
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
          ].join('; ')
          return html.replace(
            '<head>',
            `<head>\n    <meta http-equiv="Content-Security-Policy" content="${csp}" />`
          )
        },
      },
    ],
    server: {
      port: 5173,
    },
  }
})
