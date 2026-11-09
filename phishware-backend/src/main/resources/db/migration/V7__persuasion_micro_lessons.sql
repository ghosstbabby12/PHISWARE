-- ============================================================
-- V9: Microlecciones de principios de persuasión (una por principio de Cialdini)
-- Referenciadas por IndicatorEducationCatalog (threat -> microlección)
-- ============================================================

INSERT INTO educational_content (title, slug, category, difficulty, reading_time_min, summary, content, tags, is_published)
SELECT * FROM (VALUES
(
    'Autoridad: el disfraz favorito del phishing',
    'micro-principio-autoridad',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Aprende a reconocer cuándo un mensaje abusa de la autoridad para que obedezcas sin verificar.',
    E'# Autoridad: el disfraz favorito del phishing\n\nLos atacantes se hacen pasar por tu banco, tu jefe o una entidad oficial (DIAN, policía) porque tendemos a obedecer a la autoridad sin cuestionarla.\n\n## Señales\n\n- Mensajes que dicen ser de tu banco o de un superior.\n- Logos copiados y lenguaje formal.\n- Piden actuar ya, sin verificar por otro canal.\n\n## Qué hacer\n\n1. Verifica siempre por un canal oficial que tú mismo abras (app, web o teléfono del reverso de la tarjeta).\n2. Ningún banco te pide claves por correo, SMS o llamada.\n3. Si dudas, cierra el mensaje y contacta directamente a la entidad.',
    ARRAY['autoridad', 'Cialdini', 'microlección', 'verificación'],
    TRUE
),
(
    'Urgencia y escasez: decidir sin pensar',
    'micro-principio-escasez',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'La prisa es la herramienta del estafador: aprende a frenar antes de hacer clic.',
    E'# Urgencia y escasez: decidir sin pensar\n\n"Tu cuenta será bloqueada en 24 horas", "solo quedan 2 cupos": la urgencia y la escasez apagan tu pensamiento crítico.\n\n## Señales\n\n- Cuentas regresivas y amenazas de bloqueo.\n- Ofertas que "vencen hoy".\n- Presión para no consultar con nadie.\n\n## Qué hacer\n\n1. Respira: ninguna emergencia real se resuelve haciendo clic en un enlace.\n2. Verifica por el canal oficial, sin prisa.\n3. Un dominio registrado hace días con "ofertas urgentes" es casi siempre fraude.',
    ARRAY['urgencia', 'escasez', 'Cialdini', 'microlección'],
    TRUE
),
(
    'Simpatía y confianza: el anzuelo invisible',
    'micro-principio-simpatia',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Si algo parece demasiado amable o familiar, verifica igual: la confianza también se falsifica.',
    E'# Simpatía y confianza: el anzuelo invisible\n\nLos atacantes imitan el tono de un amigo, un compañero o una marca querida para que bajes la guardia: dominios parecidos (typosquatting), remitentes conocidos y diseños copiados.\n\n## Señales\n\n- Direcciones con letras cambiadas (paypa1, micros0ft).\n- Mensajes "de un amigo" con enlaces raros.\n- Certificados o candados que no garantizan nada por sí solos.\n\n## Qué hacer\n\n1. Lee el dominio letra por letra.\n2. Si un contacto envía algo extraño, pregúntale por otro medio.\n3. El candado solo indica cifrado, no legitimidad.',
    ARRAY['simpatía', 'confianza', 'typosquatting', 'microlección'],
    TRUE
),
(
    'Prueba social: si todos caen, tú también',
    'micro-principio-prueba-social',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Reseñas falsas y contadores inventados te empujan a confiar. Aprende a dudar.',
    E'# Prueba social: si todos caen, tú también\n\n"Miles de personas ya lo hicieron" o reseñas perfectas pueden estar fabricadas para que confíes.\n\n## Señales\n\n- Testimonios idénticos o sin fecha.\n- Contadores de "descargas" sin fuente.\n- Presión de grupo en mensajes ("todos tus contactos ya verificaron").\n\n## Qué hacer\n\n1. Busca opiniones fuera del propio sitio.\n2. Desconfía de la unanimidad perfecta.\n3. Verifica el dominio antes que las reseñas.',
    ARRAY['prueba social', 'Cialdini', 'microlección'],
    TRUE
),
(
    'Reciprocidad: nada es gratis',
    'micro-principio-reciprocidad',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Regalos y premios inesperados crean una deuda que el estafador cobra con tus datos.',
    E'# Reciprocidad: nada es gratis\n\nPremios, cupones y "regalos" inesperados activan la necesidad de devolver el favor: el pago siempre son tus datos.\n\n## Señales\n\n- Premios que no pediste ni concursaste.\n- Archivos adjuntos "de regalo" (.apk, .exe, .zip).\n- Para "reclamar" piden datos o pagos pequeños.\n\n## Qué hacer\n\n1. Si no participaste, no ganaste nada.\n2. Nunca instales adjuntos de mensajes no solicitados.\n3. Ningún premio real pide tus claves.',
    ARRAY['reciprocidad', 'premios', 'Cialdini', 'microlección'],
    TRUE
),
(
    'Compromiso: el sí que te encadena',
    'micro-principio-compromiso',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Pequeños pasos llevan a grandes entregas: detecta la escalada antes del daño.',
    E'# Compromiso: el sí que te encadena\n\nPrimero te piden algo pequeño (responder una encuesta, confirmar tu correo) y luego escalan hasta tus credenciales. Mantener la coherencia con tu primer "sí" es la trampa.\n\n## Señales\n\n- Formularios por pasos que piden cada vez más datos.\n- "Solo falta un dato para terminar".\n- Procesos que no puedes cancelar a mitad de camino.\n\n## Qué hacer\n\n1. Puedes abandonar cualquier proceso en cualquier momento.\n2. Revisa qué entregaste en cada paso.\n3. Un sitio legítimo explica para qué usa cada dato.',
    ARRAY['compromiso', 'coherencia', 'Cialdini', 'microlección'],
    TRUE
),
(
    'Unidad e identidad: "los de tu grupo ya lo hicieron"',
    'micro-principio-unidad',
    'SOCIAL_ENGINEERING', 'BEGINNER', 3,
    'Los ataques que apelan a tu grupo (empresa, universidad, familia) buscan obediencia automática.',
    E'# Unidad e identidad\n\nMensajes que apelan a tu empresa, tu universidad o tu familia ("todos en la empresa ya actualizaron sus datos") usan tu identidad de grupo para que actúes sin verificar.\n\n## Señales\n\n- Correos "internos" desde dominios externos.\n- Grupos de mensajería con "comunicados oficiales".\n- Piden difundir el mensaje a tus contactos.\n\n## Qué hacer\n\n1. Verifica por el canal oficial de tu organización.\n2. Un comunicado real no llega solo por WhatsApp.\n3. No reenvíes sin verificar: cortas la cadena.',
    ARRAY['unidad', 'identidad', 'Cialdini', 'microlección'],
    TRUE
)
) AS v(title, slug, category, difficulty, reading_time_min, summary, content, tags, is_published)
WHERE NOT EXISTS (SELECT 1 FROM educational_content c WHERE c.slug = v.slug);
