# PROGRESS – v1.1 improvements (`feature/v1.1-improvements`)

Base: `main` @ `fd2c0c6`. Reglas: Conventional Commits (EN), commits pequeños, push cada 2-3 commits, verificar antes de cada commit (backend `mvn -q clean verify`; frontend `npm run lint && npm run build && npm run test`; android `./gradlew assembleDebug testDebugUnitTest`; extensión JSON válido). No push a main, no force-push.

## FASE 1 – Seguridad y limpieza
- [ ] 1. `chore(security)`: sanear secretos (.env.example, docker-compose, application.yml, README) — _HISTORIAL: git todavía contiene las claves viejas; hay que rotarlas a mano._
- [ ] 2. `fix(auth)`: anti-enumeración de usuarios + test
- [ ] 3. `fix(security)`: SSRF con resolución DNS + tests
- [ ] 4. `fix(rate-limit)`: X-Forwarded-For solo desde trusted-proxies + test

## FASE 2 – Bugs del motor (5-8)
- [ ] 5. `fix(virustotal)`: polling de GET /analyses/{id}
- [ ] 6. `perf(analysis)`: CompletableFuture sin join bloqueante
- [ ] 7. `fix(system)`: confianzas reales en /system/api-status
- [ ] 8. `feat(auth)`: refresh token

## FASE 3 – Innovaciones del motor (9-13)
- [ ] 9. `feat(engine)`: persuasionPrinciple + IndicatorEducationCatalog + V9 + response
- [ ] 10. `feat(engine)`: official site seal
- [ ] 11. `feat(engine)`: favicon similarity
- [ ] 12. `feat(engine)`: community CONFIRMED feed
- [ ] 13. `feat(engine)`: password-check k-anonymity

## FASE 4 – Frontend (14-17)
- [ ] 14. `feat(web)`: theme light/dark/system
- [ ] 15. `feat(web)`: i18n ES/EN completo + i18n:check + códigos de error
- [ ] 16. `feat(web)`: explained alert + microlesson + seal + CAI Virtual
- [ ] 17. `feat(web)`: badges/streaks/admin stats + 10 tests

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
