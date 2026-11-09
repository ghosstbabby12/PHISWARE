import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import RiskBadge from '@/components/common/RiskBadge'
import { LanguageProvider } from '@/i18n/LanguageContext'

function renderWithLang(ui: React.ReactElement, lang: 'es' | 'en' = 'es') {
  localStorage.setItem('phishware_lang', lang)
  return render(<LanguageProvider>{ui}</LanguageProvider>)
}

describe('RiskBadge', () => {
  it.each([
    ['SAFE', 'Seguro'],
    ['SUSPICIOUS', 'Sospechoso'],
    ['DANGEROUS', 'Peligroso'],
  ] as const)('shows the Spanish verdict for %s', (level, label) => {
    renderWithLang(<RiskBadge level={level} />)
    expect(screen.getByText(label)).toBeInTheDocument()
  })

  it('shows the English verdict when lang is en', () => {
    renderWithLang(<RiskBadge level="DANGEROUS" />, 'en')
    expect(screen.getByText('Dangerous')).toBeInTheDocument()
  })

  it('applies the severity class', () => {
    const { container } = renderWithLang(<RiskBadge level="SAFE" />)
    expect(container.querySelector('.badge-safe')).not.toBeNull()
  })
})
