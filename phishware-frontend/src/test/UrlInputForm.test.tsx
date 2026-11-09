import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import UrlInputForm from '@/components/url-analysis/UrlInputForm'
import { LanguageProvider } from '@/i18n/LanguageContext'

function renderForm(props?: Partial<React.ComponentProps<typeof UrlInputForm>>) {
  localStorage.setItem('phishware_lang', 'es')
  return render(
    <LanguageProvider>
      <UrlInputForm onSubmit={() => {}} isLoading={false} {...props} />
    </LanguageProvider>,
  )
}

describe('UrlInputForm', () => {
  it('renders the input and the analyze button', () => {
    renderForm()
    expect(screen.getByLabelText('Enlace a analizar')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /analizar enlace/i })).toBeInTheDocument()
  })

  it('shows the hint before typing', () => {
    renderForm()
    expect(screen.getByText(/Acepta URL completas/i)).toBeInTheDocument()
  })

  it('shows an error and does not submit an invalid URL', () => {
    const onSubmit = vi.fn()
    renderForm({ onSubmit })
    fireEvent.change(screen.getByLabelText('Enlace a analizar'), { target: { value: 'no es url??' } })
    fireEvent.click(screen.getByRole('button', { name: /analizar enlace/i }))
    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits a valid URL', () => {
    const onSubmit = vi.fn()
    renderForm({ onSubmit })
    fireEvent.change(screen.getByLabelText('Enlace a analizar'), { target: { value: 'https://ejemplo.com/ingreso' } })
    fireEvent.click(screen.getByRole('button', { name: /analizar enlace/i }))
    expect(onSubmit).toHaveBeenCalledWith('https://ejemplo.com/ingreso')
  })
})
