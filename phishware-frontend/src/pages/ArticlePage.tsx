import { useEffect } from 'react'
import { useParams, Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Clock, Eye } from 'lucide-react'
import { educationService } from '@/services/educationService'
import ModuleQuiz from '@/components/education/ModuleQuiz'
import MarkdownContent from '@/components/education/MarkdownContent'
import { useLang } from '@/i18n/LanguageContext'

export default function ArticlePage() {
  const { slug } = useParams<{ slug: string }>()
  const { t, lang } = useLang()

  // lang en la clave: al cambiar de idioma se vuelve a pedir el artículo traducido
  const { data: article, isLoading, isError } = useQuery({
    queryKey: ['article', slug, lang],
    queryFn: () => educationService.getBySlug(slug!),
    enabled: !!slug,
  })

  // Desde el panel de progreso se llega con #quiz: bajar directo a la evaluación
  const { hash } = useLocation()
  useEffect(() => {
    if (hash === '#quiz' && article) {
      document.getElementById('quiz')?.scrollIntoView({ behavior: 'smooth' })
    }
  }, [hash, article])

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="card animate-pulse space-y-4">
          <div className="h-8 bg-ink-100 dark:bg-dark-800 rounded w-3/4" />
          <div className="h-4 bg-ink-100 dark:bg-dark-800 rounded w-full" />
          <div className="h-4 bg-ink-100 dark:bg-dark-800 rounded w-5/6" />
          <div className="h-96 bg-ink-100 dark:bg-dark-800 rounded" />
        </div>
      </div>
    )
  }

  if (isError || !article) {
    return (
      <div className="max-w-3xl mx-auto text-center py-20">
        <p className="text-ink-500 dark:text-ink-300">{t.edu.notFound}</p>
        <Link to="/education" className="btn-primary mt-4 inline-flex">{t.common.back}</Link>
      </div>
    )
  }

  const categoryLabel = (t.edu.categories as Record<string, string>)[article.category] ?? article.category.replace(/_/g, ' ')

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <Link to="/education" className="inline-flex items-center gap-2 text-sm font-medium text-ink-500 dark:text-ink-300 hover:text-primary-700 dark:hover:text-white transition-colors">
        <ArrowLeft className="w-4 h-4" />
        {t.edu.back}
      </Link>

      <article className="card">
        <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-ink-500 dark:text-ink-300 mb-6">
          <span className="flex items-center gap-1.5">
            <Clock className="w-4 h-4" />
            {t.edu.readingTime(article.readingTimeMin)}
          </span>
          <span className="flex items-center gap-1.5">
            <Eye className="w-4 h-4" />
            {t.edu.views(article.views)}
          </span>
          <span className="badge-primary">{categoryLabel}</span>
        </div>

        {/* Algunos artículos (OWASP/NIST) empiezan en "##": se muestra el título del módulo */}
        {!article.content.trimStart().startsWith('# ') && (
          <h1 className="mb-4 text-2xl sm:text-3xl font-bold tracking-tight text-ink-900 dark:text-ink-50">{article.title}</h1>
        )}
        <MarkdownContent source={article.content} />

        {article.tags.length > 0 && (
          <div className="mt-8 pt-6 border-t divider flex flex-wrap gap-2">
            {article.tags.map(tag => (
              <span key={tag} className="px-3 py-1 bg-ink-100/60 dark:bg-white/5 text-ink-500 dark:text-ink-300 text-xs rounded-full">
                #{tag}
              </span>
            ))}
          </div>
        )}
      </article>

      <div id="quiz" className="scroll-mt-24">
        <ModuleQuiz slug={article.slug} />
      </div>
    </div>
  )
}
