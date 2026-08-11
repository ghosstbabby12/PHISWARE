# Decisiones Arquitectónicas — PHISHWARE

## Patrón General: Clean Architecture + MVC

### Capas del Backend

```
Presentación  │  Controller  ← HTTP Request
              │      ↓
Aplicación    │  Service     ← Lógica de negocio
              │      ↓
Dominio       │  Entity      ← Modelos del dominio
              │      ↓
Datos         │  Repository  ← Acceso a BD
              │      ↓
              │  PostgreSQL  ← Persistencia
```

### Principios SOLID Aplicados

**S — Single Responsibility**
- `UrlAnalysisService`: solo orquesta el análisis
- `GoogleSafeBrowsingService`: solo llama a la API de GSB
- `GamificationService`: solo gestiona puntos y niveles
- `AuditLogService`: solo registra eventos

**O — Open/Closed**
- `RiskLevel` enum extensible sin modificar `UrlAnalysisService`
- Nuevas fuentes de análisis se añaden como beans inyectables

**L — Liskov Substitution**
- `UserDetails` implementado por `User` — Spring Security usa la interfaz

**I — Interface Segregation**
- Repositorios con interfaces específicas por entidad
- Sin repositorio "dios"

**D — Dependency Inversion**
- Services inyectados mediante `@RequiredArgsConstructor`
- `WebClient` inyectado por nombre (`@Qualifier`)

## Decisión: JWT sobre Sessions

**Problema**: La app debe funcionar en web y mobile.

**Decisión**: JWT stateless.

**Ventajas**:
- Sin estado en servidor → escala horizontalmente
- Funciona en mobile (Android) sin cookies
- Payload con roles evita consultas extra a BD

**Desventaja**: No revocación inmediata (mitigado con TTL corto de 24h).

## Decisión: Combinar Google Safe Browsing + VirusTotal

**Problema**: Ninguna API tiene cobertura del 100%.

**Decisión**: Sistema de scoring combinado con pesos (GSB: 60%, VT: 40%).

**Lógica**:
- GSB detecta: social engineering, malware, PHA
- VirusTotal detecta: malware con ratio de motores
- El score final [0-100] clasifica en SAFE/SUSPICIOUS/DANGEROUS

## Decisión: JSONB para respuestas crudas

**Problema**: Guardar respuestas completas de APIs (estructura variable).

**Decisión**: Columna `raw_response JSONB` en `url_analysis`.

**Ventaja**: Permite análisis histórico sin perder datos aunque el esquema de la API cambie.
