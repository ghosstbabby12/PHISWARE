# PHISHWARE — Mapeo OWASP Top 10 × NIST CSF × Controles Implementados

**Versión:** 1.1 (rama `feature/v1.1-improvements`)
**Estándar OWASP:** Top 10:2025 (migración desde Top 10:2021)
**Estándar NIST:** Cybersecurity Framework 2.0 (6 funciones, incluye Govern) + SP 800-30 Rev.1 + SP 800-63B

---

## Alcance

> Implementar controles de seguridad basados en las recomendaciones de OWASP y mecanismos de identificación, protección, detección y respuesta definidos por el NIST Cybersecurity Framework para fortalecer la prevención de ataques de phishing.

---

## Tabla de Mapeo Completa

| OWASP Top 10:2025 | NIST CSF 2.0 Function | NIST Control | Control en PHISHWARE | Clase Java |
|---|---|---|---|---|
| A01 — Broken Access Control | PR (Protect) | PR.AC-3, PR.AC-4 | RBAC con `@PreAuthorize`, validación de propietario en recursos | `SecurityConfig`, `OwaspComplianceValidator#validateOwnership()` |
| A02 — Cryptographic Failures | PR (Protect) | PR.DS-1, PR.DS-2 | BCrypt (cost=12) para contraseñas, HTTPS, JWT HS256 + refresh rotation | `SecurityConfig`, `AuthService`, `JwtTokenProvider` |
| A03 — Injection | DE (Detect) | SI-10, SI-3 | Sanitización HTML, validación de URL, bloqueo de SQL/Command injection | `InputSanitizerService`, `OwaspComplianceValidator#validateUrlForInjection()` |
| A04 — Insecure Design | PR (Protect) | SC-5, DE.CM-7 | Rate limiting por IP (120 req/min) en endpoints críticos, `X-Forwarded-For` solo desde trusted proxies | `RateLimitingConfig.RateLimitFilter` |
| A05 — Security Misconfiguration | PR (Protect) | CM-6, CM-7 | Cabeceras HTTP de seguridad (CSP, HSTS, X-Frame-Options), sin valores por defecto inseguros | `SecurityHeadersConfig` |
| A06 — Vulnerable Components | PR (Protect) | SA-10, SI-2 | Spring Boot 3.5 / Java 17 LTS, dependencias con versiones explícitas, Flyway V1–V7 | `pom.xml` |
| A07 — Auth Failures | ID (Identify) + PR | IA-5, IA-8 | JWT stateless + refresh rotation, BCrypt, NIST SP 800-63B, sin enumeración de usuarios | `JwtTokenProvider`, `AuthController#/auth/refresh`, `GlobalExceptionHandler` |
| A08 — Data Integrity Failures | PR (Protect) | SI-7, CM-3 | Flyway versioned migrations, validación de input antes de persistir | `V1__create_schema.sql`, `V7__persuasion_micro_lessons.sql` |
| A09 — Logging Failures | DE (Detect) | AU-2, AU-3, AU-12 | `AuditLog` entity, logging de eventos de seguridad con nivel WARN | `AuditLogService`, `OwaspComplianceValidator#logSecurityEvent()` |
| A10 — SSRF | PR (Protect) | SC-7, AC-3 | Resolución DNS + bloqueo de non-public (loopback, RFC 1918, CGNAT, ULA, `inet_aton` legado) antes de llamar APIs | `SafeNetwork`, `OwaspComplianceValidator#validateAgainstSsrf()`, `SafePageFetcher` |
| — (controles nuevos v1.1) | DE (Detect) | DE.CM-2 | VT con polling del veredicto; comunidad CONFIRMED como 6.ª fuente; k-anonimato HIBP | `VirusTotalService`, `CommunityReportService`, `PasswordBreachService` |
| — (NIST CSF 2.0) | GV (Govern) | GV.OC-1, GV.SC-1 | Roles y alcance documentados; gestión de riesgo del proyecto en PROGRESS.md | `docs/`, `PROGRESS.md` |

---

## NIST CSF — Implementación por Función

### IDENTIFY (ID) — Identificar

| Sub-categoría NIST | Implementación PHISHWARE |
|---|---|
| ID.AM-1: Inventario de activos físicos | Entidades JPA mapeadas a tablas PostgreSQL con relaciones explícitas |
| ID.AM-2: Inventario de activos de software | `pom.xml` con versiones explícitas, Flyway para estado de BD |
| ID.RA-1: Identificación de vulnerabilidades | Motor heurístico con 7 indicadores de phishing |
| ID.RA-2: Inteligencia de amenazas | Consulta a Google Safe Browsing + VirusTotal |
| ID.RA-3: Amenazas internas y externas | `NistThreatClassifier` — clasifica cada URL con función NIST por indicador |
| ID.SC-1: Cadena de suministro | WebClient con timeouts, manejo de errores en APIs externas |

### PROTECT (PR) — Proteger

| Sub-categoría NIST | Implementación PHISHWARE |
|---|---|
| PR.AC-1: Gestión de identidades | JWT con expiración 24h + refresh rotation (7d), refresh token nunca válido como Bearer |
| PR.AC-3: Gestión de acceso remoto | CORS configurado con orígenes explícitos, HTTPS |
| PR.AC-4: Permisos mínimos | `@PreAuthorize("hasRole('ADMIN')")` en endpoints admin |
| PR.AC-7: Autenticación de usuarios | BCrypt cost=12, contraseñas validadas contra NIST SP 800-63B |
| PR.DS-1: Protección de datos en reposo | Contraseñas nunca almacenadas en texto plano, tokens JWT firmados |
| PR.DS-2: Protección de datos en tránsito | HTTPS forzado via HSTS header |
| PR.IP-1: Línea base de configuración | `application.yml` con toda la configuración externalizada |
| PR.MA-2: Mantenimiento remoto controlado | Acceso admin solo con ROLE_ADMIN autenticado |
| PR.PT-3: Principio de mínimo funcional | Endpoints públicos explicitamente listados en SecurityConfig |

### DETECT (DE) — Detectar

| Sub-categoría NIST | Implementación PHISHWARE |
|---|---|
| DE.AE-1: Línea base de red establecida | Registro de todas las URLs analizadas con metadata |
| DE.AE-2: Análisis de eventos detectados | Score noisy-OR: heurística×0.75 + contenido×0.85 + edad×0.70 + comunidad×0.9 + GSB/VT |
| DE.AE-3: Correlación de datos | `UrlAnalysis` combina 6 fuentes; `scoreBreakdown` por fuente |
| DE.CM-1: Monitoreo de red | Rate limit logging de IPs que exceden el límite |
| DE.CM-3: Monitoreo de personal | `AuditLog` tabla registra acciones críticas |
| DE.CM-7: Monitoreo no autorizado | Rate limiting detecta comportamiento anómalo (DoS, scraping) |
| DE.DP-4: Comunicación de detección | Alertas automáticas generadas y persistidas |

### RESPOND (RS) — Responder

| Sub-categoría NIST | Implementación PHISHWARE |
|---|---|
| RS.CO-2: Reporte de incidentes | `Alert` entity con title, message, severity, recommendations |
| RS.CO-3: Compartir información | Recomendaciones incluidas en respuesta de análisis |
| RS.AN-1: Notificaciones investigadas | Dashboard muestra alertas no leídas con badge |
| RS.AN-2: Impacto del incidente | RiskLevel + RiskScore cuantifican el impacto |
| RS.MI-1: Contención de incidentes | Alerta inmediata si riskLevel != SAFE |
| RS.MI-3: Mitigación de vulnerabilidades | Recomendaciones personalizadas por tipo de amenaza |

### RECOVER (RC) — Recuperar

| Sub-categoría NIST | Implementación PHISHWARE |
|---|---|
| RC.IM-1: Lecciones aprendidas | Módulo educativo accesible desde alertas |
| RC.IM-2: Estrategias actualizadas | Quiz gamificado para evaluar y reforzar conocimiento |
| RC.CO-1: Relaciones con stakeholders | Sistema de puntos/niveles para incentivar uso continuo |

---

## Flujo de Análisis OWASP + NIST

```
 Usuario envía URL
        │
        ▼
 ┌─────────────────────────────────────────────┐
 │  OWASP A03: InputSanitizerService           │  ← Sanitizar + validar
 │  • Longitud máx 2048 chars                  │
 │  • Bloquear patrones peligrosos (XSS, etc.) │
 │  • Validar esquema (solo http/https)         │
 └─────────────────┬───────────────────────────┘
                   │ URL limpia
                   ▼
 ┌─────────────────────────────────────────────┐
 │  OWASP A03: OwaspComplianceValidator        │  ← Detectar inyección
 │  • SQL Injection patterns                   │
 │  • Command Injection patterns               │
 └─────────────────┬───────────────────────────┘
                   │ URL validada
                   ▼
 ┌─────────────────────────────────────────────┐
 │  OWASP A10: validateAgainstSsrf()           │  ← Bloquear SSRF
 │  • Resolucion DNS + rechazo loopback, RFC1918, CGNAT, ULA        │
 │  • inet_aton legado + fail-closed si no resuelve         │
 │  • Cloud metadata (169.254.169.254)                           │
 └─────────────────┬───────────────────────────┘
                   │ URL segura para consultar
                   ▼
 ┌─────────────────────────────────────────────┐
 │  NIST ID/DE: NistThreatClassifier           │  ← Análisis heurístico local
 │  • Typosquatting+marcas CO (40) + homoglifos  │
 │  • 18 reglas lexicas + 18 de contenido       │
 │  • favicon + edad RDAP + comunidad CONFIRMED  │
     └─────────────────┬───────────────────────────┘
                   │ HeuristicResult
          ┌────────┴────────┐
          ▼                 ▼
 ┌─────────────────┐ ┌─────────────────────────┐
 │ NIST DE:        │ │ NIST DE:                │
 │ Google Safe     │ │ VirusTotal API          │
 │ Browsing API    │ │ (70+ motores + polling) │
 └────────┬────────┘ └──────────┬──────────────┘
          │   SafeBrowsingResult │ VirusTotalResult
          └──────────┬───────────┘
                     ▼
 ┌─────────────────────────────────────────────┐
 │  NIST ID.RA-3: calculateCombinedRisk()      │  ← Score combinado
 │  • GSB contribuye: 60% del score            │
 │  • VT contribuye: 30% del score             │
 │  • Heurística contribuye: 10% del score     │
 │  • Score final: 0-100 → SAFE/SUSPICIOUS/    │
 │                          DANGEROUS          │
 └─────────────────┬───────────────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
 ┌──────────────┐     ┌───────────────────────┐
 │ Persistir    │     │ NIST RS:              │
 │ UrlAnalysis  │     │ Generar alerta si     │
 │ + Threats    │     │ riskLevel != SAFE     │
 └──────────────┘     └───────────────────────┘
                               │
                               ▼
                      ┌───────────────────┐
                      │ NIST RC:          │
                      │ Gamificación +    │
                      │ Módulo educativo  │
                      └───────────────────┘
```

---

## Cabeceras de Seguridad HTTP (OWASP A05 + NIST CM-6)

| Header | Valor | Control |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | Previene MIME sniffing |
| `X-Frame-Options` | `DENY` | Previene Clickjacking |
| `X-XSS-Protection` | `1; mode=block` | Filtro XSS legacy browsers |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Fuerza HTTPS (HSTS) |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self'; ...` | Restringe fuentes de contenido |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Controla header Referer |
| `Permissions-Policy` | `camera=(), microphone=(), geolocation=()` | Deshabilita APIs del browser |
| `Cache-Control` | `no-store, no-cache` (en /api/) | Evita caché de respuestas API |

**Clase:** `SecurityHeadersConfig.java` (OncePerRequestFilter)

---

## Score de Riesgo — Pesos y Umbrales

```
Score = 100 × (1 − Π_fuentes (1 − score_fuente × confianza_fuente))

Donde (confianzas en UrlAnalysisService.sourceConfidences()):
  urlHeuristic = heuristicScore/100 × 0.75
  pageContent  = contentScore/100 × 0.85
  domainAge    = ageScore/100 × 0.70
  communityConfirmed = 0.9 si hay reportes CONFIRMED del dominio
  googleSafeBrowsing = 0.95 si GSB marca peligro, 0.0 si seguro
  virusTotal   = min(0.95, 0.5 + malicious%/100) si MALICIOUS
               = min(0.4, suspicious × 0.08) si SUSPICIOUS

isPhishing = GSB/VT malicioso o score >= 70.

Umbrales:
  score >= 50  →  RiskLevel.DANGEROUS
  score >= 20  →  RiskLevel.SUSPICIOUS
  score <  20  →  RiskLevel.SAFE
```

---

## Referencias Normativas

| Documento | Organización | Relevancia |
|---|---|---|
| [OWASP Top 10:2025](https://top10.owasp.org/2025/es/) | OWASP | Marco de vulnerabilidades web |
| [OWASP ASVS 5.0](https://github.com/asvs) | OWASP | Verificación de seguridad de aplicaciones |
| [NIST CSF 2.0](https://doi.org/10.6028/NIST.CSWP.29) | NIST | Framework de gestión de riesgo (6 funciones, incluye Govern) |
| [NIST SP 800-30 Rev.1](https://csrc.nist.gov/publications/detail/sp/800-30/rev-1/final) | NIST | Guía de evaluación de riesgos |
| [NIST SP 800-63B](https://pages.nist.gov/800-63-3/sp800-63b.html) | NIST | Requisitos de contraseñas e identidad digital |
| [NIST SP 800-53 Rev.5](https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final) | NIST | Controles de seguridad y privacidad |
| [RFC 1918](https://tools.ietf.org/html/rfc1918) | IETF | Rangos de IP privados (para bloqueo SSRF) |
