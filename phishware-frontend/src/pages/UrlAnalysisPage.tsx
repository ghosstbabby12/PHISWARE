import { useEffect, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
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
import { parseQrContent, QrContent, type QrStrings } from '@/utils/qrContent'
import { useLang } from '@/i18n/LanguageContext'

type Tab = 'url' | 'qr'

export default function UrlAnalysisPage() {
  const { t } = useLang()
  const [result, setResult] = useState<UrlAnalysisResponse | null>(null)
  const [searchParams, setSearchParams] = useSearchParams()
  const tabParam: Tab = searchParams.get('tab') === 'qr' ? 'qr' : 'url'
  const [tab, setTab] = useState<Tab>(tabParam)
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
    const dict: QrStrings = {
      network: t.qr.network,
      unnamed: t.qr.unnamed,
      security: t.qr.security,
      openValue: t.qr.openValue,
      recipient: t.qr.recipient,
      message: t.qr.message,
      number: t.qr.number,
      name: t.qr.name,
      coords: t.qr.coords,
      openWifiWarning: t.qr.openWifiWarning,
      shortNumberWarning: t.qr.shortNumberWarning,
      kinds: t.qr.kinds,
    }
    const content = parseQrContent(text, dict)
    setQrContent(content)
    setResult(null)
    setAnalyzedUrl(null)
    // Clasifica de inmediato el enlace del QR (sin abrirlo). Si hay varios,
    // se analiza el primero y el resto queda disponible con su botón.
    if (content.urls.length > 0) analyze(content.urls[0])
  }

  function resetQr() {
    setQrContent(null)
    setResult(null)
    setAnalyzedUrl(null)
  }

  // El FAB y el dashboard enlazan a /analysis?tab=qr
  useEffect(() => {
    setTab(tabParam)
    setResult(null)
    setQrContent(null)
    setAnalyzedUrl(null)
  }, [tabParam])

  function switchTab(next: Tab) {
    setSearchParams(next === 'qr' ? { tab: 'qr' } : {}, { replace: true })
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

  const tips = tab === 'qr' ? t.analysis.qrTips : t.analysis.tips

  return (
    <div className="max-w-3xl mx-auto space-y-6" ref={topRef}>
      <div>
        <h1 className="section-title flex items-center gap-2">
          <span className="grid h-10 w-10 place-items-center rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 text-white shadow-[0_8px_18px_-6px_rgba(121,13,43,0.6)]">
            <ShieldCheck className="w-5 h-5" />
          </span>
          {t.analysis.title}
        </h1>
        <p className="section-subtitle">{t.analysis.subtitle}</p>
      </div>

      <div className="glass relative inline-grid grid-cols-2 rounded-2xl p-1" role="tablist" aria-label={t.analysis.typeLabel}>
        <span
          aria-hidden
          className={`absolute inset-y-1 left-1 w-[calc(50%-0.25rem)] rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-[0_8px_18px_-8px_rgba(121,13,43,0.7)] transition-transform duration-300 ease-out ${tab === 'qr' ? 'translate-x-full' : 'translate-x-0'}`}
        />
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
            className={`relative z-10 flex items-center justify-center gap-2 px-5 py-2 rounded-xl text-sm font-semibold transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-600 ${
              tab === id ? 'text-white' : 'text-ink-500 dark:text-ink-300 hover:text-ink-900 dark:hover:text-white'
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
            <RotateCcw className="w-4 h-4" /> {t.qr.scanAnother}
          </button>
        </>
      ) : (
        <div className="card">
          <QrScanner onDecoded={handleQrDecoded} disabled={isPending} />
        </div>
      )}

      {isPending && (
        <div className="card relative overflow-hidden text-sm text-ink-700 dark:text-ink-200" role="status" aria-live="polite">
          <span className="absolute inset-x-0 top-0 h-1 bg-[linear-gradient(90deg,transparent,#B42345,transparent)] bg-[length:200%_100%] animate-shine" aria-hidden />
          <span className="flex items-center gap-3">
            <span className="relative grid h-9 w-9 place-items-center">
              <span className="absolute inset-0 rounded-full bg-primary-500/30 animate-ring-pulse" />
              <ShieldCheck className="relative h-5 w-5 text-primary-700 dark:text-primary-300" />
            </span>
            <span className="break-all">{t.analysis.analyzingUrl(analyzedUrl ?? '…')}</span>
          </span>
        </div>
      )}

      {result && <AnalysisResult result={result} onAnalyzeAnother={analyzeAnother} />}

      <div className="card !bg-primary-50/50 dark:!bg-primary-900/20">
        <h3 className="text-sm font-semibold text-ink-900 dark:text-ink-50 flex items-center gap-2 mb-3">
          <Info className="w-4 h-4 text-primary-700" />
          {t.analysis.tipsTitle}
        </h3>
        <ul className="space-y-2">
          {tips.map((tip, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-ink-700 dark:text-ink-200">
              <span className="mt-[7px] w-1.5 h-1.5 rounded-full bg-primary-600 shrink-0" aria-hidden />
              {tip}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
