import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Shield, Info } from 'lucide-react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import { getErrorMessage } from '@/services/api'
import { UrlAnalysisResponse } from '@/types/analysis.types'
import UrlInputForm from '@/components/url-analysis/UrlInputForm'
import AnalysisResult from '@/components/url-analysis/AnalysisResult'

const TIPS = [
  'Verifica que la URL use HTTPS y el dominio sea correcto.',
  'Busca errores ortográficos en el dominio (paypa1.com, amaz0n.com).',
  'Los sitios legítimos nunca te piden contraseñas por correo o SMS.',
  'URLs muy largas o con parámetros extraños son señal de alerta.',
  'Usa esta herramienta antes de ingresar datos en un sitio desconocido.',
]

export default function UrlAnalysisPage() {
  const [result, setResult] = useState<UrlAnalysisResponse | null>(null)
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

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Shield className="w-6 h-6 text-primary-400" />
          Analizar URL
        </h1>
        <p className="text-slate-400 mt-1">
          Ingresa cualquier enlace para detectar phishing, malware y amenazas en tiempo real
        </p>
      </div>

      <div className="card">
        <UrlInputForm
          onSubmit={(url) => mutate({ url })}
          isLoading={isPending}
        />
      </div>

      {result && <AnalysisResult result={result} />}

      {/* Tips */}
      <div className="card border-primary-500/20">
        <h3 className="text-sm font-semibold text-slate-300 flex items-center gap-2 mb-3">
          <Info className="w-4 h-4 text-primary-400" />
          Consejos de Seguridad
        </h3>
        <ul className="space-y-2">
          {TIPS.map((tip, i) => (
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
