import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Shield, Info, Link as LinkIcon, QrCode, RotateCcw } from 'lucide-react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import { getErrorMessage } from '@/services/api'
import { UrlAnalysisResponse } from '@/types/analysis.types'
import UrlInputForm from '@/components/url-analysis/UrlInputForm'
import AnalysisResult from '@/components/url-analysis/AnalysisResult'
import QrScanner from '@/components/url-analysis/QrScanner'
import QrContentCard from '@/components/url-analysis/QrContentCard'
import { parseQrContent, QrContent } from '@/utils/qrContent'

const TIPS = [
  'Verifica que la URL use HTTPS y el dominio sea correcto.',
  'Busca errores ortográficos en el dominio (paypa1.com, amaz0n.com).',
  'Los sitios legítimos nunca te piden contraseñas por correo o SMS.',
  'URLs muy largas o con parámetros extraños son señal de alerta.',
  'Usa esta herramienta antes de ingresar datos en un sitio desconocido.',
]

const QR_TIPS = [
  'Revisa que el QR no sea una calcomanía pegada encima del original (en parqueaderos, restaurantes o cajeros).',
  'Desconfía de QR que llegan por correo o carta pidiendo pagar, verificar tu cuenta o reclamar un premio.',
  'Un QR puede llevarte a cualquier sitio: analiza el enlace antes de abrirlo o ingresar datos.',
  'Nunca escanees un QR para iniciar sesión si no lo generaste tú desde el sitio oficial.',
]

type Tab = 'url' | 'qr'

export default function UrlAnalysisPage() {
  const [result, setResult] = useState<UrlAnalysisResponse | null>(null)
  const [tab, setTab] = useState<Tab>('url')
  const [qrContent, setQrContent] = useState<QrContent | null>(null)
  const [analyzedUrl, setAnalyzedUrl] = useState<string | null>(null)
  const queryClient = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: urlAnalysisService.analyze,
    onSuccess: (data) => {
      setResult(data)
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      if (data.riskLevel === 'DANGEROUS') {
        toast.error('¡Alerta! Sitio peligroso detectado')
      } else if (data.riskLevel === 'SUSPICIOUS') {
        toast('⚠️ Sitio sospechoso detectado', { icon: '⚠️' })
      } else {
        toast.success('URL analizada — Sin amenazas detectadas')
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
    if (content.urls.length > 0) {
      toast('QR detectado. Analizando el enlace...', { icon: '🔍' })
      analyze(content.urls[0])
    } else {
      toast('QR leído: no contiene enlaces', { icon: '📷' })
    }
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

  const tips = tab === 'qr' ? QR_TIPS : TIPS

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Shield className="w-6 h-6 text-primary-400" />
          Analizar URL
        </h1>
        <p className="text-slate-400 mt-1">
          Ingresa un enlace o escanea un código QR para detectar phishing, malware y amenazas en tiempo real
        </p>
      </div>

      <div className="inline-flex rounded-lg border border-slate-700 bg-dark-800 p-1" role="tablist" aria-label="Tipo de análisis">
        {([
          { id: 'url', label: 'Enlace', icon: LinkIcon },
          { id: 'qr',  label: 'Código QR', icon: QrCode },
        ] as const).map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            role="tab"
            aria-selected={tab === id}
            onClick={() => switchTab(id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
              tab === id ? 'bg-primary-600 text-white' : 'text-slate-400 hover:text-white'
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
            <RotateCcw className="w-4 h-4" /> Escanear otro QR
          </button>
        </>
      ) : (
        <div className="card">
          <QrScanner onDecoded={handleQrDecoded} disabled={isPending} />
        </div>
      )}

      {isPending && tab === 'qr' && (
        <div className="card animate-pulse text-sm text-slate-400">Analizando {analyzedUrl}...</div>
      )}

      {result && <AnalysisResult result={result} />}

      {/* Tips */}
      <div className="card border-primary-500/20">
        <h3 className="text-sm font-semibold text-slate-300 flex items-center gap-2 mb-3">
          <Info className="w-4 h-4 text-primary-400" />
          Consejos de Seguridad
        </h3>
        <ul className="space-y-2">
          {tips.map((tip, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-slate-400">
              <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-primary-500 flex-shrink-0" />
              {tip}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
