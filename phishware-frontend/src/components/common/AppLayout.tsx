import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import Sidebar from './Sidebar'
import Navbar from './Navbar'
import LinkGuardWidget from './LinkGuardWidget'

export default function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="flex min-h-screen bg-ink-50">
      {menuOpen && (
        <button
          type="button"
          aria-label="Cerrar menú de navegación"
          className="fixed inset-0 z-30 bg-ink-900/40 lg:hidden"
          onClick={() => setMenuOpen(false)}
        />
      )}
      <div className={`fixed z-40 h-full transition-transform duration-300 lg:static lg:z-auto lg:translate-x-0 ${menuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <Sidebar onNavigate={() => setMenuOpen(false)} />
      </div>
      <div className="flex flex-col flex-1 min-w-0">
        <Navbar onMenu={() => setMenuOpen(v => !v)} />
        <main className="flex-1 overflow-y-auto">
          <div className="max-w-6xl mx-auto w-full p-4 sm:p-6 lg:p-8">
            <Outlet />
          </div>
        </main>
      </div>
      <LinkGuardWidget />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4200,
          style: {
            background: '#fff',
            color: '#17171C',
            border: '1px solid #E7E4E8',
            borderRadius: '12px',
            fontSize: '14px',
          },
        }}
      />
    </div>
  )
}
