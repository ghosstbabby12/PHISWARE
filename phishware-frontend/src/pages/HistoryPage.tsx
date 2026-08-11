import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { History, Filter } from 'lucide-react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import RiskBadge from '@/components/common/RiskBadge'
import { RiskLevel } from '@/types/analysis.types'

const RISK_FILTERS: { value: string; label: string }[] = [
  { value: '',           label: 'Todas' },
  { value: 'SAFE',      label: 'Seguras' },
  { value: 'SUSPICIOUS', label: 'Sospechosas' },
  { value: 'DANGEROUS', label: 'Peligrosas' },
]

export default function HistoryPage() {
  const [page, setPage] = useState(0)
  const [riskFilter, setRiskFilter] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['history', page, riskFilter],
    queryFn: () => urlAnalysisService.getHistory(page, 10, riskFilter || undefined),
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <History className="w-6 h-6 text-primary-400" />
            Historial de Análisis
          </h1>
          <p className="text-slate-400 mt-1">
            {data?.totalElements ?? 0} análisis realizados
          </p>
        </div>

        {/* Filter */}
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-500" />
          {RISK_FILTERS.map(f => (
            <button
              key={f.value}
              onClick={() => { setRiskFilter(f.value); setPage(0) }}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                riskFilter === f.value
                  ? 'bg-primary-600 text-white'
                  : 'bg-dark-800 text-slate-400 hover:text-white border border-slate-700'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-20">
          <div className="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full" />
        </div>
      ) : data?.content.length === 0 ? (
        <div className="card text-center py-16">
          <History className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400">No hay análisis registrados</p>
        </div>
      ) : (
        <div className="card overflow-hidden p-0">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700/50">
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-6 py-3">URL</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Dominio</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Riesgo</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Score</th>
                <th className="text-left text-xs font-medium text-slate-400 uppercase tracking-wider px-4 py-3">Fecha</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/30">
              {data?.content.map((item) => (
                <tr key={item.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="px-6 py-4">
                    <p className="text-sm font-mono text-slate-300 truncate max-w-xs" title={item.originalUrl}>
                      {item.originalUrl}
                    </p>
                  </td>
                  <td className="px-4 py-4 text-sm text-slate-400">{item.domain}</td>
                  <td className="px-4 py-4">
                    <RiskBadge level={item.riskLevel as RiskLevel} />
                  </td>
                  <td className="px-4 py-4 text-sm font-mono text-slate-300">
                    {item.riskScore?.toFixed(1)}
                  </td>
                  <td className="px-4 py-4 text-sm text-slate-400">
                    {new Date(item.analyzedAt).toLocaleDateString('es-ES', {
                      day: '2-digit', month: 'short', year: 'numeric',
                    })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={data.first}
            className="btn-ghost disabled:opacity-30"
          >
            Anterior
          </button>
          <span className="text-sm text-slate-400">
            Página {data.number + 1} de {data.totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={data.last}
            className="btn-ghost disabled:opacity-30"
          >
            Siguiente
          </button>
        </div>
      )}
    </div>
  )
}
