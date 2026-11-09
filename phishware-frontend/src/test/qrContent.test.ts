import { describe, expect, it } from 'vitest'
import { extractUrls, normalizeUrl, parseQrContent, type QrStrings } from '@/utils/qrContent'
import type { Lang } from '@/i18n/translations'
import { STRINGS } from '@/i18n/translations'

function dict(lang: Lang): QrStrings {
  const t = STRINGS[lang]
  return {
    network: t.qr.network,
    unnamed: t.qr.unnamed,
    security: t.qr.security,
    openValue: t.qr.openValue,
    recipient: t.qr.recipient,
    message: t.qr.message,
    number: t.qr.number,
    name: t.qr.name,
    coords: t.qr.coords,
    openWifiWarning: t.qr.openWifiWarning,
    shortNumberWarning: t.qr.shortNumberWarning,
    kinds: t.qr.kinds,
  }
}

describe('qrContent', () => {
  it('normalizes bare domains with https', () => {
    expect(normalizeUrl('nequi-verificacion.tk/login')).toBe('https://nequi-verificacion.tk/login')
    expect(normalizeUrl('https://banco.com')).toBe('https://banco.com')
  })

  it('extracts at most 5 scannable URLs', () => {
    const urls = extractUrls('mira https://a.com/1 https://b.com/2 http://10.0.0.1/x http://localhost/y')
    expect(urls).toEqual(['https://a.com/1', 'https://b.com/2'])
  })

  it('parses an open WiFi QR without exposing the password', () => {
    const content = parseQrContent('WIFI:T:nopass;S:MiRed;P:clave123;;', dict('es'))
    expect(content.kind).toBe('WIFI')
    expect(content.urls).toEqual([])
    expect(content.details[0]).toMatchObject({ label: 'Red', value: 'MiRed' })
    expect(content.warnings).toHaveLength(1)
  })

  it('labels details in English when asked', () => {
    const content = parseQrContent('SMSTO:12345:Hola', dict('en'))
    expect(content.details[0]).toMatchObject({ label: 'Recipient', value: '12345' })
  })
})
