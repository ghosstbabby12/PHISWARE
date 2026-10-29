import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, Clock, Flag, Send } from 'lucide-react'
import toast from 'react-hot-toast'
import { reportService } from '@/services/reportService'
import { useLang } from '@/i18n/LanguageContext'
import type { ReportType, ReportSeverity, ReportResponse } from '@/types/report.types'
import AppHeader from '@/components/common/AppHeader'
import { EmptyState, ErrorState, LoadingState } from '@/components/common/FeedbackStates'

const reportTypeStyles: Record<ReportType, string> = {
  PHISHING: 'bg-danger-500/10 text-danger-600 border-danger-500/25',
  MALWARE: 'bg-danger-500/10 text-danger-600 border-danger-500/25',
  SUSPICIOUS: 'bg-warning-500/10 text-warning-600 border-warning-500/25',
  SCAM: 'bg-warning-500/10 text-warning-600 border-warning-500/25',
  SPAM: 'bg-ink-100 text-ink-500 border-ink-200',
}

const severityStyles: Record<ReportSeverity, string> = {
  LOW: 'bg-success-500/10 text-success-600 border border-success-500/25',
  MEDIUM: 'bg-warning-500/10 text-warning-600 border border-warning-500/25',
  HIGH: 'bg-danger-500/10 text-danger-600 border border-danger-500/25',
  CRITICAL: 'bg-danger-600/10 text-danger-700 border border-danger-600/25',
}

const statusStyles: Record<string, string> = {
  PENDING: 'bg-warning-500/10 text-warning-600 border border-warning-500/25',
  REVIEWED: 'bg-primary-100 text-primary-700 border border-primary-600/15',
  CONFIRMED: 'bg-danger-500/10 text-danger-600 border border-danger-500/25',
  REJECTED: 'bg-ink-100 text-ink-500 border border-ink-200',
}

const TYPES: ReportType[] = ['PHISHING', 'MALWARE', 'SUSPICIOUS', 'SCAM', 'SPAM']
const SEVS: ReportSeverity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export default function ReportPage() {
  const { t } = useLang()
  const queryClient = useQueryClient()
  const [url, setUrl] = useState('')
  const [type, setType] = useState<ReportType>('PHISHING')
  const [severity, setSeverity] = useState<ReportSeverity>('MEDIUM')
  const [description, setDescription] = useState('')

  const createMutation = useMutation({
    mutationFn: reportService.createReport,
    onSuccess: () => {
      toast.success(t.report.sent)
      setUrl('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['my-reports'] })
    },
    onError: () => toast.error(t.report.sendFail),
  })

  const { data: myReports, isLoading, isError, refetch } = useQuery({
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
      <AppHeader title={t.report.title} subtitle={t.report.sub} />

      <form onSubmit={handleSubmit} className="card space-y-5" aria-label={t.report.formTitle}>
        <h2 className="font-semibold text-ink-900 flex items-center gap-2">
          <span className="p-2 bg-warning-500/10 rounded-xl border border-warning-500/25">
            <AlertTriangle className="w-4 h-4 text-warning-600" aria-hidden />
          </span>
          {t.report.formTitle}
        </h2>

        <div>
          <label htmlFor="report-url" className="field-label">{t.report.urlLabel}</label>
          <input
            id="report-url"
            type="text"
            inputMode="url"
            value={url}
            onChange={e => setUrl(e.target.value)}
            placeholder="https://ejemplo-sospechoso.com/login"
            className="input-field font-mono text-[13px]"
            required
          />
          <p className="field-hint">{t.report.urlHint}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <fieldset>
            <legend className="field-label">{t.report.typeLabel}</legend>
            <div className="flex flex-wrap gap-2">
              {TYPES.map(rt => (
                <button
                  key={rt}
                  type="button"
                  onClick={() => setType(rt)}
                  aria-pressed={type === rt}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                    type === rt ? reportTypeStyles[rt] : 'border-ink-200 text-ink-500 hover:text-ink-900 hover:border-ink-300'
                  }`}
                >
                  {t.report.types[rt]}
                </button>
              ))}
            </div>
          </fieldset>

          <fieldset>
            <legend className="field-label">{t.report.sevLabel}</legend>
            <div className="flex flex-wrap gap-2">
              {SEVS.map(s => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setSeverity(s)}
                  aria-pressed={severity === s}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    severity === s ? severityStyles[s] : 'text-ink-500 hover:text-ink-900 bg-ink-50 border border-ink-200'
                  }`}
                >
                  {t.report.sevs[s]}
                </button>
              ))}
            </div>
          </fieldset>
        </div>

        <div>
          <label htmlFor="report-desc" className="field-label">{t.report.descLabel} <span className="font-normal text-ink-500">{t.report.descOpt}</span></label>
          <textarea
            id="report-desc"
            value={description}
            onChange={e => setDescription(e.target.value)}
            placeholder={t.report.descPh}
            className="input-field min-h-[80px] resize-y"
            maxLength={1000}
          />
        </div>

        <button
          type="submit"
          disabled={createMutation.isPending || !url.trim()}
          className="btn-primary flex items-center gap-2"
        >
          <Send className="w-4 h-4" aria-hidden />
          {createMutation.isPending ? t.report.sending : t.report.send}
        </button>
      </form>

      <section className="card space-y-4" aria-label={t.report.recent}>
        <h2 className="font-semibold text-ink-900 flex items-center gap-2">
          <Clock className="w-5 h-5 text-primary-700" aria-hidden />
          {t.report.recent}
        </h2>
        {isLoading ? (
          <LoadingState message={t.common.loading} rows={2} />
        ) : isError ? (
          <ErrorState title={t.history.loadFail} description={t.history.loadFailSub} onRetry={() => refetch()} />
        ) : !myReports?.content || myReports.content.length === 0 ? (
          <EmptyState icon={Flag} title={t.history.empty} description={t.report.sub} />
        ) : (
          <ul className="space-y-3">
            {myReports.content.map((report: ReportResponse) => (
              <li key={report.id} className="bg-ink-50 rounded-xl p-4 border divider">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-ink-900 font-mono truncate" title={report.reportedUrl}>{report.reportedUrl}</p>
                    <p className="text-xs text-ink-500 mt-1">
                      {report.description || t.report.noDesc}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold border ${reportTypeStyles[report.reportType as ReportType] ?? ''}`}>
                      {t.report.types[report.reportType as ReportType] ?? report.reportType}
                    </span>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${statusStyles[report.status] ?? ''}`}>
                      {t.report.status[report.status as keyof typeof t.report.status] ?? report.status}
                    </span>
                  </div>
                </div>
                <p className="text-xs text-ink-300 mt-2">
                  <time dateTime={report.createdAt}>
                    {new Date(report.createdAt).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </time>
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
