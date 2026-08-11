import { useState } from 'react'
import { Search, Link, Loader2 } from 'lucide-react'

interface Props {
  onSubmit: (url: string) => void
  isLoading: boolean
}

export default function UrlInputForm({ onSubmit, isLoading }: Props) {
  const [url, setUrl] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const trimmed = url.trim()
    if (trimmed) onSubmit(trimmed)
  }

  return (
    <form onSubmit={handleSubmit} className="w-full">
      <div className="flex gap-3">
        <div className="relative flex-1">
          <Link className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://ejemplo.com o pega cualquier enlace sospechoso..."
            className="input-field pl-11"
            disabled={isLoading}
          />
        </div>
        <button
          type="submit"
          disabled={isLoading || !url.trim()}
          className="btn-primary flex items-center gap-2 whitespace-nowrap"
        >
          {isLoading ? (
            <><Loader2 className="w-4 h-4 animate-spin" /> Analizando...</>
          ) : (
            <><Search className="w-4 h-4" /> Analizar</>
          )}
        </button>
      </div>
      <p className="mt-2 text-xs text-slate-500">
        Ejemplos: URL completa, dominio, o enlace de correo electrónico o SMS sospechoso
      </p>
    </form>
  )
}
