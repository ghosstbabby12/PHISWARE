import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, Flag, ExternalLink, Clock, Send } from 'lucide-react'
import toast from 'react-hot-toast'
import { reportService } from '@/services/reportService'
import type { ReportType, ReportSeverity, ReportResponse } from '@/types/report.types'

const reportTypes: { value: ReportType; label: string; color: string }[] = [
  { value: 'PHISHING',   label: 'Phishing',         color: 'bg-danger-500/20 text-danger-400 border-danger-500/30' },
  { value: 'MALWARE',    label: 'Malware',           color: 'bg-danger-500/20 text-danger-400 border-danger-500/30' },
  { value: 'SUSPICIOUS', label: 'Sitio Sospechoso',  color: 'bg-warning-500/20 text-warning-400 border-warning-500/30' },
  { value: 'SCAM',       label: 'Estafa',            color: 'bg-warning-500/20 text-warning-400 border-warning-500/30' },
  { value: 'SPAM',       label: 'Spam',              color: 'bg-slate-500/20 text-slate-400 border-slate-500/30' },
]

const severities: { value: ReportSeverity; label: string; color: string }[] = [
  { value: 'LOW',      label: 'Bajo',   color: 'bg-success-500/20 text-success-400' },
  { value: 'MEDIUM',   label: 'Medio',  color: 'bg-warning-500/20 text-warning-400' },
  { value: 'HIGH',     label: 'Alto',   color: 'bg-danger-500/20 text-danger-400' },
  { value: 'CRITICAL', label: 'Critico', color: 'bg-danger-600/30 text-danger-300' },
]

const statusLabels: Record<string, { label: string; color: string }> = {
  PENDING:   { label: 'Pendiente',   color: 'bg-warning-500/20 text-warning-400' },
  REVIEWED:  { label: 'Revisado',    color: 'bg-primary-500/20 text-primary-400' },
  CONFIRMED: { label: 'Confirmado',  color: 'bg-danger-500/20 text-danger-400' },
  REJECTED:  { label: 'Rechazado',   color: 'bg-slate-500/20 text-slate-400' },
}

export default function ReportPage() {
  const queryClient = useQueryClient()
  const [url, setUrl] = useState('')
  const [type, setType] = useState<ReportType>('PHISHING')
  const [severity, setSeverity] = useState<ReportSeverity>('MEDIUM')
  const [description, setDescription] = useState('')

  const createMutation = useMutation({
    mutationFn: reportService.createReport,
    onSuccess: () => {
      toast.success('Reporte enviado correctamente (+10 pts)')
      setUrl('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['my-reports'] })
    },
    onError: () => toast.error('Error al enviar el reporte'),
  })

  const { data: myReports } = useQuery({
    queryKey: ['my-reports'],
    queryFn: () => reportService.getMyReports(0, 5),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!url.trim()) return
    createMutation.mutate({ reportedUrl: url, reportType: type, severity, description: description || undefined })
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fade-in">
      <div className="flex items-center gap-3">
        <div className="p-2 bg-warning-500/20 rounded-lg">
          <Flag className="w-6 h-6 text-warning-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Reportes Comunitarios</h1>
          <p className="text-slate-400 text-sm">Ayuda a la comunidad reportando URLs sospechosas (+10 pts por reporte)</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="card space-y-5">
        <h2 className="font-semibold text-white flex items-center gap-2">
          <AlertTriangle className="w-5 h-5 text-warning-400" />
          Reportar URL Sospechosa
        </h2>

        <div>
          <label className="block text-sm text-slate-400 mb-1.5">URL sospechosa</label>
          <div className="flex gap-2">
            <input
              type="text"
              value={url}
              onChange={e => setUrl(e.target.value)}
              placeholder="https://ejemplo-sospechoso.com/login"
              className="input-field flex-1"
              required
            />
            {url && (
              <a href={url} target="_blank" rel="noopener noreferrer" className="btn-ghost p-2.5">
                <ExternalLink className="w-5 h-5" />
              </a>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm text-slate-400 mb-1.5">Tipo de amenaza</label>
            <div className="flex flex-wrap gap-2">
              {reportTypes.map(rt => (
                <button
                  key={rt.value}
                  type="button"
                  onClick={() => setType(rt.value)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                    type === rt.value ? rt.color : 'border-slate-700 text-slate-500 hover:text-slate-300'
                  }`}
                >
                  {rt.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm text-slate-400 mb-1.5">Severidad</label>
            <div className="flex flex-wrap gap-2">
              {severities.map(s => (
                <button
                  key={s.value}
                  type="button"
                  onClick={() => setSeverity(s.value)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    severity === s.value ? s.color : 'text-slate-500 hover:text-slate-300 bg-slate-800'
                  }`}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div>
          <label className="block text-sm text-slate-400 mb-1.5">Descripcion (opcional)</label>
          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            placeholder="Describe por que crees que esta URL es sospechosa..."
            className="input-field min-h-[80px] resize-y"
            maxLength={1000}
          />
        </div>

        <button
          type="submit"
          disabled={createMutation.isPending || !url.trim()}
          className="btn-primary flex items-center gap-2"
        >
          <Send className="w-4 h-4" />
          {createMutation.isPending ? 'Enviando...' : 'Enviar Reporte'}
        </button>
      </form>

      {myReports?.content && myReports.content.length > 0 && (
        <div className="card space-y-4">
          <h2 className="font-semibold text-white flex items-center gap-2">
            <Clock className="w-5 h-5 text-primary-400" />
            Mis Reportes Recientes
          </h2>
          <div className="space-y-3">
            {myReports.content.map((report: ReportResponse) => (
              <div key={report.id} className="bg-dark-900 rounded-lg p-4 border border-slate-700/50">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-white font-mono truncate">{report.reportedUrl}</p>
                    <p className="text-xs text-slate-500 mt-1">
                      {report.description || 'Sin descripcion'}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                      reportTypes.find(t => t.value === report.reportType)?.color ?? ''
                    }`}>
                      {reportTypes.find(t => t.value === report.reportType)?.label}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                      statusLabels[report.status]?.color ?? ''
                    }`}>
                      {statusLabels[report.status]?.label}
                    </span>
                  </div>
                </div>
                <p className="text-xs text-slate-600 mt-2">
                  {new Date(report.createdAt).toLocaleDateString('es', { day: '2-digit', month: 'short', year: 'numeric' })}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
