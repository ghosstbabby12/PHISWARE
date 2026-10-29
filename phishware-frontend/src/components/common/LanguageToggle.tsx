import { Languages } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'

export default function LanguageToggle({ compact = false }: { compact?: boolean }) {
  const { lang, toggle } = useLang()
  return (
    <button
      type="button"
      onClick={toggle}
      className={compact ? 'btn-ghost !px-3 !py-2 text-sm font-semibold' : 'btn-ghost text-sm font-semibold'}
      aria-label={lang === 'es' ? 'Cambiar a English' : 'Switch to Español'}
      title={lang === 'es' ? 'Cambiar a English' : 'Switch to Español'}
    >
      <Languages className="w-4 h-4" aria-hidden />
      {lang === 'es' ? 'EN' : 'ES'}
    </button>
  )
}
