/** Orbes de color difuminados detrás del vidrio. Puramente decorativo. */
export default function AmbientBackground() {
  return (
    <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden" aria-hidden>
      <div className="absolute -top-32 -left-24 h-[28rem] w-[28rem] rounded-full bg-primary-500/20 blur-3xl animate-blob dark:bg-primary-600/25" />
      <div className="absolute top-1/3 -right-32 h-[26rem] w-[26rem] rounded-full bg-primary-300/30 blur-3xl animate-blob [animation-delay:-7s] dark:bg-primary-800/40" />
      <div className="absolute -bottom-40 left-1/4 h-[24rem] w-[24rem] rounded-full bg-rose-200/40 blur-3xl animate-blob [animation-delay:-14s] dark:bg-primary-900/50" />
      {/* Malla sutil para dar textura al vidrio */}
      <div
        className="absolute inset-0 opacity-[0.035] dark:opacity-[0.06]"
        style={{
          backgroundImage: 'radial-gradient(currentColor 1px, transparent 1px)',
          backgroundSize: '22px 22px',
        }}
      />
    </div>
  )
}
