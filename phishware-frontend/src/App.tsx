import { AuthProvider } from '@/context/AuthContext'
import { LanguageProvider } from '@/i18n/LanguageContext'
import AppRouter from '@/routes/AppRouter'

export default function App() {
  return (
    <LanguageProvider>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </LanguageProvider>
  )
}
