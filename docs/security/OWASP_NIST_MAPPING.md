# PHISHWARE — Mapeo OWASP Top 10 × NIST CSF × Controles Implementados

**Versión:** 1.0  
**Estándar OWASP:** Top 10:2021  
**Estándar NIST:** Cybersecurity Framework v1.1 + SP 800-30 Rev.1 + SP 800-63B  

---

## Alcance

> Implementar controles de seguridad basados en las recomendaciones de OWASP y mecanismos de identificación, protección, detección y respuesta definidos por el NIST Cybersecurity Framework para fortalecer la prevención de ataques de phishing.

---

## Tabla de Mapeo Completa

| OWASP Top 10:2021 | NIST CSF Function | NIST Control | Control en PHISHWARE | Clase Java |
|---|---|---|---|---|
| A01 — Broken Access Control | PR (Protect) | PR.AC-3, PR.AC-4 | RBAC con `@PreAuthorize`, validación de propietario en recursos | `SecurityConfig`, `OwaspComplianceValidator#validateOwnership()` |
| A02 — Cryptographic Failures | PR (Protect) | PR.DS-1, PR.DS-2 | BCrypt (cost=12) para contraseñas, HTTPS obligatorio, JWT HS256 | `SecurityConfig`, `AuthService` |
| A03 — Injection | DE (Detect) | SI-10, SI-3 | Sanitización HTML, validación de URL, bloqueo de SQL/Command injection | `InputSanitizerService`, `OwaspComplianceValidator#validateUrlForInjection()` |
| A04 — Insecure Design | PR (Protect) | SC-5, DE.CM-7 | Rate limiting por IP (30 req/min) en endpoints críticos | `RateLimitingConfig.RateLimitFilter` |
| A05 — Security Misconfiguration | PR (Protect) | CM-6, CM-7 | Cabeceras HTTP de seguridad (CSP, HSTS, X-Frame-Options) | `SecurityHeadersConfig` |
| A06 — Vulnerable Components | PR (Protect) | SA-10, SI-2 | Spring Boot 3.2.5 LTS, dependencias con versiones explícitas, Flyway migrations | `pom.xml` |
| A07 — Auth Failures | ID (Identify) + PR | IA-5, IA-8 | JWT stateless, BCrypt, validación NIST SP 800-63B en contraseñas | `JwtTokenProvider`, `OwaspComplianceValidator#validatePasswordStrength()` |
| A08 — Data Integrity Failures | PR (Protect) | SI-7, CM-3 | Flyway versioned migrations, validación de input antes de persistir | `V1__create_schema.sql`, `V2__insert_initial_data.sql` |
| A09 — Logging Failures | DE (Detect) | AU-2, AU-3, AU-12 | `AuditLog` entity, logging de eventos de seguridad con nivel WARN | `AuditLogService`, `OwaspComplianceValidator#logSecurityEvent()` |
| A10 — SSRF | PR (Protect) | SC-7, AC-3 | Bloqueo de RFC 1918, loopback, cloud metadata antes de llamar APIs | `OwaspComplianceValidator#validateAgainstSsrf()` |

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
| PR.AC-1: Gestión de identidades | JWT con expiración 24h, refresh via re-login |
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
| DE.AE-2: Análisis de eventos detectados | Score combinado: GSB (60%) + VT (30%) + Heurística (10%) |
| DE.AE-3: Correlación de datos | `UrlAnalysis` combina resultados de múltiples fuentes |
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
 │  • RFC 1918 ranges (10.x, 192.168.x)        │
 │  • Cloud metadata (169.254.169.254)         │
 │  • localhost, ::1                           │
 └─────────────────┬───────────────────────────┘
                   │ URL segura para consultar
                   ▼
 ┌─────────────────────────────────────────────┐
 │  NIST ID/DE: NistThreatClassifier           │  ← Análisis heurístico local
 │  • Typosquatting de marcas (score: 30)      │
 │  • Subdominio sospechoso (score: 20)        │
 │  • TLD alto riesgo (score: 15)              │
 │  • Palabras clave phishing (score: 10)      │
 │  • URL excesivamente larga (score: 5)       │
 │  • IP como dominio (score: 35)              │
 │  • Subdominios excesivos (score: 15)        │
 └─────────────────┬───────────────────────────┘
                   │ HeuristicResult
          ┌────────┴────────┐
          ▼                 ▼
 ┌─────────────────┐ ┌─────────────────────────┐
 │ NIST DE:        │ │ NIST DE:                │
 │ Google Safe     │ │ VirusTotal API          │
 │ Browsing API    │ │ (70+ antivirus engines) │
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
Score = GSB_contribution(0-60) + VT_contribution(0-30) + Heuristic_contribution(0-10)

Donde:
  GSB_contribution  = 60.0 si GSB detecta amenaza, 0 si es seguro
  VT_contribution   = min(30, maliciousPercentage * 0.3)  si MALICIOUS
                    = min(15, suspiciousCount * 2)         si SUSPICIOUS
  Heuristic_contrib = heuristicScore * 0.10

Umbrales:
  score >= 50  →  RiskLevel.DANGEROUS
  score >= 20  →  RiskLevel.SUSPICIOUS
  score <  20  →  RiskLevel.SAFE
```

---

## Referencias Normativas

| Documento | Organización | Relevancia |
|---|---|---|
| [OWASP Top 10:2021](https://owasp.org/Top10/) | OWASP | Marco de vulnerabilidades web |
| [NIST CSF v1.1](https://www.nist.gov/cyberframework) | NIST | Framework de gestión de riesgo |
| [NIST SP 800-30 Rev.1](https://csrc.nist.gov/publications/detail/sp/800-30/rev-1/final) | NIST | Guía de evaluación de riesgos |
| [NIST SP 800-63B](https://pages.nist.gov/800-63-3/sp800-63b.html) | NIST | Requisitos de contraseñas e identidad digital |
| [NIST SP 800-53 Rev.5](https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final) | NIST | Controles de seguridad y privacidad |
| [RFC 1918](https://tools.ietf.org/html/rfc1918) | IETF | Rangos de IP privados (para bloqueo SSRF) |
