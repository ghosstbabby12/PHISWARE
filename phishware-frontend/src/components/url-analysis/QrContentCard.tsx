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

export default function QrContentCard({ content, analyzedUrl, onAnalyze, isAnalyzing }: Props) {
  const Icon = KIND_ICONS[content.kind]

  return (
    <div className="card space-y-4">
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-lg bg-primary-600/20">
          <Icon className="w-5 h-5 text-primary-400" />
        </div>
        <div>
          <p className="text-xs text-slate-500">Contenido del código QR</p>
          <p className="text-white font-semibold">{QR_KIND_LABELS[content.kind]}</p>
        </div>
      </div>

      {content.details.length > 0 && (
        <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
          {content.details.map(d => (
            <div key={d.label} className="contents">
              <dt className="text-slate-500">{d.label}</dt>
              <dd className="text-slate-200 break-all">{d.value || '—'}</dd>
            </div>
          ))}
        </dl>
      )}

      {content.kind !== 'WIFI' && (
        <pre className="text-xs text-slate-300 bg-dark-900/70 border border-slate-700/60 rounded-lg p-3 whitespace-pre-wrap break-all max-h-40 overflow-auto">
          {content.raw}
        </pre>
      )}

      {content.warnings.map(w => (
        <p key={w} className="flex items-start gap-2 text-sm text-warning-400">
          <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5" /> {w}
        </p>
      ))}

      {content.urls.length === 0 ? (
        <p className="text-sm text-slate-400">
          Este QR no contiene ningún enlace, así que no hay nada que analizar.
        </p>
      ) : content.urls.length > 1 && (
        <div className="space-y-2">
          <p className="text-sm text-slate-400">El QR contiene {content.urls.length} enlaces. Analiza cada uno:</p>
          {content.urls.map(url => (
            <div key={url} className="flex items-center gap-2">
              <span className="flex-1 text-sm text-slate-300 truncate" title={url}>{url}</span>
              <button
                type="button"
                onClick={() => onAnalyze(url)}
                disabled={isAnalyzing || url === analyzedUrl}
                className="btn-ghost text-xs inline-flex items-center gap-1 disabled:opacity-50"
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
