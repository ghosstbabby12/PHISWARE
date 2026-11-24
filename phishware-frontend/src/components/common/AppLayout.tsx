import { Outlet, useLocation } from 'react-router-dom'
import FloatingDock from './FloatingDock'
import Navbar from './Navbar'
import QuickActionsFab from './QuickActionsFab'
import SafetyAssistant from './SafetyAssistant'
import AmbientBackground from './AmbientBackground'

export default function AppLayout() {
  const { pathname } = useLocation()

  return (
    <div className="relative isolate min-h-screen lg:pl-20">
      <AmbientBackground />
      <FloatingDock />
      <Navbar />
      <main className="mx-auto w-full max-w-6xl px-4 pt-6 pb-32 sm:px-6 lg:px-8 lg:pb-28">
        {/* key: re-dispara la animación de entrada en cada cambio de página */}
        <div key={pathname} className="animate-slide-up">
          <Outlet />
        </div>
      </main>
      <SafetyAssistant />
      <QuickActionsFab />
    </div>
  )
}
