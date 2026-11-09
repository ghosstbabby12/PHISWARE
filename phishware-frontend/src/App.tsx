import { AuthProvider } from '@/context/AuthContext'
import { LanguageProvider } from '@/i18n/LanguageContext'
import { ThemeProvider } from '@/theme/ThemeContext'
import AppRouter from '@/routes/AppRouter'
import AppToaster from '@/components/common/AppToaster'

export default function App() {
  return (
    <LanguageProvider>
      <ThemeProvider>
        <AuthProvider>
          <AppRouter />
          <AppToaster />
        </AuthProvider>
      </ThemeProvider>
    </LanguageProvider>
  )
}
