import { useState, useEffect, useCallback, useRef } from 'react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import type { UrlAnalysisResponse } from '@/types/analysis.types'

const URL_REGEX = /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)/gi
const SCAN_TTL_MS = 5 * 60 * 1000 // 5 min before re-scanning the same URL

export interface GuardAlert {
  url: string
  result: UrlAnalysisResponse
  at: number
}

export function useClipboardGuard() {
  const [isActive, setIsActive] = useState(false)
  const [lastAlert, setLastAlert] = useState<GuardAlert | null>(null)
  const [isScanning, setIsScanning] = useState(false)
  const scannedCache = useRef(new Map<string, number>())
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const showBrowserNotification = useCallback((result: UrlAnalysisResponse) => {
    if (!('Notification' in window) || Notification.permission !== 'granted') return
    const emoji = result.riskLevel === 'DANGEROUS' ? '🚨' : '⚠️'
    const label = result.riskLevel === 'DANGEROUS' ? 'URL PELIGROSA detectada' : 'URL Sospechosa'
    new Notification(`${emoji} PHISWARE — ${label}`, {
      body: `${result.domain}\nScore de riesgo: ${result.riskScore}/100`,
      tag: 'phisware-guard',
      requireInteraction: result.riskLevel === 'DANGEROUS',
    })
  }, [])

  const scanUrl = useCallback(async (url: string) => {
    const now = Date.now()
    const last = scannedCache.current.get(url)
    if (last && now - last < SCAN_TTL_MS) return

    // Skip localhost and RFC-1918 addresses
    if (/localhost|127\.0\.0\.1|::1|192\.168\.|10\.\d|172\.(1[6-9]|2\d|3[01])\./i.test(url)) return
    // Skip our own frontend/backend
    if (url.includes('localhost:5173') || url.includes('localhost:8080')) return

    scannedCache.current.set(url, now)
    setIsScanning(true)

    try {
      const result = await urlAnalysisService.analyze({ url })
      setLastAlert({ url, result, at: now })
      if (result.riskLevel !== 'SAFE') {
        showBrowserNotification(result)
      }
    } catch {
      // Network error or backend down — fail silently
    } finally {
      setIsScanning(false)
    }
  }, [showBrowserNotification])

  const readClipboard = useCallback(async () => {
    try {
      const text = await navigator.clipboard.readText()
      const matches = text.match(URL_REGEX)
      if (matches?.[0]) await scanUrl(matches[0])
    } catch {
      // Clipboard read denied or document not focused — ignore
    }
  }, [scanUrl])

  useEffect(() => {
    if (!isActive) {
      if (intervalRef.current) clearInterval(intervalRef.current)
      return
    }

    // Request notification permission when guard activates
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }

    // Instant detection: listen to copy events while on this tab
    const onCopy = () => setTimeout(readClipboard, 80)
    document.addEventListener('copy', onCopy)

    // Catch URLs copied from other tabs by polling on focus
    const onFocus = () => readClipboard()
    window.addEventListener('focus', onFocus)

    // Fallback polling every 3s (covers paste from keyboard etc.)
    intervalRef.current = setInterval(readClipboard, 3000)

    return () => {
      document.removeEventListener('copy', onCopy)
      window.removeEventListener('focus', onFocus)
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [isActive, readClipboard])

  const toggle = useCallback(() => setIsActive(prev => !prev), [])
  const clearAlert = useCallback(() => setLastAlert(null), [])

  return { isActive, toggle, lastAlert, isScanning, clearAlert }
}
