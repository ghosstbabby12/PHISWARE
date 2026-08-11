import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import { getErrorMessage } from '@/services/api'
import { UrlAnalysisResponse } from '@/types/analysis.types'

export function useUrlAnalysis() {
  const [result, setResult] = useState<UrlAnalysisResponse | null>(null)
  const queryClient = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: urlAnalysisService.analyze,
    onSuccess: (data) => {
      setResult(data)
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['history'] })

      if (data.riskLevel === 'DANGEROUS') {
        toast.error('⛔ Sitio peligroso detectado', { duration: 5000 })
      } else if (data.riskLevel === 'SUSPICIOUS') {
        toast('⚠️ Sitio sospechoso', { icon: '⚠️' })
      } else {
        toast.success('✅ URL analizada — Sin amenazas')
      }
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  return {
    analyze: (url: string) => mutate({ url }),
    result,
    isPending,
    clearResult: () => setResult(null),
  }
}
