import { AlertTriangle, Mail, MapPin, MessageSquare, Phone, QrCode, Search, Type, User, Wifi, Link as LinkIcon } from 'lucide-react'
import { QR_KIND_LABELS, QrContent, QrKind } from '@/utils/qrContent'

const KIND_ICONS: Record<QrKind, typeof QrCode> = {
  URL: LinkIcon, WIFI: Wifi, EMAIL: Mail, PHONE: Phone, SMS: MessageSquare,
  CONTACT: User, LOCATION: MapPin, TEXT: Type,
}

interface Props {
  content: QrContent
  analyzedUrl: string | null
  onAnalyze: (url: string) => void
  isAnalyzing: boolean
}

/** Contenido decodificado del QR. Nunca abre el destino: solo muestra y ofrece analizar. */
export default function QrContentCard({ content, analyzedUrl, onAnalyze, isAnalyzing }: Props) {
  const Icon = KIND_ICONS[content.kind]

  return (
    <div className="card space-y-4">
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-xl bg-primary-100 border border-primary-600/10">
          <Icon className="w-5 h-5 text-primary-700" />
        </div>
        <div>
          <p className="text-xs text-ink-500 font-medium">Contenido del código QR</p>
          <p className="text-ink-900 font-semibold">{QR_KIND_LABELS[content.kind]}</p>
        </div>
      </div>

      {content.details.length > 0 && (
        <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
          {content.details.map(d => (
            <div key={d.label} className="contents">
              <dt className="text-ink-500">{d.label}</dt>
              <dd className="text-ink-900 break-all font-mono text-[13px]">{d.value || '—'}</dd>
            </div>
          ))}
        </dl>
      )}

      {content.kind !== 'WIFI' && (
        <pre className="text-xs text-ink-700 bg-ink-50 border divider rounded-xl p-3 whitespace-pre-wrap break-all max-h-40 overflow-auto">
          {content.raw}
        </pre>
      )}

      {content.warnings.map(w => (
        <p key={w} className="flex items-start gap-2 text-sm text-warning-600 font-medium">
          <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" aria-hidden /> {w}
        </p>
      ))}

      {content.urls.length === 0 ? (
        <p className="text-sm text-ink-500">
          Este QR no contiene ningún enlace, así que no hay nada que analizar.
        </p>
      ) : content.urls.length > 1 && (
        <div className="space-y-2">
          <p className="text-sm text-ink-500">El QR contiene {content.urls.length} enlaces. Analiza cada uno sin abrirlos:</p>
          {content.urls.map(url => (
            <div key={url} className="flex items-center gap-2 rounded-xl border divider px-3 py-2">
              <span className="flex-1 text-sm text-ink-900 font-mono truncate" title={url}>{url}</span>
              <button
                type="button"
                onClick={() => onAnalyze(url)}
                disabled={isAnalyzing || url === analyzedUrl}
                className="btn-ghost text-xs inline-flex items-center gap-1 disabled:opacity-50 !text-primary-700"
              >
                <Search className="w-3.5 h-3.5" />
                {url === analyzedUrl ? 'Analizado' : 'Analizar'}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
