# PHISHWARE — Sistema de Detección y Prevención de Phishing

> Proyecto Académico-Profesional | Arquitectura Full Stack | Java 21 · Spring Boot · React · Kotlin

---

## Tabla de Contenidos

1. [Descripción](#descripción)
2. [Objetivos](#objetivos)
3. [Arquitectura del Sistema](#arquitectura-del-sistema)
4. [Tecnologías](#tecnologías)
5. [Estructura del Proyecto](#estructura-del-proyecto)
6. [Modelo de Base de Datos](#modelo-de-base-de-datos)
7. [API REST](#api-rest)
8. [Diagrama de Clases](#diagrama-de-clases)
9. [Diagrama de Casos de Uso](#diagrama-de-casos-de-uso)
10. [Diagrama de Secuencia](#diagrama-de-secuencia)
11. [Instalación y Configuración](#instalación-y-configuración)
12. [Variables de Entorno](#variables-de-entorno)
13. [Testing](#testing)
14. [Seguridad Implementada](#seguridad-implementada)

---

## Descripción

**PHISHWARE** es una solución tecnológica orientada a la **detección y prevención de ataques de phishing**. Combina análisis automatizado de URLs con motores de seguridad de clase mundial (Google Safe Browsing + VirusTotal), alertas preventivas en tiempo real y un módulo educativo gamificado de ciberseguridad.

### ¿Por qué PHISHWARE?

El phishing es responsable del **36% de todos los ciberataques** según IBM Security. Las víctimas a menudo no distinguen sitios falsos de legítimos. PHISHWARE aborda este problema desde dos ángulos:

1. **Detección técnica**: Análisis automatizado con APIs de seguridad profesionales
2. **Educación**: Contenido didáctico que fortalece la cultura de ciberseguridad

---

## Objetivos

### General
Desarrollar una aplicación web y Android para detectar y prevenir ataques de phishing mediante análisis de amenazas, alertas en tiempo real y recursos educativos.

### Específicos
- Caracterizar técnicas de phishing: email phishing, spear phishing, smishing, vishing
- Diseñar arquitectura funcional para detección de enlaces sospechosos
- Implementar alertas preventivas durante la navegación
- Incorporar módulo educativo con gamificación
- Evaluar usabilidad y efectividad del sistema

---

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE (Web / Android)                   │
│  React + TypeScript + TailwindCSS  │  Kotlin + MVVM         │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTPS / JWT
┌─────────────────────▼───────────────────────────────────────┐
│                  BACKEND (Spring Boot)                        │
│  REST API │ Spring Security │ JWT │ Flyway │ Swagger         │
│                                                               │
│  Controllers ─► Services ─► Repositories ─► Entities        │
│                    │                                          │
│           ┌────────┼────────┐                                │
│           ▼        ▼        ▼                                │
│    Google Safe  VirusTotal  Heuristic                        │
│    Browsing API    API      Engine                           │
└─────────────────────┬───────────────────────────────────────┘
                      │ JPA / Hibernate
┌─────────────────────▼───────────────────────────────────────┐
│                   PostgreSQL Database                         │
│  users │ roles │ url_analysis │ threats │ alerts            │
│  educational_content │ quiz │ quiz_results │ audit_logs     │
└─────────────────────────────────────────────────────────────┘
```

### Decisiones Arquitectónicas

| Decisión | Justificación |
|----------|---------------|
| **Clean Architecture + MVC** | Separación clara de responsabilidades, testabilidad, mantenibilidad |
| **JWT Stateless** | Escalabilidad horizontal, sin estado en servidor, compatible con mobile |
| **Flyway Migrations** | Control de versiones del esquema de BD, reproducibilidad |
| **APIs combinadas (GSB + VT)** | Mayor precisión al combinar múltiples fuentes de inteligencia |
| **WebClient (Reactivo)** | Llamadas no bloqueantes a APIs externas, mejor throughput |
| **PostgreSQL + JSONB** | ACID + almacenamiento flexible de respuestas crudas de APIs |
| **TailwindCSS** | Design system consistente, dark mode nativo, bundle mínimo |
| **Hilt (Android DI)** | DI oficial de Google, reduce boilerplate, testabilidad |

---

## Tecnologías

### Backend
| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.2.5 | Framework web |
| Spring Security | 6.x | Autenticación/Autorización |
| Spring WebFlux | 6.x | Clientes HTTP reactivos |
| Hibernate/JPA | 6.x | ORM |
| Flyway | 10.x | Migraciones de BD |
| jjwt | 0.12.5 | Tokens JWT |
| MapStruct | 1.5.5 | Mapeo de objetos |
| Lombok | Latest | Reducción de boilerplate |
| SpringDoc | 2.5.0 | Documentación Swagger |
| JUnit 5 | 5.x | Testing |
| Mockito | Latest | Mocks en tests |

### Frontend
| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| React | 18.3 | UI Framework |
| TypeScript | 5.4 | Tipado estático |
| Vite | 5.3 | Build tool |
| TailwindCSS | 3.4 | Estilos |
| React Router | 6.23 | Enrutamiento |
| TanStack Query | 5.45 | Server state |
| Axios | 1.7 | HTTP client |
| Recharts | 2.12 | Gráficas |
| Lucide React | 0.395 | Iconos |
| Vitest | 1.6 | Testing |

### Android
| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Kotlin | 1.9 | Lenguaje |
| Android SDK | 34 | Plataforma |
| MVVM + LiveData | Architecture | Patrón de arquitectura |
| Hilt | 2.51 | Inyección de dependencias |
| Retrofit | 2.11 | HTTP client |
| OkHttp | 4.12 | HTTP core |
| Coroutines | 1.8 | Concurrencia asíncrona |
| DataStore | 1.1 | Almacenamiento local |
| Navigation Component | 2.7 | Navegación |

### Base de Datos
| Tecnología | Propósito |
|-----------|-----------|
| PostgreSQL 16 | BD principal |
| JSONB | Respuestas crudas de APIs |
| Flyway | Versionado de esquema |

---

## Estructura del Proyecto

```
PHISWARE/
├── phishware-backend/                    # API Spring Boot
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/phishware/
│       │   │   ├── PhishwareApplication.java
│       │   │   ├── config/               # SecurityConfig, CorsConfig, SwaggerConfig, WebClientConfig
│       │   │   ├── controller/           # AuthController, UrlAnalysisController, DashboardController...
│       │   │   ├── service/              # AuthService, UrlAnalysisService, GoogleSafeBrowsingService...
│       │   │   ├── repository/           # UserRepository, UrlAnalysisRepository...
│       │   │   ├── entity/               # User, Role, UrlAnalysis, Threat, Alert...
│       │   │   │   └── enums/            # RiskLevel, AnalysisSource, Severity
│       │   │   ├── dto/
│       │   │   │   ├── request/          # LoginRequest, RegisterRequest, UrlAnalysisRequest...
│       │   │   │   └── response/         # AuthResponse, UrlAnalysisResponse, DashboardResponse...
│       │   │   ├── security/             # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
│       │   │   ├── mapper/               # Interfaces MapStruct
│       │   │   └── exception/            # GlobalExceptionHandler, ResourceNotFoundException...
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_schema.sql
│       │           └── V2__insert_initial_data.sql
│       └── test/                         # JUnit + Mockito tests
│
├── phishware-frontend/                   # React + TypeScript
│   ├── src/
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   ├── index.css
│   │   ├── components/
│   │   │   ├── common/                   # AppLayout, Sidebar, Navbar, RiskBadge
│   │   │   ├── url-analysis/             # UrlInputForm, AnalysisResult
│   │   │   ├── dashboard/                # StatCard
│   │   │   └── education/
│   │   ├── pages/                        # LandingPage, LoginPage, RegisterPage, Dashboard...
│   │   ├── services/                     # api.ts, authService, urlAnalysisService...
│   │   ├── context/                      # AuthContext
│   │   ├── types/                        # auth.types, analysis.types, dashboard.types, education.types
│   │   └── routes/                       # AppRouter
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── tailwind.config.js
│
├── phishware-android/                    # Kotlin Android
│   └── app/src/main/java/com/phishware/android/
│       ├── PhishwareApp.kt               # Hilt Application
│       ├── data/
│       │   ├── model/                    # ApiModels.kt (DTOs)
│       │   ├── remote/                   # ApiService.kt, RetrofitClient.kt
│       │   ├── local/                    # SessionManager.kt (DataStore)
│       │   └── repository/               # PhishwareRepository.kt
│       ├── viewmodel/                    # AuthViewModel.kt, UrlAnalysisViewModel.kt
│       ├── ui/                           # Activities, Fragments
│       └── di/                           # AppModule.kt (Hilt)
│
└── docs/
    ├── architecture/
    ├── database/
    ├── diagrams/
    └── api/
```

---

## Modelo de Base de Datos

### Diagrama ER (Entidades y Relaciones)

```
users ─────────────── user_roles ─────────────── roles
  │                                                
  ├─── url_analysis ─── threats
  │        │
  │        └─── alerts
  │
  ├─── quiz_results ─── quiz ─── quiz_questions
  │                       │
  │               educational_content
  │
  ├─── user_badges ─── badges
  └─── audit_logs
```

### Tablas Principales

| Tabla | Descripción |
|-------|-------------|
| `users` | Usuarios del sistema con gamificación |
| `roles` | Roles: ROLE_USER, ROLE_ADMIN |
| `url_analysis` | Resultados de análisis de URLs |
| `threats` | Amenazas detectadas por análisis |
| `alerts` | Alertas de seguridad generadas |
| `educational_content` | Artículos y recursos educativos |
| `quiz` | Evaluaciones del módulo educativo |
| `quiz_questions` | Preguntas de los quizzes (JSONB) |
| `quiz_results` | Resultados de usuarios en quizzes |
| `badges` | Insignias del sistema de gamificación |
| `audit_logs` | Log de auditoría de acciones |

---

## API REST

Base URL: `http://localhost:8080/api`  
Documentación Swagger: `http://localhost:8080/api/swagger-ui.html`

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/auth/login` | Iniciar sesión | ❌ |
| POST | `/auth/register` | Registrar usuario | ❌ |

### Análisis de URLs

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/analysis` | Analizar una URL | ✅ |
| GET | `/analysis/history` | Historial paginado | ✅ |

### Dashboard

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/dashboard` | Estadísticas del usuario | ✅ |

### Alertas

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/alerts` | Listar alertas | ✅ |
| GET | `/alerts/unread-count` | Contar no leídas | ✅ |
| PATCH | `/alerts/{id}/read` | Marcar como leída | ✅ |
| PATCH | `/alerts/read-all` | Marcar todas leídas | ✅ |

### Módulo Educativo

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/education` | Listar contenido | ❌ |
| GET | `/education/{slug}` | Obtener artículo | ❌ |
| POST | `/education` | Crear contenido | ADMIN |
| PUT | `/education/{id}` | Actualizar | ADMIN |
| DELETE | `/education/{id}` | Eliminar | ADMIN |

### Ejemplos de Requests/Responses

**POST /auth/login**
```json
// Request
{
  "usernameOrEmail": "usuario@ejemplo.com",
  "password": "MiPass@123"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": 1,
  "username": "usuario",
  "email": "usuario@ejemplo.com",
  "roles": ["ROLE_USER"],
  "points": 0,
  "level": 1
}
```

**POST /analysis**
```json
// Request
{
  "url": "https://paypa1.com/login"
}

// Response 200
{
  "id": 42,
  "originalUrl": "https://paypa1.com/login",
  "domain": "paypa1.com",
  "riskLevel": "DANGEROUS",
  "riskScore": 85.5,
  "isPhishing": true,
  "threats": [
    {
      "threatType": "SOCIAL_ENGINEERING",
      "description": "Amenaza detectada por Google Safe Browsing API",
      "severity": "HIGH",
      "source": "GOOGLE_SAFE_BROWSING"
    }
  ],
  "riskMessage": "⛔ Este sitio es potencialmente peligroso.",
  "recommendations": [
    "No ingrese credenciales ni datos personales en este sitio.",
    "Cierre esta pestaña inmediatamente."
  ]
}
```

---

## Diagrama de Clases

```
┌──────────────┐       ┌─────────────────┐
│     User     │ 1───* │   UrlAnalysis   │
├──────────────┤       ├─────────────────┤
│ id           │       │ id              │
│ uuid         │       │ uuid            │
│ username     │       │ originalUrl     │
│ email        │       │ domain          │
│ password     │       │ riskLevel       │
│ points       │       │ riskScore       │
│ level        │       │ isPhishing      │
│ roles: Set   │       │ analysisSource  │
└──────┬───────┘       │ threats: List   │
       │               └────────┬────────┘
       │ 1───*                  │ 1───*
┌──────▼───────┐       ┌────────▼────────┐
│    Alert     │       │     Threat      │
├──────────────┤       ├─────────────────┤
│ title        │       │ threatType      │
│ message      │       │ description     │
│ alertType    │       │ severity        │
│ severity     │       │ source          │
│ isRead       │       └─────────────────┘
└──────────────┘

┌──────────────────┐       ┌───────────────┐
│ EducationalContent│ 1───* │     Quiz      │
├──────────────────┤       ├───────────────┤
│ title            │       │ title         │
│ slug             │       │ passingScore  │
│ category         │       │ pointsReward  │
│ content          │       │ questions:List│
│ difficulty       │       └───────┬───────┘
│ tags: List       │               │ 1───*
└──────────────────┘       ┌───────▼───────┐
                            │ QuizQuestion  │
                            ├───────────────┤
                            │ questionText  │
                            │ options: JSON │
                            │ correctAnswers│
                            └───────────────┘
```

---

## Diagrama de Casos de Uso

### Actor: Usuario
- Registrarse / Iniciar Sesión
- Analizar URL/Enlace
- Ver historial de análisis
- Recibir y gestionar alertas
- Leer contenido educativo
- Completar quizzes y ganar puntos
- Ver dashboard con estadísticas personales

### Actor: Administrador
- Gestionar usuarios (ver, activar/desactivar)
- Crear/editar/eliminar contenido educativo
- Gestionar quizzes y preguntas
- Ver estadísticas globales del sistema
- Consultar logs de auditoría

---

## Diagrama de Secuencia — Análisis de URL

```
Usuario       Frontend          Backend           Google Safe     VirusTotal
  │               │                │               Browsing API       API
  │──POST /analisis──►             │                    │              │
  │               │──POST /analysis/►                  │              │
  │               │                │──checkUrl()───────►              │
  │               │                │◄──SafeBrowsingResult──           │
  │               │                │──analyzeUrl()─────────────────────►
  │               │                │◄──VirusTotalResult────────────────
  │               │                │                    │              │
  │               │                │──calculateRisk()   │              │
  │               │                │──saveAnalysis()    │              │
  │               │                │──[if risky] createAlert()         │
  │               │                │──addPoints() (async)              │
  │               │◄──UrlAnalysisResponse─              │              │
  │◄──Result──────│                │                    │              │
```

---

## Instalación y Configuración

### Prerrequisitos
- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+
- Android Studio Hedgehog+

### 1. Base de Datos

```bash
# Crear base de datos
psql -U postgres
CREATE DATABASE phishware_db;
CREATE USER phishware WITH PASSWORD 'phishware2024';
GRANT ALL PRIVILEGES ON DATABASE phishware_db TO phishware;
\q
```

Las migraciones Flyway se ejecutan automáticamente al iniciar el backend.

### 2. Backend

```bash
cd phishware-backend

# Configurar variables de entorno (o editar application.yml)
export DB_USERNAME=phishware
export DB_PASSWORD=phishware2024
export JWT_SECRET=phishware-super-secret-key-min-256-bits-long
export GOOGLE_SAFE_BROWSING_KEY=tu_api_key
export VIRUSTOTAL_KEY=tu_api_key

# Ejecutar
mvn spring-boot:run

# La API estará disponible en:
# http://localhost:8080/api
# Swagger UI: http://localhost:8080/api/swagger-ui.html
```

### 3. Frontend

```bash
cd phishware-frontend

# Instalar dependencias
npm install

# Variables de entorno (opcional)
echo "VITE_API_URL=/api" > .env.local

# Modo desarrollo
npm run dev
# → http://localhost:5173

# Build producción
npm run build
```

### 4. Android

```bash
# Abrir en Android Studio
# File > Open > phishware-android/

# Para emulador: BASE_URL ya apunta a 10.0.2.2:8080 (host del emulador)
# Para dispositivo físico: cambiar en build.gradle DEBUG buildConfigField
```

---

## Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_USERNAME` | Usuario PostgreSQL | `phishware` |
| `DB_PASSWORD` | Contraseña PostgreSQL | `phishware2024` |
| `JWT_SECRET` | Clave secreta JWT (min 32 chars) | *ver application.yml* |
| `GOOGLE_SAFE_BROWSING_KEY` | API Key Google Safe Browsing | **Requerida** |
| `VIRUSTOTAL_KEY` | API Key VirusTotal | **Requerida** |

### Obtener API Keys

- **Google Safe Browsing**: [Google Cloud Console](https://console.cloud.google.com) → APIs & Services → Enable "Safe Browsing API"
- **VirusTotal**: [VirusTotal](https://www.virustotal.com) → Sign up → My API Key (tier gratuito: 500 req/día)

---

## Testing

### Backend (JUnit 5 + Mockito)

```bash
cd phishware-backend
mvn test

# Con cobertura
mvn test jacoco:report
# Reporte: target/site/jacoco/index.html
```

**Tests incluidos:**
- `AuthServiceTest` — Registro, login, validaciones
- `UrlAnalysisServiceTest` — Análisis URL, clasificación de riesgo, detección de amenazas

### Frontend (Vitest)

```bash
cd phishware-frontend
npm test

# Con cobertura
npm run test:coverage
```

### Android (JUnit 4 + Mockito-Kotlin)

```bash
cd phishware-android
./gradlew test

# Tests instrumentados (requiere emulador)
./gradlew connectedAndroidTest
```

---

## Seguridad Implementada

| Capa | Mecanismo | Descripción |
|------|-----------|-------------|
| Autenticación | JWT (HS256) | Tokens sin estado, expiración configurable |
| Contraseñas | BCrypt (cost=12) | Hash unidireccional resistente a fuerza bruta |
| Autorización | Spring Security + @PreAuthorize | RBAC por endpoints y métodos |
| Validación | Bean Validation (@Valid) | Validación en capa de entrada |
| XSS | HttpOnly cookies + CSP | Sanitización en frontend |
| SQL Injection | JPA/Hibernate parameterizado | Sin SQL concatenado |
| CORS | Orígenes permitidos explícitos | No wildcard en producción |
| Auditoría | AuditLog entity | Registro asíncrono de todas las acciones |
| HTTPS | SSL/TLS en producción | Certificado requerido en release |
| Rate Limiting | Recomendado: Spring Rate Limiter | Implementar en producción |

---

## Funcionalidades

### Implementadas
- [x] Análisis de URL con Google Safe Browsing API
- [x] Análisis con VirusTotal API
- [x] Cálculo combinado de score de riesgo (0-100)
- [x] Clasificación: SAFE / SUSPICIOUS / DANGEROUS
- [x] Generación automática de alertas
- [x] Sistema de gamificación (puntos + niveles)
- [x] Módulo educativo con 5 artículos iniciales
- [x] Sistema de quizzes con validación de respuestas
- [x] Historial de análisis paginado
- [x] Dashboard con estadísticas y gráficas
- [x] Autenticación JWT completa
- [x] RBAC (USER / ADMIN)
- [x] Log de auditoría asíncrono
- [x] Swagger/OpenAPI documentado
- [x] App Android con MVVM + Hilt

### Futuras mejoras
- [ ] Rate limiting por usuario
- [ ] Notificaciones push (Android)
- [ ] Extensión de navegador Chrome/Firefox
- [ ] Detección heurística local (ML)
- [ ] Dashboard admin con gráficas globales
- [ ] Sistema de reportes de amenazas comunitario

---

## Créditos y Licencia

**PHISHWARE** — Proyecto Académico-Profesional  
Arquitectura: Clean Architecture · MVC · SOLID  
Desarrollado con Java 21, Spring Boot 3, React 18, Kotlin

APIs utilizadas:
- Google Safe Browsing API — Google LLC
- VirusTotal API — VirusTotal (Google LLC)

---

*Generado como proyecto académico con fines educativos.*
