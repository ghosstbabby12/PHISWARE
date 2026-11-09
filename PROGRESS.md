# PROGRESS – v1.1 improvements (`feature/v1.1-improvements`)

Base: `main` @ `fd2c0c6`. Reglas: Conventional Commits (EN), commits pequeños, push cada 2-3 commits, verificar antes de cada commit (backend `mvn -q clean verify`; frontend `npm run lint && npm run build && npm run test`; android `./gradlew assembleDebug testDebugUnitTest`; extensión JSON válido). No push a main, no force-push.

## FASE 1 – Seguridad y limpieza
- [x] 1. `chore(security)` (`7433404`): sanear secretos (.env.example, docker-compose, application.yml, README) — _HISTORIAL: git todavía contiene las claves viejas (`.env.example` con GSB/VT keys, `DB_PASSWORD`, `JWT_SECRET`); hay que rotarlas a mano en Google Cloud, VirusTotal y donde aplique._
- [x] 2. `fix(auth)` (`7802472`): anti-enumeración de usuarios + test
- [x] 3. `fix(security)` (`53bc539`): SSRF con resolución DNS + inet_aton legado + tests
- [x] 4. `fix(rate-limit)` (`bf64089`): X-Forwarded-For solo desde trusted-proxies + test

## FASE 2 – Bugs del motor (5-8)
- [x] 5. `fix(virustotal)` (`5e1b944`): polling de GET /analyses/{id} + tests
- [x] 6. `perf(analysis)` (`acf9321`): CompletableFuture.allOf sin joins secuenciales
- [x] 7. `fix(system)` (`78766b8`): confianzas reales en /system/api-status
- [x] 8. `feat(auth)` (`9982e55`): refresh token con rotación + tests

## FASE 3 – Innovaciones del motor (9-13)
- [x] 9. `feat(engine)` (`1b27718`): persuasionPrinciple + catalog + V7 microlecciones + respuestas explicadas
- [x] 10. `feat(engine)` (`87b2cba`): sello oficial contra BrandCatalog
- [x] 11. `feat(engine)` (`5f149ba`): señal de favicon + hash SHA-256 en evidencia
- [x] 12. `feat(engine)` (`a1a11ed`): feed con reportes COMMUNITY CONFIRMED (confianza 0.9)
- [x] 13. `feat(engine)` (`cf185ff`): password-check k-anonimato (HIBP range API)
- Nota: la migración de microlecciones se creó como **V7** (no V9) para no dejar huecos V7/V8 en la cadena Flyway.

## FASE 4 – Frontend (14-17)
- [x] 14. `feat(web)` (`8e2af03`): theme light/dark/system + toggle + dark variants
- [x] 15. `feat(web)` (`475c53b` + `d8ba09f` parcial): i18n ES/EN completo (398 claves) + i18n:check + códigos de error
- [x] 16. `feat(web)` (`d8ba09f`): alerta explicada + microlección + sello + CAI Virtual + noisy-OR con recharts
- [x] 17. `feat(web)` (`f6b4b56`): badges/streaks/admin stats (backend+frontend) + 17 tests vitest

## FASE 5 – Extensión (18-19)
- [ ] 18. `feat(extension)`: heurística offline + fail-safe + blocklist
- [ ] 19. `feat(extension)`: config URL + i18n + theme

## FASE 6 – Android (20-22)
- [ ] 20. `feat(android)`: MessageTextAnalyzer on-device + tests
- [ ] 21. `feat(android)`: DayNight + ES/EN + alerta educativa
- [ ] 22. `chore(android)`: flavor play + quitar Glide + 2 instrumentados

## FASE 7 – Evaluación/calidad/despliegue (23-26)
- [ ] 23. `test(eval)`: dataset real + scripts corregidos + Wilcoxon
- [ ] 24. `ci`: GitHub Actions
- [ ] 25. `chore(deploy)`: Dockerfiles + compose
- [ ] 26. `docs`: README/ARCHITECTURE/OWASP_NIST_MAPPING/CHANGELOG/ISO

## FASE 8 – Documento de grado (27-30)
- [ ] 27-30. `docs(thesis)`: copia v1.1, correcciones, alineación, comparativa, biblio
- Nota: `docs/thesis/` NO existe en el repo; los .docx están en `C:\phsiware trabajo final\` (fuera del repo).

## Commits
(none yet)
