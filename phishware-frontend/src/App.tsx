import { AuthProvider } from '@/context/AuthContext'
import { LanguageProvider } from '@/i18n/LanguageContext'
import { ThemeProvider } from '@/theme/ThemeContext'
import AppRouter from '@/routes/AppRouter'

export default function App() {
  return (
    <LanguageProvider>
      <ThemeProvider>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </ThemeProvider>
    </LanguageProvider>
  )
}
