import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BookOpen, Clock, Eye, Tag, Shield, AlertTriangle, Search, SlidersHorizontal } from 'lucide-react'
import { educationService } from '@/services/educationService'

const CATEGORIES = [
  { value: '',                    label: 'Todo' },
  { value: 'PHISHING_BASICS',    label: 'Phishing' },
  { value: 'SOCIAL_ENGINEERING', label: 'Ing. Social' },
  { value: 'SMISHING',           label: 'Smishing' },
  { value: 'VISHING',            label: 'Vishing' },
  { value: 'BEST_PRACTICES',     label: 'Buenas Prácticas' },
  { value: 'OWASP',              label: 'OWASP Top 10' },
  { value: 'NIST',               label: 'NIST CSF' },
]

const DIFFICULTY_COLORS: Record<string, string> = {
  BEGINNER:     'bg-success-500/20 text-success-400',
  INTERMEDIATE: 'bg-warning-500/20 text-warning-400',
  ADVANCED:     'bg-danger-500/20 text-danger-400',
}

const DIFFICULTY_LABELS: Record<string, string> = {
  BEGINNER:     'Básico',
  INTERMEDIATE: 'Intermedio',
  ADVANCED:     'Avanzado',
}

function OwaspNistBanner({ category }: { category: string }) {
  if (category !== 'OWASP' && category !== 'NIST' && category !== '') return null

  if (category === 'OWASP') {
    return (
      <div className="rounded-xl border border-orange-500/30 bg-orange-500/5 p-5 flex gap-4">
        <div className="flex-shrink-0">
          <div className="w-12 h-12 rounded-lg bg-orange-500/20 flex items-center justify-center">
            <AlertTriangle className="w-6 h-6 text-orange-400" />
          </div>
        </div>
        <div>
          <h2 className="font-bold text-orange-300 mb-1">OWASP Top 10:2021 aplicado al phishing</h2>
          <p className="text-sm text-slate-400 leading-relaxed">
            El <strong className="text-slate-300">Open Web Application Security Project</strong> documenta las 10 vulnerabilidades más críticas en aplicaciones web.
            Los atacantes de phishing explotan estas mismas debilidades para crear sitios falsos convincentes y evadir detección.
            PHISHWARE implementa controles contra A01, A03, A04, A07, A09 y A10.
          </p>
          <div className="flex flex-wrap gap-2 mt-3">
            {['A01 Access Control','A03 Injection','A07 Auth Failures','A10 SSRF'].map(c => (
              <span key={c} className="text-xs px-2 py-0.5 rounded-full bg-orange-500/15 text-orange-400 border border-orange-500/20">
                {c}
              </span>
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (category === 'NIST') {
    return (
      <div className="rounded-xl border border-blue-500/30 bg-blue-500/5 p-5 flex gap-4">
        <div className="flex-shrink-0">
          <div className="w-12 h-12 rounded-lg bg-blue-500/20 flex items-center justify-center">
            <Shield className="w-6 h-6 text-blue-400" />
          </div>
        </div>
        <div>
          <h2 className="font-bold text-blue-300 mb-1">NIST Cybersecurity Framework en PHISHWARE</h2>
          <p className="text-sm text-slate-400 leading-relaxed">
            El <strong className="text-slate-300">National Institute of Standards and Technology</strong> define 5 funciones para gestionar el riesgo cibernético.
            PHISHWARE alinea su arquitectura de detección con este framework: heurística local (IDENTIFY),
            validaciones (PROTECT), APIs externas (DETECT), alertas (RESPOND) y este módulo educativo (RECOVER).
          </p>
          <div className="flex flex-wrap gap-2 mt-3">
            {['ID Identify','PR Protect','DE Detect','RS Respond','RC Recover'].map(fn => (
              <span key={fn} className="text-xs px-2 py-0.5 rounded-full bg-blue-500/15 text-blue-400 border border-blue-500/20">
                {fn}
              </span>
            ))}
          </div>
        </div>
      </div>
    )
  }

  // category === '' → show both as a summary row
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="rounded-xl border border-orange-500/30 bg-orange-500/5 p-4 flex gap-3">
        <AlertTriangle className="w-5 h-5 text-orange-400 flex-shrink-0 mt-0.5" />
        <div>
          <p className="text-sm font-semibold text-orange-300">OWASP Top 10</p>
          <p className="text-xs text-slate-400 mt-0.5">Vulnerabilidades web que facilitan el phishing y cómo prevenirlas</p>
        </div>
      </div>
      <div className="rounded-xl border border-blue-500/30 bg-blue-500/5 p-4 flex gap-3">
        <Shield className="w-5 h-5 text-blue-400 flex-shrink-0 mt-0.5" />
        <div>
          <p className="text-sm font-semibold text-blue-300">NIST CSF</p>
          <p className="text-xs text-slate-400 mt-0.5">Framework de 5 funciones para gestión integral del riesgo de phishing</p>
        </div>
      </div>
    </div>
  )
}

function CategoryBadge({ category }: { category: string }) {
  if (category === 'OWASP') {
    return <span className="text-xs px-2 py-0.5 rounded-full bg-orange-500/15 text-orange-400 border border-orange-500/20">OWASP</span>
  }
  if (category === 'NIST') {
    return <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/15 text-blue-400 border border-blue-500/20">NIST</span>
  }
  return null
}

export default function EducationPage() {
  const [category, setCategory] = useState('')
  const [difficulty, setDifficulty] = useState('')
  const [search, setSearch] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['education', category, difficulty],
    queryFn: () => educationService.getAll({
      category: category || undefined,
      difficulty: difficulty || undefined,
    }),
  })

  const normalizedSearch = search.trim().toLowerCase()
  const articles = data?.content.filter((article) => {
    if (!normalizedSearch) return true
    return [article.title, article.summary, ...article.tags]
      .join(' ')
      .toLowerCase()
      .includes(normalizedSearch)
  }) ?? []

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <BookOpen className="w-6 h-6 text-primary-400" />
          Módulo Educativo
        </h1>
        <p className="text-slate-400 mt-1">
          Fortalece tu cultura de ciberseguridad con nuestros recursos
        </p>
      </div>

      {/* Category filter */}
      <div className="flex flex-wrap gap-2">
        {CATEGORIES.map(c => (
          <button
            key={c.value}
            onClick={() => setCategory(c.value)}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
              category === c.value
                ? c.value === 'OWASP'
                  ? 'bg-orange-600 text-white'
                  : c.value === 'NIST'
                    ? 'bg-blue-600 text-white'
                    : 'bg-primary-600 text-white'
                : 'bg-dark-800 text-slate-400 hover:text-white border border-slate-700'
            }`}
          >
            {c.label}
          </button>
        ))}
      </div>

      <div className="flex flex-col md:flex-row gap-3">
        <label className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Buscar por título, tema o etiqueta"
            className="input pl-10 w-full"
            aria-label="Buscar contenido educativo"
          />
        </label>
        <label className="flex items-center gap-2 min-w-52">
          <SlidersHorizontal className="w-4 h-4 text-slate-500" />
          <select
            value={difficulty}
            onChange={(event) => setDifficulty(event.target.value)}
            className="input w-full"
            aria-label="Filtrar por dificultad"
          >
            <option value="">Todas las dificultades</option>
            <option value="BEGINNER">Básico</option>
            <option value="INTERMEDIATE">Intermedio</option>
            <option value="ADVANCED">Avanzado</option>
          </select>
        </label>
      </div>

      {/* OWASP / NIST contextual banner */}
      <OwaspNistBanner category={category} />

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-4 bg-slate-700 rounded w-3/4 mb-3" />
              <div className="h-3 bg-slate-700 rounded w-full mb-2" />
              <div className="h-3 bg-slate-700 rounded w-5/6" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {articles.map((article) => (
            <Link
              key={article.id}
              to={`/education/${article.slug}`}
              className={`card hover:-translate-y-0.5 transition-all duration-200 group ${
                article.category === 'OWASP'
                  ? 'hover:border-orange-500/50'
                  : article.category === 'NIST'
                    ? 'hover:border-blue-500/50'
                    : 'hover:border-primary-500/50'
              }`}
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-2">
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${DIFFICULTY_COLORS[article.difficulty] ?? ''}`}>
                    {DIFFICULTY_LABELS[article.difficulty] ?? article.difficulty}
                  </span>
                  <CategoryBadge category={article.category} />
                </div>
                <span className="text-xs text-slate-500 flex items-center gap-1">
                  <Eye className="w-3 h-3" />
                  {article.views}
                </span>
              </div>

              <h3 className={`font-semibold text-white transition-colors mb-2 line-clamp-2 ${
                article.category === 'OWASP'
                  ? 'group-hover:text-orange-400'
                  : article.category === 'NIST'
                    ? 'group-hover:text-blue-400'
                    : 'group-hover:text-primary-400'
              }`}>
                {article.title}
              </h3>
              <p className="text-sm text-slate-400 line-clamp-2 mb-4">{article.summary}</p>

              <div className="flex items-center justify-between text-xs text-slate-500">
                <span className="flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  {article.readingTimeMin} min de lectura
                </span>
                {article.tags.length > 0 && (
                  <span className="flex items-center gap-1">
                    <Tag className="w-3 h-3" />
                    {article.tags[0]}
                  </span>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}

      {!isLoading && articles.length === 0 && (
        <div className="card text-center py-12">
          <Search className="w-8 h-8 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-300 font-medium">No encontramos contenido</p>
          <p className="text-sm text-slate-500 mt-1">Prueba con otra búsqueda o cambia los filtros.</p>
        </div>
      )}

      {data?.content.length === 0 && (
        <div className="card text-center py-16">
          <BookOpen className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400">No hay contenido disponible para esta categoría</p>
        </div>
      )}
    </div>
  )
}
