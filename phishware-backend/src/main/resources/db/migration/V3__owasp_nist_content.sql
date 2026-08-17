-- =============================================================================
-- PHISHWARE — Migración V3: Contenido Educativo OWASP Top 10 + NIST CSF
-- =============================================================================
-- OWASP A09:2021 — Logging and Monitoring: el módulo educativo es parte del
-- plan de respuesta NIST CSF RC.IM-2 (Recovery improvements based on learnings)
-- =============================================================================

-- Ampliar categorías permitidas para incluir contenido OWASP y NIST
ALTER TABLE educational_content DROP CONSTRAINT educational_content_category_check;
ALTER TABLE educational_content ADD CONSTRAINT educational_content_category_check
    CHECK (category IN ('PHISHING_BASICS', 'SOCIAL_ENGINEERING', 'SMISHING', 'VISHING', 'BEST_PRACTICES', 'CASE_STUDIES', 'TOOLS', 'OWASP', 'NIST'));

-- ─────────────────────────────────────────────────────────────────────────────
-- SECCIÓN 1: Artículos educativos OWASP Top 10 aplicado a phishing
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO educational_content (title, slug, content, category, difficulty, reading_time_min, tags, is_published, created_at, updated_at) VALUES
(
  'OWASP Top 10: Las Amenazas Web que Facilitan el Phishing',
  'owasp-top-10-amenazas-web-phishing',
  '## ¿Qué es OWASP Top 10?

El **Open Web Application Security Project (OWASP)** publica cada pocos años la lista de las 10 vulnerabilidades más críticas en aplicaciones web. Los atacantes de phishing explotan estas mismas vulnerabilidades para hacer sus ataques más convincentes y difíciles de detectar.

## Cómo OWASP se relaciona con el phishing

### A01 — Broken Access Control
Los sitios de phishing frecuentemente imitan portales donde el **control de acceso roto** les permite escalar privilegios. Si un banco real tiene esta vulnerabilidad, los atacantes la explotan para redirigir usuarios a páginas falsas.

**Ejemplo:** Un correo que dice "Su cuenta fue comprometida, acceda AQUÍ para verificar" lleva a una URL similar a la del banco real pero con un subdominio diferente.

### A03 — Injection (Inyección)
Los atacantes usan técnicas de inyección para modificar URLs legítimas y redirigir a sitios falsos. Los **parámetros de consulta manipulados** pueden ejecutar código en el navegador del usuario.

**Ejemplo:** `https://banco-real.com/redirect?url=https://banco-falso.xyz`

### A05 — Security Misconfiguration
Servidores mal configurados permiten que atacantes alojen contenido fraudulento. La ausencia de cabeceras de seguridad como `X-Frame-Options` permite ataques de **clickjacking** que engañan visualmente al usuario.

### A07 — Authentication Failures
El phishing es, en esencia, un ataque dirigido a robar credenciales de autenticación. Los sitios falsos imitan exactamente las páginas de login para capturar usuario y contraseña.

**Señales de alerta:**
- La URL no coincide exactamente con el dominio oficial
- El certificado SSL es de una entidad desconocida o no existe
- La página pide más datos de los habituales

### A10 — Server-Side Request Forgery (SSRF)
El SSRF permite a un atacante hacer que el servidor víctima realice peticiones HTTP a recursos internos. En el contexto del phishing, se usa para exfiltrar datos desde redes corporativas internas.

## Cómo PHISHWARE te protege

PHISHWARE implementa controles específicos para cada categoría OWASP:
- **Sanitización de entrada** (A03) en cada URL analizada
- **Validación de esquemas** que bloquea redirecciones abiertas (A01)
- **Rate limiting** para prevenir abuso masivo (A04)
- **Cabeceras de seguridad HTTP** en todas las respuestas (A05)
- **Bloqueo de SSRF** antes de llamar a APIs externas (A10)

## Checklist de seguridad personal

- [ ] Verifica que la URL empiece con `https://` y el candado esté presente
- [ ] Comprueba que el dominio sea exactamente correcto (no variaciones)
- [ ] Desconfía de correos que crean urgencia ("su cuenta será suspendida")
- [ ] Nunca hagas clic en "olvidé contraseña" desde un enlace recibido por correo
- [ ] Usa un gestor de contraseñas: te alertará si la URL no coincide',
  'OWASP',
  'INTERMEDIATE',
  12,
  '{"owasp", "top10", "phishing", "seguridad-web", "vulnerabilidades"}',
  true,
  NOW(),
  NOW()
),
(
  'OWASP A03: Inyección y Phishing — Cómo los Atacantes Manipulan URLs',
  'owasp-a03-inyeccion-phishing-manipulacion-urls',
  '## Inyección en el contexto del phishing

La **inyección** ocupa el tercer lugar en OWASP Top 10:2021. En el contexto del phishing, los atacantes usan técnicas de inyección de múltiples maneras para engañar tanto a usuarios como a sistemas de seguridad.

## Tipos de inyección usados en phishing

### 1. Inyección en parámetros de URL
Los atacantes modifican parámetros de redirección en URLs legítimas:

```
https://www.google.com/url?q=https://phishing-site.xyz
https://l.facebook.com/l.php?u=https://fake-login.com
```

Los usuarios ven "google.com" y confían en el enlace, sin notar la redirección al final.

### 2. HTML Injection en correos electrónicos
El cuerpo del correo contiene HTML inyectado que muestra una URL diferente a la real del enlace:

```html
<a href="https://sitio-malicioso.xyz">https://banco-oficial.com</a>
```

El usuario ve `banco-oficial.com` pero el enlace lleva al sitio malicioso.

### 3. Inyección de Unicode (Homograph Attack)
Los atacantes usan caracteres Unicode visualmente idénticos a letras latinas:

| Lo que ves | Lo real | Diferencia |
|------------|---------|------------|
| paypal.com | pаypal.com | La "а" es cirílica (U+0430) |
| apple.com | аpple.com | La "а" es cirílica |
| microsoft.com | micrоsoft.com | La "о" es cirílica (U+043E) |

### 4. Subdomain Injection
```
https://secure-login.paypal.com.attacker.xyz/verify
```
El navegador resuelve el dominio desde la derecha: el dominio real es `attacker.xyz`.

## Cómo identificar una URL inyectada

**Regla 1:** Lee la URL de derecha a izquierda, antes de la primera `/`
**Regla 2:** Verifica cada carácter en el dominio — especialmente letras similares
**Regla 3:** Pega el enlace en https://phishware.app antes de hacer clic
**Regla 4:** En correos, pasa el cursor sobre el enlace sin hacer clic

## Control técnico en PHISHWARE

```java
// OWASP A03: PHISHWARE bloquea patrones de inyección en URLs
owaspValidator.validateUrlForInjection(url);
// Detecta: SQL Injection, Command Injection, UNION SELECT, etc.
```

El servicio también detecta el ataque homograph mediante normalización Unicode antes del análisis.',
  'OWASP',
  'ADVANCED',
  10,
  '{"owasp", "inyeccion", "url-manipulation", "homograph", "phishing"}',
  true,
  NOW(),
  NOW()
),
(
  'OWASP A07: Fallos de Autenticación — El Objetivo Principal del Phishing',
  'owasp-a07-fallos-autenticacion-objetivo-phishing',
  '## Por qué la autenticación es el objetivo #1

OWASP A07:2021 — *Identification and Authentication Failures* describe exactamente lo que el phishing intenta explotar: **robar credenciales de autenticación**.

Según el informe Verizon DBIR 2023, el **83% de las brechas de datos** involucran credenciales robadas. El phishing es el método de robo número uno.

## Cómo funciona el robo de credenciales

### Fase 1 — Reconocimiento
El atacante identifica el servicio que quiere suplantar (banco, correo, red social) y estudia la página de login original.

### Fase 2 — Clonación
Herramientas como *SET (Social Engineering Toolkit)* o *Evilginx* pueden:
- Clonar la página de login pixel por pixel
- Actuar como proxy transparente interceptando credenciales en tiempo real
- Capturar incluso tokens MFA (Multi-Factor Authentication)

### Fase 3 — Distribución
La URL maliciosa se distribuye por:
- Correo electrónico (spear phishing personalizado)
- SMS (smishing)
- Mensajes directos en redes sociales
- Anuncios pagados en buscadores (SEO poisoning)

### Fase 4 — Captura
El usuario ingresa sus credenciales, el atacante las recibe en tiempo real y puede:
- Iniciar sesión inmediatamente antes de que el usuario note algo
- Cambiar contraseña y bloquear al usuario legítimo
- Exfiltrar datos sensibles

## NIST SP 800-63B: Cómo proteger tus credenciales

El estándar NIST para contraseñas recomienda:

| Recomendación NIST | Descripción |
|-------------------|-------------|
| Mínimo 8 caracteres | Para cuentas normales |
| Mínimo 15 caracteres | Para cuentas privilegiadas |
| Sin requisitos de complejidad arbitrarios | Favorece longitud sobre complejidad |
| Verificar contra listas negras | Bloquear contraseñas conocidas |
| No expirar periódicamente | Solo al detectar compromiso |

## Señales de un sitio de login falso

1. **URL diferente:** `login-paypal-secure.com` en lugar de `paypal.com`
2. **Sin HTTPS o certificado inválido**
3. **Solicita información adicional:** Número de teléfono, pregunta de seguridad, PIN
4. **Diseño ligeramente diferente:** Fuentes, colores, logos con pequeñas diferencias
5. **No hay autocompletado:** Los gestores de contraseñas no reconocen el dominio

## Tu plan de defensa

- **Activa MFA/2FA** en todas tus cuentas importantes
- Usa un **gestor de contraseñas** — no puede ser engañado por dominios falsos
- Configura **alertas de inicio de sesión** en tus cuentas
- Nunca uses la misma contraseña en múltiples sitios
- Considera una **llave de seguridad física** (YubiKey) para cuentas críticas',
  'OWASP',
  'BEGINNER',
  8,
  '{"owasp", "autenticacion", "credenciales", "mfa", "contrasenas"}',
  true,
  NOW(),
  NOW()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- SECCIÓN 2: Artículos educativos NIST Cybersecurity Framework
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO educational_content (title, slug, content, category, difficulty, reading_time_min, tags, is_published, created_at, updated_at) VALUES
(
  'NIST Cybersecurity Framework: Tu Marco de Defensa contra el Phishing',
  'nist-cybersecurity-framework-defensa-phishing',
  '## ¿Qué es el NIST Cybersecurity Framework?

El **National Institute of Standards and Technology (NIST)** publicó el Cybersecurity Framework (CSF) como guía voluntaria para que organizaciones de cualquier tamaño gestionen y reduzcan el riesgo de ciberseguridad.

El framework se organiza en **5 funciones core** que forman un ciclo continuo de mejora:

```
IDENTIFY → PROTECT → DETECT → RESPOND → RECOVER
    ↑                                        ↓
    └────────── (ciclo continuo) ────────────┘
```

## Las 5 Funciones Aplicadas al Phishing

### 🔍 IDENTIFY (Identificar) — ID
*"Desarrollar el entendimiento organizacional para gestionar el riesgo"*

En el contexto del phishing:
- **ID.AM:** Inventariar qué datos personales y corporativos podrían ser objetivo
- **ID.RA:** Evaluar el riesgo de recibir phishing por correo, SMS o llamadas
- **ID.SC:** Entender la cadena de suministro de comunicaciones digitales

**En PHISHWARE:** El motor heurístico clasifica cada URL según indicadores de riesgo antes de consultar APIs externas.

### 🛡️ PROTECT (Proteger) — PR
*"Implementar las salvaguardas apropiadas"*

- **PR.AC:** Controlar quién accede a tus sistemas y datos
- **PR.AT:** Capacitar al personal sobre phishing y ingeniería social
- **PR.DS:** Proteger los datos en tránsito y en reposo
- **PR.IP:** Implementar políticas de seguridad de la información

**En PHISHWARE:** Autenticación JWT, BCrypt para contraseñas, HTTPS, cabeceras de seguridad HTTP.

### 🔎 DETECT (Detectar) — DE
*"Desarrollar actividades para identificar la ocurrencia de un evento"*

- **DE.AE:** Detectar anomalías y eventos de seguridad
- **DE.CM:** Monitorear continuamente actividad sospechosa
- **DE.DP:** Mantener procesos de detección actualizados

**En PHISHWARE:** Consulta simultánea a Google Safe Browsing y VirusTotal, análisis heurístico de 7 indicadores de phishing.

### 🚨 RESPOND (Responder) — RS
*"Desarrollar actividades para actuar cuando se detecta un incidente"*

- **RS.CO:** Comunicar el incidente a las partes apropiadas
- **RS.AN:** Analizar el incidente para entender el impacto
- **RS.MI:** Mitigar el impacto del incidente

**En PHISHWARE:** Generación automática de alertas, notificaciones en tiempo real, recomendaciones personalizadas.

### 🔄 RECOVER (Recuperar) — RC
*"Desarrollar actividades para mantener la resiliencia"*

- **RC.RP:** Ejecutar el plan de recuperación
- **RC.IM:** Mejorar basándose en lecciones aprendidas
- **RC.CO:** Coordinar la comunicación de recuperación

**En PHISHWARE:** Módulo educativo con gamificación — aprender de los incidentes para prevenir futuros ataques.

## ¿Por qué importa el NIST CSF?

1. **Lenguaje común:** Permite comunicar el riesgo entre equipos técnicos y directivos
2. **Escalable:** Aplica igualmente a individuos, PYMEs y grandes corporaciones
3. **No prescriptivo:** Describe QUÉ hacer, no exactamente CÓMO
4. **Internacionalmente reconocido:** Base de muchos estándares y regulaciones

## Tu plan de acción personal NIST

| Función | Acción personal |
|---------|----------------|
| IDENTIFY | Lista tus cuentas críticas (banco, correo, trabajo) |
| PROTECT | Activa MFA en todas, usa contraseñas únicas |
| DETECT | Instala PHISHWARE, revisa alertas de inicio de sesión |
| RESPOND | Si caíste en phishing: cambia contraseñas, avisa a tu banco |
| RECOVER | Aprende a identificar el tipo de ataque para evitar el siguiente |',
  'NIST',
  'INTERMEDIATE',
  15,
  '{"nist", "csf", "framework", "ciberseguridad", "gestion-riesgos"}',
  true,
  NOW(),
  NOW()
),
(
  'NIST SP 800-30: Evaluación de Riesgos de Phishing en tu Organización',
  'nist-sp-800-30-evaluacion-riesgos-phishing',
  '## ¿Qué es NIST SP 800-30?

La publicación especial **NIST SP 800-30 Rev. 1** — *Guide for Conducting Risk Assessments* — proporciona una guía para evaluar los riesgos de seguridad de la información. PHISHWARE usa esta metodología para calcular el score de riesgo de cada URL analizada.

## Los 4 pasos de la evaluación de riesgo

### Paso 1 — Preparación de la evaluación
Definir el contexto: ¿qué activos proteges? ¿cuál es tu perfil de amenaza?

Para phishing personal:
- Activos: cuentas bancarias, correo corporativo, redes sociales, datos de clientes
- Amenazas: spear phishing, whaling, smishing, vishing
- Vulnerabilidades: falta de MFA, contraseñas débiles, desconocimiento de señales

### Paso 2 — Ejecución de la evaluación
Identificar fuentes de amenaza y eventos. NIST SP 800-30 define:

**Likelihood (Probabilidad):**
| Nivel | Definición NIST | Ejemplo phishing |
|-------|----------------|-----------------|
| Muy alto | Casi certeza de ataque | Ejecutivo C-suite sin MFA |
| Alto | Alta probabilidad | Usuario con datos en brechas conocidas |
| Moderado | Cierta probabilidad | Usuario corporativo promedio |
| Bajo | Baja probabilidad | Usuario con buenas prácticas de seguridad |

**Impact (Impacto):**
| Nivel | Definición NIST | Ejemplo phishing |
|-------|----------------|-----------------|
| Muy alto | Daño severo o catastrófico | Robo de identidad completo |
| Alto | Daño significativo | Cuenta bancaria vaciada |
| Moderado | Daño moderado | Acceso a correo corporativo |
| Bajo | Daño limitado | Exposición de datos no sensibles |

### Paso 3 — Comunicación de resultados
El riesgo se calcula como:

```
Riesgo = Probabilidad × Impacto
```

**Cómo PHISHWARE implementa este cálculo:**
```
Score final = GSB_score(60%) + VT_score(30%) + Heuristic_score(10%)
```
Donde cada fuente representa una dimensión de probabilidad e impacto combinados.

### Paso 4 — Mantenimiento de la evaluación
El riesgo cambia constantemente. PHISHWARE actualiza el análisis consultando:
- Google Safe Browsing (actualizado cada 30 minutos por Google)
- VirusTotal (inteligencia colectiva de 70+ motores antivirus)
- Motor heurístico local (análisis inmediato sin dependencias externas)

## Matrices de riesgo PHISHWARE

```
         IMPACTO
         BAJO    MEDIO   ALTO    CRÍTICO
P  BAJO │  🟢  │  🟢  │  🟡  │  🟠  │
R MEDIO │  🟢  │  🟡  │  🟠  │  🔴  │
O  ALTO │  🟡  │  🟠  │  🔴  │  🔴  │
B CRIT. │  🟠  │  🔴  │  🔴  │  🔴  │
```

## Aplicación práctica en tu empresa

1. **Identifica activos críticos** — ¿qué información no puede ser robada?
2. **Mapea vectores de phishing** — correo, SMS, llamadas, USB, QR codes
3. **Evalúa controles actuales** — ¿tienes filtros de spam? ¿MFA? ¿capacitación?
4. **Calcula riesgo residual** — después de los controles, ¿cuánto riesgo queda?
5. **Documenta y revisa** — mínimo anualmente o tras un incidente

## Recursos adicionales

- [NIST SP 800-30 Rev.1](https://csrc.nist.gov/publications/detail/sp/800-30/rev-1/final)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [NIST SP 800-63B — Digital Identity](https://pages.nist.gov/800-63-3/sp800-63b.html)',
  'NIST',
  'ADVANCED',
  14,
  '{"nist", "sp800-30", "riesgo", "evaluacion", "metodologia"}',
  true,
  NOW(),
  NOW()
),
(
  'Buenas Prácticas OWASP y NIST: Tu Guía Completa de Ciberhigiene',
  'buenas-practicas-owasp-nist-ciberhigiene',
  '## Ciberhigiene: El concepto que une OWASP y NIST

La **ciberhigiene** es el conjunto de prácticas cotidianas de seguridad que, al igual que lavarse las manos, previenen la mayoría de los problemas antes de que ocurran.

OWASP define las vulnerabilidades técnicas que debes evitar; NIST define el proceso de gestión que debes seguir. Juntos forman una defensa completa.

## Las 10 prácticas fundamentales

### 1. Contraseñas fuertes y únicas (OWASP A07 + NIST PR.AC-1)
- Usa contraseñas de **mínimo 16 caracteres** (NIST recomienda longitud sobre complejidad)
- **Nunca reutilices** la misma contraseña en múltiples servicios
- Usa un **gestor de contraseñas**: Bitwarden (gratuito), 1Password, KeePass
- El gestor no puede ser engañado por dominios falsos — una defensa clave contra phishing

### 2. Autenticación multifactor (OWASP A07 + NIST PR.AC-7)
Activar MFA reduce el riesgo de compromiso en un **99.9%** (Microsoft, 2019):
- **Preferir:** Llaves de seguridad físicas (FIDO2/WebAuthn) > Apps TOTP > SMS
- Activa MFA en: correo, banco, redes sociales, trabajo, gestores de contraseñas

### 3. Verificar URLs antes de hacer clic (OWASP A03 + NIST DE.CM-1)
Antes de introducir credenciales, comprueba:
- [ ] Protocolo: `https://` (con S)
- [ ] Dominio exacto: `paypal.com` no `paypal-secure.com`
- [ ] Sin subdominios sospechosos: `secure.paypal.com` ✅ vs `paypal.com.login-secure.xyz` ❌
- [ ] Pega la URL en PHISHWARE si tienes dudas

### 4. Desconfía de la urgencia (NIST ID.RA-1)
Los ataques de phishing explotan el **sesgo cognitivo de urgencia**:
- "Su cuenta será suspendida en 24 horas"
- "Pago pendiente — confirme ahora"
- "Actividad sospechosa detectada — verifique inmediatamente"

**Regla de oro:** Si algo crea urgencia, espera 5 minutos. Los ataques reales no desaparecen si esperas.

### 5. Actualizar software regularmente (OWASP A06 + NIST PR.IP-12)
El 60% de las brechas explotan vulnerabilidades con parches disponibles:
- Activa **actualizaciones automáticas** en tu SO y navegador
- Mantén actualizado tu **antivirus y EDR**
- Actualiza las extensiones del navegador y aplicaciones instaladas

### 6. Copias de seguridad (NIST RC.RP-1)
Si caes en un ataque de ransomware distribuido por phishing:
- Regla **3-2-1:** 3 copias, 2 medios diferentes, 1 offsite/cloud
- Prueba regularmente que puedes restaurar desde el backup
- Los backups en cloud sin versioning NO te protegen del ransomware

### 7. Revisar permisos de aplicaciones (OWASP A01 + NIST PR.AC-3)
- Audita qué apps tienen acceso a tu correo y redes sociales
- Revoca acceso a apps que ya no usas (OAuth tokens)
- Usa el principio de **mínimo privilegio**: no otorgues más permisos de los necesarios

### 8. Separación de cuentas (NIST PR.AC-4)
- **No uses el mismo correo** para trabajo y personal
- Crea una cuenta de correo desechable para registros en sitios de terceros
- Usa **alias de correo** (SimpleLogin, Addy.io) para aislar brechas

### 9. Reportar ataques (OWASP A09 + NIST RS.CO-2)
Reportar no es solo para organizaciones:
- **Colombia:** incidentes@colcert.gov.co
- **España:** incidencias@incibe-cert.es
- **México:** cgsi@sspc.gob.mx
- Reporta URLs de phishing en: Google Safe Browsing, VirusTotal, PhishTank

### 10. Educación continua (NIST RC.IM-1)
El phishing evoluciona constantemente:
- Realiza los quizzes en PHISHWARE regularmente
- Comparte este conocimiento con familia y colegas
- Suscríbete a alertas de seguridad: CISA, INCIBE, COLCERT

## Checklist mensual de ciberhigiene

```
□ Revisar alertas de PHISHWARE de las últimas semanas
□ Verificar si tus datos están en brechas recientes (HaveIBeenPwned)
□ Auditar apps con acceso OAuth a tus cuentas
□ Revisar sesiones activas en cuentas críticas
□ Actualizar contraseñas de cuentas sin MFA
□ Completar un quiz de phishing en PHISHWARE
```',
  'NIST',
  'BEGINNER',
  11,
  '{"nist", "owasp", "buenas-practicas", "ciberhigiene", "guia-completa"}',
  true,
  NOW(),
  NOW()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- SECCIÓN 3: Quiz OWASP Top 10 aplicado a phishing
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO quiz (title, description, difficulty, time_limit_sec, passing_score, points_reward, is_active, created_at) VALUES
(
  'Quiz OWASP: ¿Conoces las Vulnerabilidades del Phishing?',
  'Evalúa tu conocimiento sobre cómo el OWASP Top 10 se relaciona con los ataques de phishing y las defensas que PHISHWARE implementa. Requiere haber leído los artículos OWASP.',
  'INTERMEDIATE',
  900,
  70,
  50,
  true,
  NOW()
);

-- Guardar ID del quiz recién creado
DO $$
DECLARE
  v_quiz_id BIGINT;
BEGIN
  SELECT id INTO v_quiz_id FROM quiz WHERE title = 'Quiz OWASP: ¿Conoces las Vulnerabilidades del Phishing?' ORDER BY id DESC LIMIT 1;

  -- Pregunta 1
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   '¿Cuál categoría de OWASP Top 10:2021 describe directamente el objetivo principal del phishing?',
   'SINGLE_CHOICE',
    '{"A": "A01 - Broken Access Control", "B": "A03 - Injection", "C": "A07 - Identification and Authentication Failures", "D": "A10 - Server-Side Request Forgery"}',
    '{"correct": ["C"]}',
    'OWASP A07 cubre el robo y fallo en la autenticación de identidades. El phishing busca exactamente eso: robar credenciales de autenticación (usuario y contraseña) para suplantar la identidad de la víctima.',
    10, 1);

  -- Pregunta 2
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   '¿Qué es un "homograph attack" en el contexto de phishing?',
   'SINGLE_CHOICE',
   '{"A": "Un ataque que usa imágenes para ocultar texto malicioso", "B": "Uso de caracteres Unicode visualmente idénticos para crear dominios falsos", "C": "Un ataque que copia exactamente el diseño visual de un sitio legítimo", "D": "Envío masivo del mismo correo de phishing a múltiples víctimas"}',
   '{"correct": ["B"]}',
   'Los ataques homograph usan caracteres de otros alfabetos (como cirílico) que son visualmente idénticos a letras latinas. Por ejemplo: "pаypal.com" donde la "а" es cirílica (U+0430), no latina. El dominio es diferente pero parece idéntico visualmente.',
    10, 2);

  -- Pregunta 3
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   '¿Cuál de las siguientes URLs es más probable que sea un ataque de phishing? (OWASP A03)',
   'SINGLE_CHOICE',
   '{"A": "https://paypal.com/es/signin", "B": "https://secure-paypal.com/login", "C": "https://www.paypal.com/signin?returnUrl=/home", "D": "https://developer.paypal.com/docs/api/"}',
   '{"correct": ["B"]}',
   'La URL "secure-paypal.com" es un dominio diferente a "paypal.com". El prefijo "secure-" es un truco común: el dominio real es "secure-paypal.com", no "paypal.com". Las otras tres URLs pertenecen legítimamente al dominio "paypal.com".',
    10, 3);

  -- Pregunta 4
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   '¿Qué categoría OWASP protege contra ataques donde el servidor es usado para escanear redes internas?',
   'SINGLE_CHOICE',
   '{"A": "A01 - Broken Access Control", "B": "A05 - Security Misconfiguration", "C": "A09 - Security Logging Failures", "D": "A10 - Server-Side Request Forgery (SSRF)"}',
   '{"correct": ["D"]}',
   'OWASP A10 cubre SSRF: ataques donde el atacante engaña al servidor para que realice peticiones HTTP a recursos internos (192.168.x.x, 169.254.169.254 para metadata de cloud, etc.). PHISHWARE bloquea este vector antes de consultar APIs externas.',
    10, 4);

  -- Pregunta 5
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   'Según NIST SP 800-63B, ¿cuál recomendación sobre contraseñas es INCORRECTA?',
   'SINGLE_CHOICE',
   '{"A": "Las contraseñas deben tener mínimo 8 caracteres", "B": "Se deben verificar contra listas negras de contraseñas comprometidas", "C": "Las contraseñas deben expirar obligatoriamente cada 90 días", "D": "Se deben permitir todos los caracteres ASCII y Unicode"}',
   '{"correct": ["C"]}',
   'NIST SP 800-63B específicamente recomienda NO forzar la expiración periódica de contraseñas. Este requisito lleva a los usuarios a elegir contraseñas predecibles (Password1, Password2...). NIST solo recomienda cambiar la contraseña cuando hay evidencia de compromiso.',
    10, 5);

END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- SECCIÓN 4: Quiz NIST Cybersecurity Framework
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO quiz (title, description, difficulty, time_limit_sec, passing_score, points_reward, is_active, created_at) VALUES
(
  'Quiz NIST CSF: Gestión del Riesgo de Phishing',
  'Evalúa tu comprensión del NIST Cybersecurity Framework y cómo sus 5 funciones (Identify, Protect, Detect, Respond, Recover) se aplican a la prevención y respuesta ante ataques de phishing.',
  'INTERMEDIATE',
  720,
  70,
  50,
  true,
  NOW()
);

DO $$
DECLARE
  v_quiz_id BIGINT;
BEGIN
  SELECT id INTO v_quiz_id FROM quiz WHERE title = 'Quiz NIST CSF: Gestión del Riesgo de Phishing' ORDER BY id DESC LIMIT 1;

  -- Pregunta 1
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   'Has recibido un correo sospechoso y copiaste la URL en PHISHWARE antes de hacer clic. ¿A qué función NIST CSF corresponde esta acción?',
   'SINGLE_CHOICE',
   '{"A": "IDENTIFY (ID) — Identificar activos y riesgos", "B": "PROTECT (PR) — Implementar salvaguardas", "C": "DETECT (DE) — Detectar eventos de seguridad", "D": "RESPOND (RS) — Responder a incidentes"}',
   '{"correct": ["C"]}',
   'Verificar activamente una URL antes de hacer clic es una acción de DETECCIÓN (DE). Estás usando una herramienta para detectar si la URL es un riesgo antes de exponerte. PROTECT sería tener filtros automáticos; IDENTIFY sería mapear los activos en riesgo.',
    10, 1);

  -- Pregunta 2
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   'Tu empresa decide activar MFA obligatorio para todos los empleados después de un incidente de phishing. ¿A qué función NIST CSF pertenece esta decisión?',
   'SINGLE_CHOICE',
   '{"A": "IDENTIFY — Catalogar activos de la organización", "B": "PROTECT — Implementar salvaguardas para servicios críticos", "C": "DETECT — Monitorear eventos de seguridad continúamente", "D": "RECOVER — Implementar mejoras post-incidente"}',
   '{"correct": ["B"]}',
   'Implementar MFA es una salvaguarda de PROTECCIÓN (PR.AC-7: Users, devices, and other assets are authenticated). PROTECT incluye todos los controles preventivos: control de acceso, capacitación, protección de datos e infraestructura.',
    10, 2);

  -- Pregunta 3
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   '¿Cuál es el orden correcto de las 5 funciones del NIST Cybersecurity Framework?',
   'SINGLE_CHOICE',
   '{"A": "Protect → Identify → Detect → Respond → Recover", "B": "Identify → Protect → Detect → Respond → Recover", "C": "Detect → Identify → Protect → Respond → Recover", "D": "Identify → Detect → Protect → Respond → Recover"}',
   '{"correct": ["B"]}',
   'El orden lógico del NIST CSF es: primero IDENTIFY (conocer qué proteger), luego PROTECT (implementar controles), DETECT (monitorear amenazas), RESPOND (actuar ante incidentes), y RECOVER (volver a la normalidad y mejorar). Forman un ciclo continuo.',
    10, 3);

  -- Pregunta 4
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   'Después de que un empleado cayó en un ataque de phishing, la empresa realiza una sesión de capacitación sobre el tipo de ataque. ¿Qué función NIST CSF aplica principalmente?',
   'SINGLE_CHOICE',
   '{"A": "DETECT (DE) — Detección de amenazas futuras", "B": "RESPOND (RS) — Respuesta al incidente actual", "C": "RECOVER (RC) — Mejora de capacidades post-incidente", "D": "IDENTIFY (ID) — Identificación de nuevos riesgos"}',
   '{"correct": ["C"]}',
   'La capacitación post-incidente corresponde a RECOVER, específicamente RC.IM-1 (Recovery plans incorporate lessons learned) y RC.IM-2 (Recovery strategies are updated). La organización aprende del incidente y mejora sus capacidades para el futuro.',
    10, 4);

  -- Pregunta 5
  INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index) VALUES
  (v_quiz_id,
   'Según NIST SP 800-30, ¿cómo se calcula el riesgo en una evaluación formal?',
   'SINGLE_CHOICE',
   '{"A": "Riesgo = Amenaza + Vulnerabilidad", "B": "Riesgo = Probabilidad × Impacto", "C": "Riesgo = Activo × Amenaza × Vulnerabilidad", "D": "Riesgo = Impacto ÷ Controles"}',
   '{"correct": ["B"]}',
   'NIST SP 800-30 define: Riesgo = f(Probabilidad, Impacto). La probabilidad incluye la likelihood de que una amenaza explote una vulnerabilidad; el impacto mide el daño resultante. PHISHWARE implementa esta fórmula combinando el score de GSB (60%), VirusTotal (30%) y heurística (10%).',
    10, 5);

END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- SECCIÓN 5: Badges para contenido OWASP + NIST
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO badges (name, description, icon_url, badge_type, condition_type, condition_value, points_reward) VALUES
(
  'Defensor OWASP',
  'Completaste todos los artículos educativos sobre OWASP Top 10 y aprobaste el quiz con más del 80%.',
  'shield-owasp',
  'EDUCATION',
  'QUIZ_SCORE_OWASP',
  80,
  100
),
(
  'Analista NIST',
  'Completaste todos los artículos sobre NIST Cybersecurity Framework y aprobaste el quiz con más del 80%.',
  'chart-nist',
  'EDUCATION',
  'QUIZ_SCORE_NIST',
  80,
  100
),
(
  'Maestro OWASP y NIST',
  'Completaste todo el contenido de OWASP y NIST. Eres un defensor avanzado contra el phishing.',
  'star-security',
  'SPECIAL',
  'BADGES_EARNED',
  2,
  200
);
