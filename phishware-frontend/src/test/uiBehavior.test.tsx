import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ThemeProvider } from '@/theme/ThemeContext'
import ThemeToggle from '@/components/common/ThemeToggle'
import { LanguageProvider } from '@/i18n/LanguageContext'
import { getErrorMessage } from '@/services/api'
import { AxiosError } from 'axios'

function axiosErrorWithCode(code: string) {
  return {
    isAxiosError: true,
    response: { data: { code, message: 'server text' }, status: 401 },
  } as AxiosError
}

describe('error codes', () => {
  it('maps AUTH_INVALID_CREDENTIALS to the Spanish message', () => {
    localStorage.setItem('phishware_lang', 'es')
    expect(getErrorMessage(axiosErrorWithCode('AUTH_INVALID_CREDENTIALS'))).toBe(
      'Credenciales incorrectas. Verifica tu usuario y contraseña.',
    )
  })

  it('maps AUTH_INVALID_CREDENTIALS to the English message', () => {
    localStorage.setItem('phishware_lang', 'en')
    expect(getErrorMessage(axiosErrorWithCode('AUTH_INVALID_CREDENTIALS'))).toBe(
      'Wrong credentials. Check your username and password.',
    )
  })

  it('falls back to the server message for unknown codes', () => {
    localStorage.setItem('phishware_lang', 'es')
    expect(getErrorMessage(axiosErrorWithCode('SOMETHING_ELSE'))).toBe('server text')
  })
})

describe('ThemeToggle', () => {
  it('toggles the dark class on the document', () => {
    localStorage.removeItem('phishware_theme')
    document.documentElement.classList.remove('dark')
    render(
      <LanguageProvider>
        <ThemeProvider>
          <ThemeToggle />
        </ThemeProvider>
      </LanguageProvider>,
    )
    fireEvent.click(screen.getByRole('button'))
    expect(document.documentElement.classList.contains('dark')).toBe(true)
    fireEvent.click(screen.getByRole('button'))
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })
})
