import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Clock, Eye } from 'lucide-react'
import { educationService } from '@/services/educationService'

export default function ArticlePage() {
  const { slug } = useParams<{ slug: string }>()

  const { data: article, isLoading, isError } = useQuery({
    queryKey: ['article', slug],
    queryFn: () => educationService.getBySlug(slug!),
    enabled: !!slug,
  })

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="card animate-pulse space-y-4">
          <div className="h-8 bg-slate-700 rounded w-3/4" />
          <div className="h-4 bg-slate-700 rounded w-full" />
          <div className="h-4 bg-slate-700 rounded w-5/6" />
          <div className="h-96 bg-slate-700 rounded" />
        </div>
      </div>
    )
  }

  if (isError || !article) {
    return (
      <div className="max-w-3xl mx-auto text-center py-20">
        <p className="text-slate-400">Artículo no encontrado</p>
        <Link to="/education" className="btn-primary mt-4 inline-block">Volver</Link>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in">
      <Link to="/education" className="inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white transition-colors">
        <ArrowLeft className="w-4 h-4" />
        Volver al módulo educativo
      </Link>

      <article className="card">
        <div className="flex items-center gap-4 text-sm text-slate-400 mb-6">
          <span className="flex items-center gap-1.5">
            <Clock className="w-4 h-4" />
            {article.readingTimeMin} min de lectura
          </span>
          <span className="flex items-center gap-1.5">
            <Eye className="w-4 h-4" />
            {article.views} vistas
          </span>
          <span className="bg-primary-600/20 text-primary-400 text-xs px-2 py-0.5 rounded-full border border-primary-500/30">
            {article.category.replace(/_/g, ' ')}
          </span>
        </div>

        <div className="prose prose-invert prose-slate max-w-none">
          <div
            className="text-slate-200 leading-relaxed whitespace-pre-wrap"
            style={{ fontFamily: 'inherit' }}
          >
            {/* Render markdown-like content */}
            {article.content.split('\n').map((line, i) => {
              if (line.startsWith('# '))  return <h1 key={i} className="text-2xl font-bold text-white mt-6 mb-3">{line.slice(2)}</h1>
              if (line.startsWith('## ')) return <h2 key={i} className="text-xl font-semibold text-white mt-5 mb-2">{line.slice(3)}</h2>
              if (line.startsWith('### ')) return <h3 key={i} className="text-lg font-semibold text-primary-400 mt-4 mb-2">{line.slice(4)}</h3>
              if (line.startsWith('- ') || line.startsWith('✅ ')) return (
                <li key={i} className="ml-4 text-slate-300 my-1 list-disc">{line.replace(/^[-✅] /, '')}</li>
              )
              if (line.trim() === '') return <br key={i} />
              return <p key={i} className="text-slate-300 my-2">{line}</p>
            })}
          </div>
        </div>

        {article.tags.length > 0 && (
          <div className="mt-6 pt-6 border-t border-slate-700/50 flex flex-wrap gap-2">
            {article.tags.map(tag => (
              <span key={tag} className="px-3 py-1 bg-slate-700/50 text-slate-400 text-xs rounded-full">
                #{tag}
              </span>
            ))}
          </div>
        )}
      </article>
    </div>
  )
}
