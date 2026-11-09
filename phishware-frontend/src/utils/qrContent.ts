/**
 * Interpreta el texto decodificado de un código QR.
 * La extracción de URLs replica phishware-android/.../security/UrlExtractor.kt
 * para que web y móvil analicen exactamente lo mismo.
 */

export type QrKind = 'URL' | 'WIFI' | 'EMAIL' | 'PHONE' | 'SMS' | 'CONTACT' | 'LOCATION' | 'TEXT'

export interface QrStrings {
  network: string
  unnamed: string
  security: string
  openValue: string
  recipient: string
  message: string
  number: string
  name: string
  coords: string
  openWifiWarning: string
  shortNumberWarning: string
  kinds: Record<QrKind, string>
}

export interface QrContent {
  kind: QrKind
  raw: string
  /** URLs encontradas (máx. 5), ya normalizadas con esquema */
  urls: string[]
  /** Datos legibles según el tipo (red WiFi, destinatario, número...) */
  details: { label: string; value: string }[]
  /** Advertencias que no dependen del backend (WiFi abierta, SMS a número premium...) */
  warnings: string[]
}

const URL_REGEX = /\b((?:https?:\/\/|www\.)[^\s<>"'()]+)/gi
// Dominios desnudos típicos de smishing/quishing (sin https:// ni www): nequi-verificacion.tk/login
// Incluye TLDs de acortadores (bit.ly, goo.gl, t.me, is.gd, rb.gy). El lookbehind evita tomar
// el dominio de un correo (soporte@banco.com) como si fuera un enlace.
const BARE_DOMAIN_REGEX = /(?<![@\w.-])((?:[a-z0-9-]+\.)+(?:com|net|org|info|biz|xyz|tk|ml|ga|cf|top|icu|sbs|buzz|work|date|review|stream|science|racing|download|cricket|wang|faith|bid|men|loan|trade|party|zip|country|co|io|app|link|site|online|click|live|shop|store|ly|gl|me|gd|gy|cc|to|su)\b(?:\/[^\s<>"'()]*)?)/gi
const MAX_URL_LEN = 2048
const MAX_URLS = 5   // tope por QR: evita el rate-limit de 30/min del backend

export function normalizeUrl(raw: string): string {
  const u = raw.trim()
  return /^https?:\/\//i.test(u) ? u : `https://${u}`
}

/** Ignora localhost/RFC1918 y esquemas propios, igual que la extensión y la app móvil. */
export function isScannable(url: string): boolean {
  const lower = url.toLowerCase()
  if (!lower.startsWith('http://') && !lower.startsWith('https://')) return false
  const host = lower.split('://')[1]?.split('/')[0]?.split('@').pop()?.split(':')[0] ?? ''
  if (!host) return false
  if (host === 'localhost' || host.startsWith('127.') || host === '::1') return false
  if (host.startsWith('10.') || host.startsWith('192.168.')) return false
  if (host.startsWith('172.')) {
    const second = Number(host.split('.')[1])
    if (second >= 16 && second <= 31) return false
  }
  return true
}

export function extractUrls(text: string): string[] {
  if (!text.trim()) return []
  const withScheme = Array.from(text.matchAll(URL_REGEX), m => m[1])
  const masked = text.replace(URL_REGEX, ' ')
  const bare = Array.from(masked.matchAll(BARE_DOMAIN_REGEX), m => m[1])
  const urls = [...withScheme, ...bare]
    .map(u => u.trim().replace(/[.,;!?)\]"']+$/, ''))
    .map(normalizeUrl)
    .filter(u => u.length <= MAX_URL_LEN && isScannable(u))
  return Array.from(new Set(urls)).slice(0, MAX_URLS)
}

/** Lee campos del formato MECARD/WIFI: "WIFI:T:WPA;S:MiRed;P:clave;;" */
function mecardField(raw: string, key: string): string | undefined {
  const match = raw.match(new RegExp(`[:;]${key}:((?:\\\\.|[^;])*)`, 'i'))
  return match?.[1]?.replace(/\\(.)/g, '$1')
}

export function parseQrContent(raw: string, dict: QrStrings): QrContent {
  const text = raw.trim()
  const upper = text.toUpperCase()
  const base: QrContent = { kind: 'TEXT', raw: text, urls: extractUrls(text), details: [], warnings: [] }

  if (upper.startsWith('WIFI:')) {
    const security = (mecardField(text, 'T') || 'nopass').toUpperCase()
    const ssid = mecardField(text, 'S') ?? ''
    const open = security === 'NOPASS' || security === ''
    return {
      ...base,
      kind: 'WIFI',
      urls: [],   // la clave WiFi nunca se envía a ningún servidor
      details: [
        { label: dict.network, value: ssid || dict.unnamed },
        { label: dict.security, value: open ? dict.openValue : security },
      ],
      warnings: open ? [dict.openWifiWarning] : [],
    }
  }

  if (upper.startsWith('SMSTO:') || upper.startsWith('SMS:')) {
    const [, number = '', ...body] = text.split(':')
    const message = body.join(':')
    const warnings: string[] = []
    // Números cortos de 4-6 dígitos suelen ser servicios con tarifa especial
    if (/^\d{4,6}$/.test(number.replace(/\D/g, ''))) {
      warnings.push(dict.shortNumberWarning)
    }
    return {
      ...base,
      kind: 'SMS',
      urls: extractUrls(message),
      details: [{ label: dict.recipient, value: number }, { label: dict.message, value: message }],
      warnings,
    }
  }

  if (upper.startsWith('MAILTO:') || upper.startsWith('MATMSG:')) {
    const to = upper.startsWith('MAILTO:')
      ? decodeURIComponent(text.slice(7).split('?')[0])
      : mecardField(text, 'TO') ?? ''
    return { ...base, kind: 'EMAIL', details: [{ label: dict.recipient, value: to }] }
  }

  if (upper.startsWith('TEL:')) {
    return { ...base, kind: 'PHONE', urls: [], details: [{ label: dict.number, value: text.slice(4) }] }
  }

  if (upper.startsWith('BEGIN:VCARD') || upper.startsWith('MECARD:')) {
    const name = upper.startsWith('MECARD:')
      ? mecardField(text, 'N')
      : text.match(/^FN:(.*)$/im)?.[1]
    return { ...base, kind: 'CONTACT', details: name ? [{ label: dict.name, value: name.trim() }] : [] }
  }

  if (upper.startsWith('GEO:')) {
    return { ...base, kind: 'LOCATION', urls: [], details: [{ label: dict.coords, value: text.slice(4) }] }
  }

  if (base.urls.length > 0) return { ...base, kind: 'URL' }
  return base
}

