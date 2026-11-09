# CHANGELOG — PhishWare

Todos los cambios notables de este proyecto se documentan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

## [v1.1] — 2026-11-09 — Rama `feature/v1.1-improvements` (sin fusionar a `main`)

### Seguridad
- Secretos fuera del repo: `.env.example` sanitizado, `docker-compose.yml` y `application.yml` sin valores por defecto (fail-fast). **El historial aún contiene las claves viejas: rotarlas.**
- Anti-enumeración de usuarios (mensaje genérico `AUTH_INVALID_CREDENTIALS`).
- SSRF con resolución DNS + `inet_aton` legado + fail-closed (`SafeNetwork`).
- Rate-limit solo confía en `X-Forwarded-For` desde `trusted-proxies`.
- Códigos de error máquina-legibles (`code`) en todas las respuestas de error.

### Motor de detección
- VirusTotal con polling de `GET /analyses/{id}` hasta el veredicto.
- `CompletableFuture.allOf` en lugar de `join()` secuenciales.
- `/system/api-status` con confianzas reales, fórmula noisy-OR y umbrales.
- Refresh tokens con rotación (`POST /auth/refresh`; `type: access|refresh`).
- Principio de persuasión (Cialdini) por indicador + `IndicatorEducationCatalog` + 7 microlecciones (V7) + respuestas explicadas.
- Sello de sitio oficial contra `BrandCatalog` (+ en quick-check).
- Favicon con hash SHA-256 como evidencia + indicador `MISSING_BRAND_FAVICON`.
- Reportes comunitarios CONFIRMED como sexta fuente (0.9) + `GET /public/blocklist`.
- Verificación de filtraciones con k-anonimato (`POST /security/password-check`).

### Web
- Tema claro/oscuro/sistema + i18n ES/EN completo (398 claves, `npm run i18n:check`) + códigos de error traducidos.
- Alerta explicada, microlección enlazada, sello oficial, botón CAI Virtual, desglose noisy-OR con recharts.
- Insignias (12), racha, admin con gráficas (barras + pastel) y 17 tests vitest.

### Extensión MV3
- Heurística offline espejo del backend + fail-safe + blocklist cada 30 min (+ `declarativeNetRequest`), backend configurable, ES/EN, tema claro/oscuro, principio de persuasión visible.

### Android
- Analizador de texto on-device (~40 reglas ES/EN, nunca sale del dispositivo) + 10 tests.
- DayNight, inglés por defecto (`values/` EN, `values-es/` ES), alertas con principio/microlección/sello, flavor `play` sin SMS, sin Glide, 2 tests instrumentados.

### Evaluación / calidad / despliegue
- `build_dataset.py` (OpenPhish + Tranco + sitios CO, ≥200/clase), métricas corregidas (solo válidas, F1, tasa FP), `analizar_piloto.py` (Wilcoxon + r).
- CI GitHub Actions (4 jobs), Dockerfiles multi-stage + compose (postgres+backend+frontend).
- Docs: README/ARCHITECTURE/OWASP_NIST_MAPPING (Top 10:2025, CSF 2.0) actualizados, ISO con evidencia, este CHANGELOG.

### Pendiente (a mano)
- Rotar claves expuestas en el historial git.
- `docs/tests/piloto/resultados_piloto.csv` con mínimo 15 participantes + correr `analizar_piloto.py`.
- Calificar la rúbrica ISO con director + evaluador externo.
- Correr evaluación técnica con el dataset real y JMeter SG1–SG3.
- Crear el tag `v1.1-sustentacion` solo cuando el equipo lo confirme.
