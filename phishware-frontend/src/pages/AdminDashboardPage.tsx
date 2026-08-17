import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, Check, Flag, ShieldCheck, X } from 'lucide-react'
import { reportService } from '@/services/reportService'
import { ReportResponse, ReportSeverity } from '@/types/report.types'

const severityStyles: Record<ReportSeverity, string> = {
  LOW: 'text-slate-300 bg-slate-700/50',
  MEDIUM: 'text-warning-300 bg-warning-500/15',
  HIGH: 'text-orange-300 bg-orange-500/15',
  CRITICAL: 'text-danger-300 bg-danger-500/15',
}

function SummaryCard({ label, value, icon: Icon, tone }: {
  label: string
  value: number
  icon: typeof Flag
  tone: string
}) {
  return (
    <div className="card flex items-center gap-4">
      <div className={`p-3 rounded-xl ${tone}`}>
        <Icon className="w-5 h-5" />
      </div>
      <div>
        <p className="text-2xl font-bold text-white">{value}</p>
        <p className="text-sm text-slate-400">{label}</p>
      </div>
    </div>
  )
}

function ReportRow({ report, onReview, isReviewing }: {
  report: ReportResponse
  onReview: (status: 'CONFIRMED' | 'REJECTED') => void
  isReviewing: boolean
}) {
  return (
    <article className="border border-slate-700/60 rounded-xl p-4 space-y-3">
      <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-3">
        <div className="min-w-0">
          <p className="font-medium text-white break-all">{report.reportedUrl}</p>
          <p className="text-xs text-slate-500 mt-1">
            Reportado por {report.reportedBy} · {new Date(report.createdAt).toLocaleDateString()}
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span className="text-xs px-2 py-1 rounded-full bg-primary-500/15 text-primary-300">{report.reportType}</span>
          <span className={`text-xs px-2 py-1 rounded-full ${severityStyles[report.severity]}`}>{report.severity}</span>
        </div>
      </div>
      {report.description && <p className="text-sm text-slate-400">{report.description}</p>}
      <div className="flex gap-2 pt-1">
        <button type="button" disabled={isReviewing} onClick={() => onReview('CONFIRMED')} className="btn-primary inline-flex items-center gap-2 text-sm">
          <Check className="w-4 h-4" /> Confirmar
        </button>
        <button type="button" disabled={isReviewing} onClick={() => onReview('REJECTED')} className="btn-secondary inline-flex items-center gap-2 text-sm">
          <X className="w-4 h-4" /> Rechazar
        </button>
      </div>
    </article>
  )
}

export default function AdminDashboardPage() {
  const queryClient = useQueryClient()
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin', 'pending-reports'],
    queryFn: () => reportService.getPendingReports(0, 50),
  })
  const reviewMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'CONFIRMED' | 'REJECTED' }) => reportService.reviewReport(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'pending-reports'] }),
  })

  const reports = data?.content ?? []
  const critical = reports.filter((report) => report.severity === 'CRITICAL').length
  const high = reports.filter((report) => report.severity === 'HIGH').length

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2"><ShieldCheck className="w-6 h-6 text-primary-400" /> Panel de administracion</h1>
        <p className="text-slate-400 mt-1">Revisa los reportes comunitarios y ayuda a mantener segura la plataforma.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <SummaryCard label="Pendientes" value={data?.totalElements ?? 0} icon={Flag} tone="bg-primary-500/15 text-primary-300" />
        <SummaryCard label="Severidad alta" value={high} icon={AlertTriangle} tone="bg-orange-500/15 text-orange-300" />
        <SummaryCard label="Criticos" value={critical} icon={ShieldCheck} tone="bg-danger-500/15 text-danger-300" />
      </div>

      <section className="card">
        <div className="flex items-center justify-between gap-3 mb-4">
          <div>
            <h2 className="text-lg font-semibold text-white">Cola de revision</h2>
            <p className="text-sm text-slate-500">Los reportes se actualizan despues de cada decision.</p>
          </div>
          <span className="text-xs text-slate-500">{reports.length} visibles</span>
        </div>
        {isLoading && <p className="text-slate-400 py-8 text-center">Cargando reportes...</p>}
        {isError && <p className="text-danger-300 py-8 text-center">No se pudo cargar la cola de reportes.</p>}
        {!isLoading && !isError && reports.length === 0 && (
          <div className="py-10 text-center">
            <ShieldCheck className="w-10 h-10 text-success-500 mx-auto mb-3" />
            <p className="text-slate-300 font-medium">Todo al dia</p>
            <p className="text-sm text-slate-500 mt-1">No hay reportes pendientes de revision.</p>
          </div>
        )}
        <div className="space-y-3">
          {reports.map((report) => (
            <ReportRow key={report.id} report={report} isReviewing={reviewMutation.isPending} onReview={(status) => reviewMutation.mutate({ id: report.id, status })} />
          ))}
        </div>
      </section>
    </div>
  )
}