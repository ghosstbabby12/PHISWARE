import React, { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { Lang, STRINGS, Strings } from './translations'

const STORAGE_KEY = 'phishware_lang'

function initialLang(): Lang {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'es' || stored === 'en') return stored
  } catch { /* ignore */ }
  return 'es'
}

interface LangContextValue {
  lang: Lang
  t: Strings
  setLang: (l: Lang) => void
  toggle: () => void
}

const LanguageContext = createContext<LangContextValue | null>(null)

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [lang, setLangState] = useState<Lang>(initialLang)

  const setLang = useCallback((l: Lang) => {
    setLangState(l)
    try { localStorage.setItem(STORAGE_KEY, l) } catch { /* ignore */ }
  }, [])

  const toggle = useCallback(() => {
    setLangState(prev => {
      const next: Lang = prev === 'es' ? 'en' : 'es'
      try { localStorage.setItem(STORAGE_KEY, next) } catch { /* ignore */ }
      return next
    })
  }, [])

  useEffect(() => {
    document.documentElement.lang = lang
  }, [lang])

  return (
    <LanguageContext.Provider value={{ lang, t: STRINGS[lang], setLang, toggle }}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useLang(): LangContextValue {
  const ctx = useContext(LanguageContext)
  if (!ctx) throw new Error('useLang must be used within LanguageProvider')
  return ctx
}
