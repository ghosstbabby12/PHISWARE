import { Toaster } from 'react-hot-toast'
import { useTheme } from '@/theme/ThemeContext'

/**
 * Toaster único de la app (antes había dos: main.tsx y AppLayout con estilos
 * contradictorios). Lee el tema resuelto para usar la superficie correcta.
 */
export default function AppToaster() {
  const { resolved } = useTheme()
  const dark = resolved === 'dark'

  return (
    <Toaster
      position="top-right"
      toastOptions={{
        duration: 4200,
        style: {
          background: dark ? '#0f172a' : '#ffffff',
          color: dark ? '#f1f5f9' : '#17171c',
          border: dark ? '1px solid #1e293b' : '1px solid #e7e4e8',
          borderRadius: '12px',
          fontSize: '14px',
        },
        success: { iconTheme: { primary: '#16845b', secondary: '#ffffff' } },
        error: { iconTheme: { primary: '#c6283d', secondary: '#ffffff' } },
      }}
    />
  )
}
