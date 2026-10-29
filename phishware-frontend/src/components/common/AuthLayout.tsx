import { Link } from 'react-router-dom'
import { ShieldCheck } from 'lucide-react'
import Logo from './Logo'

interface Props {
  title: string
  subtitle: string
  children: React.ReactNode
  footer?: React.ReactNode
}

/** Marco de autenticación: identidad + tarjeta clara + garantías. */
export default function AuthLayout({ title, subtitle, children, footer }: Props) {
  return (
    <div className="min-h-screen bg-ink-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-7">
          <Link to="/" className="inline-block" aria-label="PHISHWARE — inicio">
            <Logo size={52} />
          </Link>
          <h1 className="text-2xl font-bold text-ink-900 tracking-tight mt-4">{title}</h1>
          <p className="text-ink-500 text-sm mt-1">{subtitle}</p>
        </div>

        <div className="card">{children}</div>

        {footer ?? (
          <p className="flex items-center justify-center gap-1.5 text-xs text-ink-500 mt-5">
            <ShieldCheck className="w-3.5 h-3.5 text-success-500" />
            Tus credenciales viajan cifradas y nunca se exponen en el análisis.
          </p>
        )}
      </div>
    </div>
  )
}
