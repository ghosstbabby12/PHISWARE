import { Link } from 'react-router-dom'
import { Search, ShieldCheck, BellRing, ArrowRight, CheckCircle } from 'lucide-react'
import Logo from '@/components/common/Logo'
import LanguageToggle from '@/components/common/LanguageToggle'
import { useLang } from '@/i18n/LanguageContext'

export default function LandingPage() {
  const { t } = useLang()

  const BENEFITS = [
    { icon: Search, title: t.landing.b1t, desc: t.landing.b1d },
    { icon: ShieldCheck, title: t.landing.b2t, desc: t.landing.b2d },
    { icon: BellRing, title: t.landing.b3t, desc: t.landing.b3d },
  ]
  const TRUST_POINTS = [t.landing.t1, t.landing.t2, t.landing.t3]

  return (
    <div className="min-h-screen bg-ink-50">
      <header className="border-b divider bg-white/85 backdrop-blur sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <Logo size={32} />
          <div className="flex items-center gap-2">
            <LanguageToggle compact />
            <Link to="/login" className="btn-ghost hidden sm:inline-flex">{t.landing.login}</Link>
            <Link to="/register" className="btn-primary !px-5 !py-2">{t.landing.start}</Link>
          </div>
        </div>
      </header>

      <main>
        <section className="max-w-6xl mx-auto px-6 pt-16 pb-14 lg:pt-24 lg:pb-20 grid gap-10 lg:grid-cols-2 lg:items-center">
          <div>
            <p className="badge-primary mb-5">{t.landing.badge}</p>
            <h1 className="text-4xl lg:text-5xl font-bold text-ink-900 tracking-tight leading-[1.1]">
              {t.landing.title}
            </h1>
            <p className="text-lg text-ink-500 mt-4 max-w-lg">
              {t.landing.subtitle}
            </p>
            <div className="flex flex-wrap items-center gap-3 mt-8">
              <Link to="/register" className="btn-primary text-base !px-7 !py-3">
                {t.landing.start} <ArrowRight className="w-4 h-4" />
              </Link>
              <Link to="/login" className="btn-secondary text-base !px-7 !py-3">
                {t.landing.login}
              </Link>
            </div>
            <ul className="mt-8 space-y-2.5">
              {TRUST_POINTS.map((point) => (
                <li key={point} className="flex items-start gap-2.5 text-sm text-ink-700">
                  <CheckCircle className="w-4 h-4 text-success-500 mt-0.5 shrink-0" />
                  {point}
                </li>
              ))}
            </ul>
          </div>

          <div className="card !p-0 overflow-hidden" aria-label={t.landing.preview}>
            <div className="bg-primary-800 px-6 py-5">
              <p className="text-white/70 text-xs font-semibold uppercase tracking-wider">{t.landing.preview}</p>
              <p className="text-white font-bold text-lg mt-1 flex items-center gap-2">
                <ShieldCheck className="w-5 h-5" /> {t.landing.preview}
              </p>
            </div>
            <div className="p-6 space-y-3">
              <div className="rounded-xl border divider bg-ink-50 px-4 py-3">
                <p className="text-xs text-ink-500 font-medium">{t.landing.previewLink}</p>
                <p className="text-sm font-mono text-ink-900 break-all">https://tu-banco-seguro.example/ingreso</p>
              </div>
              <div className="grid grid-cols-3 gap-3">
                {[
                  { k: t.landing.previewSafe, v: '✓', c: 'text-success-600' },
                  { k: t.landing.previewRisk, v: '12 / 100', c: 'text-ink-900' },
                  { k: t.landing.previewSources, v: '3 de 3', c: 'text-ink-900' },
                ].map((s) => (
                  <div key={s.k} className="rounded-xl border divider px-3 py-2.5 text-center">
                    <p className="text-[11px] text-ink-500 font-medium">{s.k}</p>
                    <p className={`text-sm font-bold ${s.c}`}>{s.v}</p>
                  </div>
                ))}
              </div>
              <p className="text-xs text-ink-500">
                {t.landing.previewNote}
              </p>
            </div>
          </div>
        </section>

        <section className="max-w-6xl mx-auto px-6 pb-20">
          <h2 className="text-2xl font-bold text-ink-900 tracking-tight text-center">
            {t.landing.featuresTitle}
          </h2>
          <p className="text-ink-500 text-center mt-2 max-w-xl mx-auto">
            {t.landing.featuresSub}
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mt-10">
            {BENEFITS.map(({ icon: Icon, title, desc }) => (
              <div key={title} className="card-hover">
                <div className="p-2.5 bg-primary-100 rounded-xl w-fit mb-4 border border-primary-600/10">
                  <Icon className="w-5 h-5 text-primary-700" />
                </div>
                <h3 className="font-semibold text-ink-900 mb-1.5">{title}</h3>
                <p className="text-sm text-ink-500 leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </section>
      </main>

      <footer className="border-t divider bg-white py-8 text-center text-ink-500 text-sm">
        <div className="flex items-center justify-center mb-2">
          <Logo size={26} />
        </div>
        <p>{t.landing.footer}</p>
      </footer>
    </div>
  )
}
