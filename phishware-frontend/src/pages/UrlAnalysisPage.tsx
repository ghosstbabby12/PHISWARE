import { useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Info, Link as LinkIcon, QrCode, RotateCcw, ShieldCheck } from 'lucide-react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import { getErrorMessage } from '@/services/api'
import { UrlAnalysisResponse } from '@/types/analysis.types'
import UrlInputForm from '@/components/url-analysis/UrlInputForm'
import AnalysisResult from '@/components/url-analysis/AnalysisResult'
import QrScanner from '@/components/url-analysis/QrScanner'
import QrContentCard from '@/components/url-analysis/QrContentCard'
import { parseQrContent, QrContent } from '@/utils/qrContent'
import { useLang } from '@/i18n/LanguageContext'

const TIPS = {
  es: [
    'Verifica que la URL use HTTPS y el dominio sea correcto.',
    'Busca errores ortográficos en el dominio (paypa1.com, amaz0n.com).',
    'Los sitios legítimos nunca te piden contraseñas por correo o SMS.',
    'URLs muy largas o con parámetros extraños son señal de alerta.',
    'Usa esta herramienta antes de ingresar datos en un sitio desconocido.',
  ],
  en: [
    'Check that the URL uses HTTPS and the domain is correct.',
    'Look for typos in the domain (paypa1.com, amaz0n.com).',
    'Legitimate sites never ask for passwords by email or SMS.',
    'Very long URLs or odd parameters are a warning sign.',
    'Use this tool before entering data on an unfamiliar site.',
  ],
}

const QR_TIPS = {
  es: [
    'Revisa que el QR no sea una calcomanía pegada encima del original (en parqueaderos, restaurantes o cajeros).',
    'Desconfía de QR que llegan por correo o carta pidiendo pagar, verificar tu cuenta o reclamar un premio.',
    'Un QR puede llevarte a cualquier sitio: analiza el enlace antes de abrirlo o ingresar datos.',
    'Nunca escanees un QR para iniciar sesión si no lo generaste tú desde el sitio oficial.',
  ],
  en: [
    'Check the QR is not a sticker placed over the original (parking lots, restaurants or ATMs).',
    'Distrust QR codes arriving by mail or letter asking you to pay, verify your account or claim a prize.',
    'A QR can take you anywhere: analyze the link before opening it or entering data.',
    'Never scan a QR to log in unless you generated it yourself from the official site.',
  ],
}

type Tab = 'url' | 'qr'

export default function UrlAnalysisPage() {
  const { t, lang } = useLang()
  const [result, setResult] = useState<UrlAnalysisResponse | null>(null)
  const [tab, setTab] = useState<Tab>('url')
  const [qrContent, setQrContent] = useState<QrContent | null>(null)
  const [analyzedUrl, setAnalyzedUrl] = useState<string | null>(null)
  const queryClient = useQueryClient()
  const topRef = useRef<HTMLDivElement>(null)

  const { mutate, isPending } = useMutation({
    mutationFn: urlAnalysisService.analyze,
    onSuccess: (data) => {
      setResult(data)
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      if (data.riskLevel === 'DANGEROUS') {
        toast.error(data.riskMessage)
      } else if (data.riskLevel === 'SUSPICIOUS') {
        toast(data.riskMessage, { icon: '⚠️' })
      } else {
        toast.success(data.riskMessage)
      }
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  function analyze(url: string) {
    setAnalyzedUrl(url)
    mutate({ url })
  }

  function handleQrDecoded(text: string) {
    const content = parseQrContent(text)
    setQrContent(content)
    setResult(null)
    setAnalyzedUrl(null)
  }

  function resetQr() {
    setQrContent(null)
    setResult(null)
    setAnalyzedUrl(null)
  }

  function switchTab(next: Tab) {
    setTab(next)
    setResult(null)
    setQrContent(null)
    setAnalyzedUrl(null)
  }

  function analyzeAnother() {
    setResult(null)
    setQrContent(null)
    setAnalyzedUrl(null)
    topRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    document.getElementById('url-input')?.focus({ preventScroll: true })
  }

  const tips = (tab === 'qr' ? QR_TIPS : TIPS)[lang]

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in" ref={topRef}>
      <div>
        <h1 className="section-title flex items-center gap-2">
          <ShieldCheck className="w-6 h-6 text-primary-700" />
          {t.analysis.title}
        </h1>
        <p className="section-subtitle">{t.analysis.subtitle}</p>
      </div>

      <div className="inline-flex rounded-xl border divider bg-white p-1 shadow-sm" role="tablist" aria-label={t.analysis.typeLabel}>
        {([
          { id: 'url', label: t.analysis.urlTab, icon: LinkIcon },
          { id: 'qr',  label: t.analysis.qrTab, icon: QrCode },
        ] as const).map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            role="tab"
            aria-selected={tab === id}
            onClick={() => switchTab(id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              tab === id ? 'bg-primary-600 text-white shadow-sm' : 'text-ink-500 hover:text-ink-900 hover:bg-ink-50'
            }`}
          >
            <Icon className="w-4 h-4" /> {label}
          </button>
        ))}
      </div>

      {tab === 'url' ? (
        <div className="card">
          <UrlInputForm
            onSubmit={analyze}
            isLoading={isPending}
          />
        </div>
      ) : qrContent ? (
        <>
          <QrContentCard
            content={qrContent}
            analyzedUrl={analyzedUrl}
            onAnalyze={analyze}
            isAnalyzing={isPending}
          />
          <button type="button" onClick={resetQr} className="btn-secondary inline-flex items-center gap-2">
            <RotateCcw className="w-4 h-4" /> {lang === 'es' ? 'Escanear otro QR' : 'Scan another QR'}
          </button>
        </>
      ) : (
        <div className="card">
          <QrScanner onDecoded={handleQrDecoded} disabled={isPending} />
        </div>
      )}

      {isPending && (
        <div className="card animate-pulse text-sm text-ink-500" role="status" aria-live="polite">
          {t.analysis.analyzingUrl(analyzedUrl ?? '…')}
        </div>
      )}

      {result && <AnalysisResult result={result} onAnalyzeAnother={analyzeAnother} />}

      <div className="card !bg-primary-50/60 !border-primary-600/15">
        <h3 className="text-sm font-semibold text-ink-900 flex items-center gap-2 mb-3">
          <Info className="w-4 h-4 text-primary-700" />
          {t.analysis.tipsTitle}
        </h3>
        <ul className="space-y-2">
          {tips.map((tip, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-ink-700">
              <span className="mt-[7px] w-1.5 h-1.5 rounded-full bg-primary-600 shrink-0" aria-hidden />
              {tip}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
