import { Link } from 'react-router-dom'
import { Shield, Search, BookOpen, Bell, ChevronRight, Lock, Zap, Globe } from 'lucide-react'

const FEATURES = [
  {
    icon: Search,
    title: 'Análisis de URLs en Tiempo Real',
    desc: 'Detecta phishing, malware y amenazas usando Google Safe Browsing y VirusTotal API.',
  },
  {
    icon: Bell,
    title: 'Alertas Preventivas',
    desc: 'Recibe notificaciones inmediatas cuando visitas un sitio peligroso o sospechoso.',
  },
  {
    icon: BookOpen,
    title: 'Módulo Educativo',
    desc: 'Aprende sobre phishing, ingeniería social, smishing y buenas prácticas de seguridad.',
  },
  {
    icon: Lock,
    title: 'Seguridad JWT + BCrypt',
    desc: 'Autenticación robusta con tokens JWT y contraseñas protegidas con BCrypt.',
  },
  {
    icon: Zap,
    title: 'Gamificación',
    desc: 'Gana puntos, sube de nivel y obtén insignias mientras te proteges.',
  },
  {
    icon: Globe,
    title: 'Multi-plataforma',
    desc: 'Disponible como aplicación web y app nativa Android.',
  },
]

export default function LandingPage() {
  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="border-b border-slate-700/50 bg-dark-900/80 backdrop-blur sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Shield className="w-6 h-6 text-primary-400" />
            <span className="font-bold text-white text-lg">PHISHWARE</span>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/login" className="btn-ghost">Iniciar Sesión</Link>
            <Link to="/register" className="btn-primary">Comenzar gratis</Link>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="max-w-6xl mx-auto px-6 py-24 text-center">
        <div className="inline-flex items-center gap-2 px-4 py-2 bg-primary-600/10 border border-primary-500/30 rounded-full text-primary-400 text-sm mb-8">
          <Zap className="w-4 h-4" />
          Protección inteligente contra phishing
        </div>
        <h1 className="text-5xl font-bold text-white mb-6 leading-tight">
          Detecta y Previene<br />
          <span className="text-primary-400">Ataques de Phishing</span>
        </h1>
        <p className="text-xl text-slate-400 max-w-2xl mx-auto mb-10">
          PHISHWARE analiza URLs en tiempo real, te alerta de amenazas y te educa
          para navegar de forma segura en Internet.
        </p>
        <div className="flex items-center justify-center gap-4">
          <Link to="/register" className="btn-primary flex items-center gap-2 text-base px-8 py-3">
            Crear cuenta gratis <ChevronRight className="w-4 h-4" />
          </Link>
          <Link to="/login" className="btn-ghost text-base px-8 py-3">
            Iniciar sesión
          </Link>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-6xl mx-auto px-6 pb-24">
        <h2 className="text-3xl font-bold text-white text-center mb-12">
          Todo lo que necesitas para navegar seguro
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="card hover:border-primary-500/40 transition-colors">
              <div className="p-2.5 bg-primary-600/20 rounded-lg w-fit mb-4 border border-primary-500/30">
                <Icon className="w-5 h-5 text-primary-400" />
              </div>
              <h3 className="font-semibold text-white mb-2">{title}</h3>
              <p className="text-sm text-slate-400">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-700/50 py-8 text-center text-slate-500 text-sm">
        <div className="flex items-center justify-center gap-2 mb-2">
          <Shield className="w-4 h-4 text-primary-400" />
          <span className="font-semibold text-white">PHISHWARE</span>
        </div>
        <p>Proyecto Académico-Profesional — Sistema de Detección y Prevención de Phishing</p>
      </footer>
    </div>
  )
}
