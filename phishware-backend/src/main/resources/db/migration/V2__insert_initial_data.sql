-- ============================================================
-- PHISHWARE - Datos Iniciales
-- ============================================================

-- Roles del sistema
INSERT INTO roles (name, description) VALUES
('ROLE_USER',  'Usuario estándar con acceso a análisis y módulo educativo'),
('ROLE_ADMIN', 'Administrador con acceso total al sistema');

-- Usuario administrador por defecto
-- password: Admin@phish2024 (BCrypt)
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_email_verified, points, level)
VALUES (
    'admin',
    'admin@phishware.com',
    '$2a$12$D8HjH5jX0sQ1xKpLwfGOHuwNwOWb1kJTjqGqO5vM7w3QWlX4S3aQW',
    'Admin',
    'Phishware',
    TRUE,
    TRUE,
    1000,
    5
);

-- Asignar roles al admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name IN ('ROLE_USER', 'ROLE_ADMIN');

-- ============================================================
-- Badges del sistema
-- ============================================================
INSERT INTO badges (name, description, badge_type, condition_type, condition_value, points_reward) VALUES
('Primer Análisis',    'Realizaste tu primer análisis de URL',             'ANALYSIS',   'TOTAL_ANALYSES',     1,   25),
('Detector Activo',    'Has analizado 10 URLs',                            'ANALYSIS',   'TOTAL_ANALYSES',     10,  50),
('Guardián Digital',   'Has analizado 50 URLs',                            'ANALYSIS',   'TOTAL_ANALYSES',     50,  100),
('Centinela Web',      'Has analizado 100 URLs',                           'ANALYSIS',   'TOTAL_ANALYSES',     100, 200),
('Primer Estudio',     'Leíste tu primer artículo educativo',              'EDUCATION',  'ARTICLES_READ',      1,   25),
('Estudiante',         'Leíste 5 artículos educativos',                    'EDUCATION',  'ARTICLES_READ',      5,   50),
('Phish Hunter',       'Detectaste tu primera amenaza real',               'ANALYSIS',   'THREATS_DETECTED',   1,   75),
('Cazador Elite',      'Detectaste 10 amenazas reales',                    'ANALYSIS',   'THREATS_DETECTED',   10,  150),
('Quiz Novato',        'Completaste tu primer quiz',                       'QUIZ',       'QUIZZES_COMPLETED',  1,   30),
('Quiz Master',        'Completaste 5 quizzes con puntuación perfecta',    'QUIZ',       'PERFECT_QUIZZES',    5,   200),
('Racha de 7 días',    'Usaste la aplicación 7 días seguidos',             'STREAK',     'LOGIN_STREAK_DAYS',  7,   100),
('Experto en Seguridad','Alcanzaste nivel 10',                             'SPECIAL',    'USER_LEVEL',         10,  500);

-- ============================================================
-- Contenido educativo inicial
-- ============================================================
INSERT INTO educational_content (title, slug, category, difficulty, reading_time_min, summary, content, tags, is_published)
VALUES
(
    '¿Qué es el Phishing? Guía Completa',
    'que-es-el-phishing-guia-completa',
    'PHISHING_BASICS',
    'BEGINNER',
    8,
    'Aprende qué es el phishing, cómo funciona y por qué es una de las amenazas más peligrosas en Internet.',
    E'# ¿Qué es el Phishing?\n\nEl **phishing** es una técnica de ataque cibernético donde los atacantes se hacen pasar por entidades confiables para engañar a las víctimas y obtener información sensible como contraseñas, datos bancarios o información personal.\n\n## ¿Cómo funciona?\n\nEl proceso típico de un ataque de phishing sigue estos pasos:\n\n1. **Preparación**: El atacante crea un sitio web o correo electrónico falso que imita a una organización legítima.\n2. **Distribución**: Envía el enlace malicioso masivamente a posibles víctimas.\n3. **Engaño**: La víctima hace clic creyendo que es legítimo.\n4. **Robo**: La víctima ingresa sus credenciales en el sitio falso.\n5. **Explotación**: El atacante usa la información robada.\n\n## Tipos de Phishing\n\n### Email Phishing\nLa forma más común. Correos masivos con urgencia falsa.\n\n### Spear Phishing\nAtaques dirigidos a personas o empresas específicas usando información personalizada.\n\n### Clone Phishing\nReplica exacta de correos legítimos con enlaces modificados.\n\n## Señales de Alerta\n\n- URLs con errores ortográficos (paypa1.com, amaz0n.com)\n- Urgencia artificial ("Tu cuenta será suspendida en 24h")\n- Solicitudes de información sensible por correo\n- Remitente desconocido o sospechoso\n- Adjuntos inesperados\n\n## ¿Cómo protegerte?\n\n✅ Verifica siempre la URL antes de ingresar datos\n✅ Usa autenticación de dos factores (2FA)\n✅ Mantén actualizado tu antivirus\n✅ Reporta correos sospechosos\n✅ Usa un gestor de contraseñas',
    ARRAY['phishing', 'seguridad', 'básico', 'correo electrónico'],
    TRUE
),
(
    'Ingeniería Social: El Arte del Engaño',
    'ingenieria-social-arte-del-engano',
    'SOCIAL_ENGINEERING',
    'INTERMEDIATE',
    10,
    'Explora las técnicas de manipulación psicológica usadas por atacantes para obtener acceso no autorizado.',
    E'# Ingeniería Social\n\nLa **ingeniería social** es la manipulación psicológica de personas para que realicen acciones o divulguen información confidencial. Es el "hackeo humano".\n\n## Principios Psicológicos Explotados\n\n### 1. Autoridad\nLos atacantes se hacen pasar por figuras de autoridad: gerentes, técnicos de TI, empleados del banco.\n\n### 2. Urgencia\nCrean situaciones de pánico: "Tu cuenta será bloqueada ahora mismo".\n\n### 3. Reciprocidad\nOfrecen algo de valor primero para luego pedir algo a cambio.\n\n### 4. Escasez\n"Solo quedan 2 plazas disponibles, actúa ya."\n\n### 5. Prueba Social\n"Miles de usuarios ya lo hacen, únete."\n\n## Técnicas Comunes\n\n### Pretexting\nCrear una historia elaborada para justificar una solicitud de información.\n\n### Baiting\nDejar USB infectados en lugares públicos esperando que alguien los conecte.\n\n### Quid Pro Quo\nOferta de servicio a cambio de información.\n\n### Tailgating\nAcceso físico siguiendo a alguien autorizado.\n\n## Defensa\n\n- Verificar identidades antes de dar información\n- No actuar bajo presión emocional\n- Seguir protocolos de seguridad establecidos\n- Capacitación continua del equipo',
    ARRAY['ingeniería social', 'manipulación', 'psicología', 'ataques'],
    TRUE
),
(
    'Smishing: Phishing por SMS',
    'smishing-phishing-por-sms',
    'SMISHING',
    'BEGINNER',
    6,
    'Descubre cómo los atacantes usan mensajes de texto para robar información y cómo protegerte.',
    E'# Smishing: Phishing por SMS\n\n**Smishing** = SMS + Phishing. Ataques de phishing realizados a través de mensajes de texto.\n\n## ¿Por qué es efectivo?\n\n- Las personas confían más en SMS que en emails\n- Los smartphones tienen tasas de apertura del 98%\n- URLs cortas ocultan el destino real\n- Menos filtros de spam que el correo\n\n## Ejemplos Reales\n\n### Bancos Falsos\n"ALERTA: Su tarjeta ha sido bloqueada. Verifique en: bit.ly/xyz"\n\n### Paquetes de Entrega\n"Su paquete no pudo entregarse. Reprograme aquí: [link]"\n\n### Premios Falsos\n"¡Ganó un iPhone! Reclame en 24 horas: [link]"\n\n## Señales de Smishing\n\n- Números desconocidos o extraños\n- URLs acortadas que no puedes verificar\n- Urgencia exagerada\n- Errores gramaticales\n- Solicitudes de datos personales\n\n## Protección\n\n✅ Nunca hagas clic en links de SMS no solicitados\n✅ Llama directamente a la organización\n✅ Usa aplicaciones bancarias oficiales\n✅ Activa el filtro de spam en tu teléfono',
    ARRAY['smishing', 'sms', 'móvil', 'phishing'],
    TRUE
),
(
    'Vishing: Phishing por Voz',
    'vishing-phishing-por-voz',
    'VISHING',
    'BEGINNER',
    7,
    'Conoce los ataques de phishing realizados mediante llamadas telefónicas y cómo identificarlos.',
    E'# Vishing: Phishing por Voz\n\n**Vishing** = Voice + Phishing. Fraudes realizados mediante llamadas telefónicas.\n\n## Cómo Funciona\n\nEl atacante llama haciéndose pasar por:\n- Técnico del banco\n- Soporte de Microsoft o Apple\n- Empleado del gobierno\n- Representante de una empresa\n\n## Técnicas de Vishing\n\n### Caller ID Spoofing\nFalsificación del número de origen para que parezca legítimo.\n\n### Robo de Autenticación\n"Le llamamos para verificar su identidad, dígame el código que recibió por SMS."\n\n### Miedo a Consecuencias\n"Su número de seguro social ha sido comprometido, necesitamos actuar ahora."\n\n## Señales de Alerta\n\n- Llamadas no solicitadas de "soporte técnico"\n- Solicitud de códigos de verificación\n- Urgencia y amenazas\n- Petición de acceso remoto a tu dispositivo\n- Solicitud de pago en métodos inusuales (gift cards, criptomonedas)\n\n## Defensa\n\n- Colgar y llamar al número oficial\n- Nunca compartir códigos 2FA por teléfono\n- No dar acceso remoto a llamadas no solicitadas\n- Registrar el número en listas de no llamar',
    ARRAY['vishing', 'llamadas', 'fraude telefónico', 'voz'],
    TRUE
),
(
    '10 Buenas Prácticas de Ciberseguridad',
    '10-buenas-practicas-ciberseguridad',
    'BEST_PRACTICES',
    'BEGINNER',
    9,
    'Las 10 acciones fundamentales que todo usuario debe implementar para navegar seguro en Internet.',
    E'# 10 Buenas Prácticas de Ciberseguridad\n\n## 1. Contraseñas Fuertes y Únicas\nUsa contraseñas de mínimo 12 caracteres combinando letras, números y símbolos. Una contraseña diferente por cada servicio.\n\n## 2. Autenticación de Dos Factores (2FA)\nActiva 2FA en todas tus cuentas importantes. Incluso si roban tu contraseña, no podrán acceder.\n\n## 3. Actualizaciones Constantes\nMantén actualizados tu sistema operativo, navegador y aplicaciones. Los parches corrigen vulnerabilidades.\n\n## 4. Gestor de Contraseñas\nUsa herramientas como Bitwarden o 1Password para generar y almacenar contraseñas seguras.\n\n## 5. VPN en Redes Públicas\nEn Wi-Fi públicas, usa una VPN para cifrar tu tráfico.\n\n## 6. Verificar URLs Antes de Clic\nRevisa siempre la URL completa. Un carácter cambiado puede llevarte a un sitio malicioso.\n\n## 7. No Abrir Adjuntos Sospechosos\nLos archivos adjuntos pueden contener malware. Verifica el remitente antes de abrir.\n\n## 8. Copias de Seguridad\nRealiza backups regulares. El ransomware pierde poder si tienes copias de tus datos.\n\n## 9. Privacidad en Redes Sociales\nLimita la información personal pública. Los atacantes usan OSINT para ataques dirigidos.\n\n## 10. Educación Continua\nLa amenaza evoluciona constantemente. Mantente informado sobre nuevas técnicas de ataque.',
    ARRAY['buenas prácticas', 'contraseñas', '2FA', 'seguridad básica'],
    TRUE
);

-- ============================================================
-- Quizzes iniciales
-- ============================================================
INSERT INTO quiz (title, description, difficulty, time_limit_sec, passing_score, points_reward, is_active)
VALUES
(
    'Fundamentos de Phishing',
    'Evalúa tus conocimientos básicos sobre phishing y cómo identificarlo.',
    'BEGINNER',
    300,
    70,
    50,
    TRUE
),
(
    'Ingeniería Social Avanzada',
    'Pon a prueba tu comprensión de las técnicas avanzadas de ingeniería social.',
    'INTERMEDIATE',
    420,
    75,
    100,
    TRUE
);

-- Preguntas Quiz 1
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type::VARCHAR, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q, (VALUES
    (
        '¿Cuál de los siguientes es el indicador MÁS común de un correo de phishing?',
        'SINGLE_CHOICE',
        '{"A": "El correo tiene un logotipo de empresa", "B": "El remitente usa urgencia exagerada y pide datos personales", "C": "El correo está en español", "D": "El correo tiene imágenes"}',
        '{"correct": ["B"]}',
        'Los ataques de phishing frecuentemente usan urgencia artificial para presionar a la víctima a actuar sin pensar.',
        10, 1
    ),
    (
        '¿Qué es el "spear phishing"?',
        'SINGLE_CHOICE',
        '{"A": "Phishing enviado masivamente a millones de personas", "B": "Phishing dirigido específicamente a un individuo u organización usando información personalizada", "C": "Phishing realizado por SMS", "D": "Phishing realizado por llamada telefónica"}',
        '{"correct": ["B"]}',
        'El spear phishing es altamente dirigido y personalizado, lo que lo hace más peligroso que el phishing masivo.',
        10, 2
    ),
    (
        '¿Cuál de estas URLs podría ser un intento de phishing de PayPal?',
        'SINGLE_CHOICE',
        '{"A": "https://www.paypal.com/login", "B": "https://www.paypa1.com/login", "C": "https://paypal.com/security", "D": "https://api.paypal.com/v1"}',
        '{"correct": ["B"]}',
        'La URL usa el número "1" en lugar de la letra "l" en paypal. Este typosquatting es una técnica clásica de phishing.',
        10, 3
    ),
    (
        '¿Qué significa "smishing"?',
        'SINGLE_CHOICE',
        '{"A": "Phishing a través de correo electrónico", "B": "Phishing a través de mensajes SMS", "C": "Phishing a través de llamadas de voz", "D": "Phishing a través de redes sociales"}',
        '{"correct": ["B"]}',
        'Smishing combina SMS + Phishing. Es especialmente peligroso porque las personas confían más en los mensajes de texto.',
        10, 4
    ),
    (
        '¿Cuáles son señales de alerta de un sitio web de phishing? (Selecciona todas las correctas)',
        'MULTIPLE_CHOICE',
        '{"A": "URL con errores ortográficos", "B": "Certificado SSL válido", "C": "Formularios que piden contraseñas por correo", "D": "Diseño idéntico al sitio original", "E": "Urgencia para actuar inmediatamente"}',
        '{"correct": ["A", "C", "E"]}',
        'Los sitios de phishing usan typosquatting, solicitan credenciales inapropiadamente y crean urgencia artificial. Un SSL válido ya no garantiza legitimidad.',
        20, 5
    )
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE q.title = 'Fundamentos de Phishing';

-- Preguntas Quiz 2
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type::VARCHAR, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q, (VALUES
    (
        '¿Qué principio psicológico explotan los atacantes cuando dicen "Solo tienes 10 minutos para reclamar tu premio"?',
        'SINGLE_CHOICE',
        '{"A": "Autoridad", "B": "Prueba social", "C": "Escasez y urgencia", "D": "Reciprocidad"}',
        '{"correct": ["C"]}',
        'La escasez y urgencia artificial impiden que la víctima piense críticamente y la impulsan a actuar de inmediato.',
        15, 1
    ),
    (
        '¿Qué es el "pretexting" en ingeniería social?',
        'SINGLE_CHOICE',
        '{"A": "Enviar correos con textos falsos", "B": "Crear una identidad o historia ficticia para manipular a la víctima", "C": "Publicar información falsa en redes sociales", "D": "Interceptar comunicaciones de texto"}',
        '{"correct": ["B"]}',
        'El pretexting implica construir un escenario creíble (pretexto) para ganar la confianza de la víctima y obtener información.',
        15, 2
    ),
    (
        '¿El "baiting" en ingeniería social se refiere a?',
        'SINGLE_CHOICE',
        '{"A": "Usar cebos físicos o digitales como USBs infectados para comprometer sistemas", "B": "Enviar mensajes de texto maliciosos", "C": "Falsificar el número de llamada entrante", "D": "Clonar sitios web bancarios"}',
        '{"correct": ["A"]}',
        'El baiting usa la curiosidad humana, como dejar USBs infectados en lugares estratégicos esperando que alguien los conecte.',
        15, 3
    ),
    (
        '¿Cuál es la mejor defensa contra un ataque de vishing (phishing por voz)?',
        'SINGLE_CHOICE',
        '{"A": "Dar solo los últimos 4 dígitos de tu tarjeta", "B": "Pedir el nombre del agente y seguir la llamada", "C": "Colgar y llamar directamente al número oficial de la organización", "D": "Verificar que la llamada sea de un número local"}',
        '{"correct": ["C"]}',
        'Siempre terminar la llamada y contactar a la organización por canales oficiales verificados es la defensa más efectiva.',
        15, 4
    )
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE q.title = 'Ingeniería Social Avanzada';
