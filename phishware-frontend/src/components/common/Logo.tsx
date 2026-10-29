import clsx from 'clsx'

interface Props {
  size?: number
  showWordmark?: boolean
  dark?: boolean
  className?: string
}

/** Identidad PHISHWARE: escudo geometrico + enlace verificado. Monocromo y legible en pequeno. */
export default function Logo({ size = 36, showWordmark = true, dark = false, className }: Props) {
  return (
    <span className={clsx('inline-flex items-center gap-2.5', className)}>
      <img
        src="/phishware-logo.svg"
        alt="PHISHWARE"
        width={size}
        height={size}
        className="shrink-0 rounded-lg"
      />
      {showWordmark && (
        <span className="flex flex-col leading-none">
          <span className={clsx('font-bold tracking-tight', dark ? 'text-white' : 'text-ink-900')} style={{ fontSize: size * 0.44 }}>
            PHISHWARE
          </span>
          <span className={clsx('text-[11px] font-medium', dark ? 'text-white/60' : 'text-ink-500')}>
            Protección Inteligente
          </span>
        </span>
      )}
    </span>
  )
}
