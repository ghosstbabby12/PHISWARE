import { useState } from 'react'
import { Search, Link as LinkIcon, Loader2 } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  onSubmit: (url: string) => void
  isLoading: boolean
}

function looksLikeUrl(value: string): boolean {
  const v = value.trim()
  if (!v) return false
  if (/^(https?:\/\/|www\.)/i.test(v)) return true
  return /^[^\s]+\.[^\s]{2,}(\/\S*)?$/.test(v)
}

export default function UrlInputForm({ onSubmit, isLoading }: Props) {
  const { t } = useLang()
  const [url, setUrl] = useState('')
  const [touched, setTouched] = useState(false)
  const trimmed = url.trim()
  const showError = touched && trimmed.length > 0 && !looksLikeUrl(trimmed)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (trimmed && looksLikeUrl(trimmed)) onSubmit(trimmed)
  }

  return (
    <form onSubmit={handleSubmit} className="w-full" noValidate>
      <label htmlFor="url-input" className="field-label">
        {t.analysis.urlLabel}
      </label>
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <LinkIcon className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
          <input
            id="url-input"
            type="text"
            inputMode="url"
            autoComplete="off"
            spellCheck={false}
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            onBlur={() => setTouched(true)}
            placeholder={t.analysis.urlPh}
            className={`input-field pl-11 ${showError ? 'input-error' : ''}`}
            disabled={isLoading}
            aria-invalid={showError}
            aria-describedby={showError ? 'url-error' : 'url-hint'}
          />
        </div>
        <button
          type="submit"
          disabled={isLoading || !trimmed}
          className="btn-primary whitespace-nowrap"
        >
          {isLoading ? (
            <><Loader2 className="w-4 h-4 animate-spin" aria-hidden /> {t.analysis.analyzing}</>
          ) : (
            <><Search className="w-4 h-4" aria-hidden /> {t.analysis.analyzeBtn}</>
          )}
        </button>
      </div>
      {showError ? (
        <p id="url-error" role="alert" className="field-error">{t.analysis.invalid}</p>
      ) : (
        <p id="url-hint" className="field-hint">{t.analysis.hint}</p>
      )}
    </form>
  )
}
