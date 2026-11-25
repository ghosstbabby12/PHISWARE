import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import MarkdownContent from '@/components/education/MarkdownContent'

describe('MarkdownContent', () => {
  it('renderiza negritas sin dejar los asteriscos visibles', () => {
    const { container } = render(<MarkdownContent source={'El **phishing** es un ataque.'} />)
    expect(screen.getByText('phishing').tagName).toBe('STRONG')
    expect(container.textContent).not.toContain('**')
  })

  it('agrupa listas numeradas, títulos y tablas', () => {
    const md = '## Pasos\n\n1. **Preparación**: crear el sitio\n2. Distribución\n\n| Nivel | Ejemplo |\n|---|---|\n| Alto | Sin MFA |'
    const { container } = render(<MarkdownContent source={md} />)
    expect(screen.getByRole('heading', { level: 2, name: 'Pasos' })).toBeInTheDocument()
    expect(container.querySelectorAll('ol > li')).toHaveLength(2)
    expect(screen.getByRole('columnheader', { name: 'Nivel' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'Sin MFA' })).toBeInTheDocument()
  })

  it('no interpreta HTML y solo enlaza URLs http(s)', () => {
    const md = '<img src=x onerror=alert(1)> [malo](javascript:alert(1)) [bueno](https://phishware.pro)'
    const { container } = render(<MarkdownContent source={md} />)
    expect(container.querySelector('img')).toBeNull()
    const links = container.querySelectorAll('a')
    expect(links).toHaveLength(1)
    expect(links[0].getAttribute('href')).toBe('https://phishware.pro')
    expect(links[0].getAttribute('rel')).toContain('noopener')
  })
})
