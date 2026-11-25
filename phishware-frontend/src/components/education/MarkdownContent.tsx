import { Fragment, type ReactNode } from 'react'
import { CheckCircle2, Square } from 'lucide-react'

/**
 * Renderizador Markdown mínimo y seguro para los artículos educativos.
 * Genera elementos React (nunca HTML crudo), así que el contenido no puede inyectar scripts.
 * Soporta: # títulos, **negrita**, *cursiva*, `código`, bloques ```, tablas, listas
 * (-, ✅, 1., - [ ]) y enlaces [texto](https://...).
 */
export default function MarkdownContent({ source }: { source: string }) {
  return <div className="space-y-3 text-[15px] leading-relaxed text-ink-700 dark:text-ink-200">{parseBlocks(source)}</div>
}

// ── Inline ────────────────────────────────────────────────────────────────────

const INLINE = /(\*\*[^*]+\*\*|`[^`]+`|\[[^\]]+\]\([^)\s]+\)|\*[^*\s][^*]*\*)/g

function renderInline(text: string): ReactNode[] {
  return text.split(INLINE).filter(Boolean).map((part, i) => {
    if (part.startsWith('**') && part.endsWith('**') && part.length > 4) {
      return <strong key={i} className="font-semibold text-ink-900 dark:text-ink-50">{renderInline(part.slice(2, -2))}</strong>
    }
    if (part.startsWith('`') && part.endsWith('`') && part.length > 2) {
      return <code key={i} className="rounded-md bg-ink-100 px-1.5 py-0.5 font-mono text-[0.85em] text-primary-700 dark:bg-white/10 dark:text-primary-300">{part.slice(1, -1)}</code>
    }
    const link = part.match(/^\[([^\]]+)\]\(([^)\s]+)\)$/)
    if (link) {
      const href = link[2]
      if (!/^https?:\/\//i.test(href)) return <Fragment key={i}>{link[1]}</Fragment>
      return (
        <a key={i} href={href} target="_blank" rel="noopener noreferrer" className="font-medium text-primary-700 underline underline-offset-2 hover:text-primary-500 dark:text-primary-300">
          {link[1]}
        </a>
      )
    }
    if (part.startsWith('*') && part.endsWith('*') && part.length > 2) {
      return <em key={i}>{renderInline(part.slice(1, -1))}</em>
    }
    return <Fragment key={i}>{part}</Fragment>
  })
}

// ── Bloques ───────────────────────────────────────────────────────────────────

type ListKind = 'ul' | 'ol' | 'check' | 'todo'

function listKind(line: string): ListKind | null {
  if (/^\s*- \[[ x]\] /i.test(line)) return 'todo'
  if (/^\s*[-*] /.test(line)) return 'ul'
  if (/^\s*✅ /.test(line)) return 'check'
  if (/^\s*\d+\. /.test(line)) return 'ol'
  return null
}

function stripMarker(line: string, kind: ListKind): string {
  if (kind === 'todo') return line.replace(/^\s*- \[[ x]\] /i, '')
  if (kind === 'ul') return line.replace(/^\s*[-*] /, '')
  if (kind === 'check') return line.replace(/^\s*✅ /, '')
  return line.replace(/^\s*\d+\. /, '')
}

function isTableRow(line: string) {
  return /^\s*\|.*\|\s*$/.test(line)
}

function cells(line: string): string[] {
  return line.trim().replace(/^\||\|$/g, '').split('|').map(c => c.trim())
}

function parseBlocks(source: string): ReactNode[] {
  const lines = source.replace(/\r\n/g, '\n').split('\n')
  const out: ReactNode[] = []
  let i = 0
  let key = 0

  while (i < lines.length) {
    const line = lines[i]

    // Bloque de código
    if (line.trim().startsWith('```')) {
      const body: string[] = []
      i++
      while (i < lines.length && !lines[i].trim().startsWith('```')) body.push(lines[i++])
      i++ // cierre
      out.push(
        <pre key={key++} className="overflow-x-auto rounded-2xl border border-ink-200/80 bg-ink-900 p-4 font-mono text-[13px] leading-relaxed text-ink-50 dark:border-white/10 dark:bg-black/40">
          <code>{body.join('\n')}</code>
        </pre>,
      )
      continue
    }

    // Tabla
    if (isTableRow(line)) {
      const rows: string[][] = []
      while (i < lines.length && isTableRow(lines[i])) {
        const row = cells(lines[i])
        if (!row.every(c => /^:?-{2,}:?$/.test(c))) rows.push(row) // omite la fila separadora
        i++
      }
      const [head, ...body] = rows
      out.push(
        <div key={key++} className="overflow-x-auto rounded-2xl border border-ink-200/80 dark:border-white/10">
          <table className="w-full text-left text-sm">
            <thead className="bg-primary-50/70 text-ink-900 dark:bg-white/5 dark:text-ink-50">
              <tr>{head.map((c, j) => <th key={j} className="px-3 py-2 font-semibold">{renderInline(c)}</th>)}</tr>
            </thead>
            <tbody>
              {body.map((r, ri) => (
                <tr key={ri} className="border-t border-ink-200/70 dark:border-white/10">
                  {r.map((c, j) => <td key={j} className="px-3 py-2 align-top">{renderInline(c)}</td>)}
                </tr>
              ))}
            </tbody>
          </table>
        </div>,
      )
      continue
    }

    // Listas (agrupa líneas consecutivas del mismo tipo)
    const kind = listKind(line)
    if (kind) {
      const items: string[] = []
      while (i < lines.length && listKind(lines[i]) === kind) items.push(stripMarker(lines[i++], kind))
      if (kind === 'ol') {
        out.push(
          <ol key={key++} className="list-decimal space-y-1.5 pl-6 marker:font-semibold marker:text-primary-700 dark:marker:text-primary-300">
            {items.map((it, j) => <li key={j}>{renderInline(it)}</li>)}
          </ol>,
        )
      } else if (kind === 'check' || kind === 'todo') {
        const Icon = kind === 'check' ? CheckCircle2 : Square
        out.push(
          <ul key={key++} className="space-y-2">
            {items.map((it, j) => (
              <li key={j} className="flex items-start gap-2.5">
                <Icon className={`mt-0.5 h-4 w-4 shrink-0 ${kind === 'check' ? 'text-success-600' : 'text-ink-300 dark:text-ink-500'}`} aria-hidden />
                <span>{renderInline(it)}</span>
              </li>
            ))}
          </ul>,
        )
      } else {
        out.push(
          <ul key={key++} className="list-disc space-y-1.5 pl-6 marker:text-primary-600">
            {items.map((it, j) => <li key={j}>{renderInline(it)}</li>)}
          </ul>,
        )
      }
      continue
    }

    // Títulos
    const h = line.match(/^(#{1,3}) (.*)$/)
    if (h) {
      const text = renderInline(h[2])
      if (h[1].length === 1) out.push(<h1 key={key++} className="pt-2 text-2xl font-bold tracking-tight text-ink-900 dark:text-ink-50">{text}</h1>)
      else if (h[1].length === 2) out.push(<h2 key={key++} className="pt-4 text-xl font-semibold text-ink-900 dark:text-ink-50">{text}</h2>)
      else out.push(<h3 key={key++} className="pt-2 text-lg font-semibold text-primary-700 dark:text-primary-300">{text}</h3>)
      i++
      continue
    }

    if (line.trim() === '') { i++; continue }

    // Párrafo: une líneas seguidas hasta un bloque distinto
    const para: string[] = []
    while (
      i < lines.length && lines[i].trim() !== '' && !listKind(lines[i]) && !isTableRow(lines[i])
      && !/^#{1,3} /.test(lines[i]) && !lines[i].trim().startsWith('```')
    ) para.push(lines[i++])
    out.push(
      <p key={key++}>
        {para.map((p, j) => <Fragment key={j}>{j > 0 && <br />}{renderInline(p)}</Fragment>)}
      </p>,
    )
  }
  return out
}
