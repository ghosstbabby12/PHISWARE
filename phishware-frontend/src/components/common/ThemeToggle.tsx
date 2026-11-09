import { Moon, Sun } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'
import { useTheme } from '@/theme/ThemeContext'

export default function ThemeToggle({ compact = false }: { compact?: boolean }) {
  const { t } = useLang()
  const { resolved, toggle } = useTheme()
  const label = resolved === 'dark' ? t.settings.themeLight : t.settings.themeDark
  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={label}
      title={label}
      className="p-2.5 rounded-xl text-ink-500 hover:text-primary-700 hover:bg-primary-100 dark:text-ink-300 dark:hover:text-white dark:hover:bg-dark-800 transition-colors"
    >
      {resolved === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
      {!compact && <span className="ml-2 text-sm font-medium">{label}</span>}
    </button>
  )
}
