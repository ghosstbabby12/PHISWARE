import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Globe, LogOut, ShieldCheck, User } from 'lucide-react'
import AppHeader from '@/components/common/AppHeader'
import ConfirmationDialog from '@/components/common/ConfirmationDialog'
import { useLang } from '@/i18n/LanguageContext'
import { useAuth } from '@/context/AuthContext'
import { Lang } from '@/i18n/translations'

export default function SettingsPage() {
  const { t, lang, setLang } = useLang()
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [confirmLogout, setConfirmLogout] = useState(false)

  function doLogout() {
    logout()
    setConfirmLogout(false)
    navigate('/login', { replace: true })
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-fade-in">
      <AppHeader title={t.settings.title} subtitle={t.settings.sub} />

      <section className="card space-y-3" aria-labelledby="lang-heading">
        <h2 id="lang-heading" className="font-semibold text-ink-900 flex items-center gap-2">
          <Globe className="w-5 h-5 text-primary-700" aria-hidden />
          {t.settings.langTitle}
        </h2>
        <p className="text-sm text-ink-500">{t.settings.langSub}</p>
        <div className="grid grid-cols-2 gap-2" role="group" aria-label={t.settings.langTitle}>
          {(['es', 'en'] as Lang[]).map(l => (
            <button
              key={l}
              type="button"
              onClick={() => setLang(l)}
              aria-pressed={lang === l}
              className={`px-4 py-2.5 rounded-xl text-sm font-semibold border transition-all ${
                lang === l
                  ? 'bg-primary-600 text-white border-primary-600 shadow-sm'
                  : 'bg-white text-ink-700 border-ink-200 hover:border-primary-600'
              }`}
            >
              {l === 'es' ? t.settings.langEs : t.settings.langEn}
            </button>
          ))}
        </div>
      </section>

      <section className="card space-y-3" aria-labelledby="account-heading">
        <h2 id="account-heading" className="font-semibold text-ink-900 flex items-center gap-2">
          <User className="w-5 h-5 text-primary-700" aria-hidden />
          {t.settings.account}
        </h2>
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-full bg-primary-600 flex items-center justify-center text-lg font-bold text-white shrink-0" aria-hidden>
            {user?.username?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div className="min-w-0">
            <p className="font-semibold text-ink-900 truncate">{user?.username}</p>
            <p className="text-sm text-ink-500 truncate">{user?.email}</p>
          </div>
        </div>
        <p className="text-xs text-ink-500 flex items-center gap-1.5">
          <ShieldCheck className="w-3.5 h-3.5 text-success-500" aria-hidden />
          {t.auth.sessionNote}
        </p>
      </section>

      <section className="card space-y-3" aria-labelledby="session-heading">
        <h2 id="session-heading" className="font-semibold text-ink-900">
          {t.settings.session}
        </h2>
        <p className="text-sm text-ink-500">{t.settings.logoutSub}</p>
        <button type="button" onClick={() => setConfirmLogout(true)} className="btn-secondary text-danger-600 !border-danger-500/30 text-sm">
          <LogOut className="w-4 h-4" aria-hidden />
          {t.settings.logoutBtn}
        </button>
      </section>

      <section className="card" aria-label={t.settings.about}>
        <h2 className="font-semibold text-ink-900 mb-1">{t.settings.about}</h2>
        <p className="text-sm text-ink-500 leading-relaxed">{t.settings.aboutText}</p>
      </section>

      <ConfirmationDialog
        open={confirmLogout}
        title={t.settings.logoutConfirm}
        description={t.settings.logoutConfirmSub}
        confirmLabel={t.settings.logoutBtn}
        cancelLabel={t.common.cancel}
        onConfirm={doLogout}
        onCancel={() => setConfirmLogout(false)}
      />
    </div>
  )
}
