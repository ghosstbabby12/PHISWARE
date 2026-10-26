-- ============================================================
-- V5: Un quiz por cada módulo educativo para seguimiento del aprendizaje
-- ============================================================

-- 1. Vincular los quizzes existentes con su módulo
UPDATE quiz SET content_id = (SELECT id FROM educational_content WHERE slug = 'que-es-el-phishing-guia-completa')
WHERE title = 'Fundamentos de Phishing' AND content_id IS NULL;
UPDATE quiz SET content_id = (SELECT id FROM educational_content WHERE slug = 'ingenieria-social-arte-del-engano')
WHERE title = 'Ingeniería Social Avanzada' AND content_id IS NULL;
UPDATE quiz SET content_id = (SELECT id FROM educational_content WHERE slug = 'owasp-top-10-amenazas-web-phishing')
WHERE title = 'Quiz OWASP: ¿Conoces las Vulnerabilidades del Phishing?' AND content_id IS NULL;
UPDATE quiz SET content_id = (SELECT id FROM educational_content WHERE slug = 'nist-cybersecurity-framework-defensa-phishing')
WHERE title = 'Quiz NIST CSF: Gestión del Riesgo de Phishing' AND content_id IS NULL;

-- 2. Completar el quiz de Ingeniería Social (tenía 4 preguntas)
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, 'Un correo que parece de tu jefe te pide hacer una transferencia urgente sin seguir el proceso habitual. ¿Qué principio psicológico se está explotando principalmente?', 'SINGLE_CHOICE',
       '{"A": "Reciprocidad", "B": "Prueba social", "C": "Autoridad", "D": "Escasez"}'::JSONB, '{"correct": ["C"]}'::JSONB,
       'El atacante se hace pasar por una figura de autoridad para que la víctima obedezca sin verificar.', 15, 5
FROM quiz q WHERE q.title = 'Ingeniería Social Avanzada'
  AND NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id AND qq.order_index = 5);

-- 3. Quizzes nuevos para los módulos que no tenían

-- Quiz: Smishing — Phishing por SMS
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz: Smishing — Phishing por SMS', 'Comprueba si sabes reconocer y responder ante mensajes de texto fraudulentos.', c.id, 'BEGINNER', 600, 70, 50, TRUE
FROM educational_content c WHERE c.slug = 'smishing-phishing-por-sms'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'smishing-phishing-por-sms',
(VALUES
    ('¿Por qué los atacantes usan URLs acortadas (como bit.ly) en los SMS de smishing?', 'SINGLE_CHOICE', '{"A": "Porque cargan más rápido en el celular", "B": "Porque ocultan el destino real del enlace", "C": "Porque los operadores exigen usarlas", "D": "Porque los SMS no admiten URLs largas"}', '{"correct": ["B"]}', 'Un acortador esconde el dominio real, así la víctima no puede verificar a dónde la lleva el enlace.', 10, 1),
    ('Recibes: ''Su paquete no pudo entregarse. Reprograme aquí: [link]'', pero no esperabas ningún paquete. ¿Qué haces?', 'SINGLE_CHOICE', '{"A": "Abro el enlace solo para ver de qué se trata", "B": "Respondo STOP para que no me escriban más", "C": "Ignoro el enlace y consulto en la web o app oficial de la transportadora", "D": "Lo reenvío a mis contactos para alertarlos"}', '{"correct": ["C"]}', 'Nunca hagas clic en enlaces de SMS no solicitados. Verifica siempre por un canal oficial que tú mismo abras.', 10, 2),
    ('¿Cuáles son señales de smishing? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Remitente con número desconocido o extraño", "B": "URL acortada que no puedes verificar", "C": "Mensaje de un contacto guardado diciendo que llega en 5 minutos", "D": "Urgencia exagerada para actuar", "E": "Errores gramaticales"}', '{"correct": ["A", "B", "D", "E"]}', 'Números extraños, enlaces acortados, urgencia y errores gramaticales son las señales clásicas del smishing.', 10, 3),
    ('Verdadero o falso: los SMS son más seguros que el correo electrónico porque tienen mejores filtros de spam.', 'TRUE_FALSE', '{"A": "Verdadero", "B": "Falso"}', '{"correct": ["B"]}', 'Es al revés: los SMS tienen menos filtros que el correo y una tasa de apertura del 98%, por eso son tan efectivos para los atacantes.', 10, 4),
    ('Te llega un SMS que dice que tu tarjeta fue bloqueada. ¿Cuál es la forma más segura de verificarlo?', 'SINGLE_CHOICE', '{"A": "Llamar al número que aparece en el SMS", "B": "Abrir la app oficial del banco o llamar al número del reverso de la tarjeta", "C": "Abrir el enlace solo si estoy conectado al Wi-Fi de mi casa", "D": "Responder el SMS con los últimos 4 dígitos de la tarjeta"}', '{"correct": ["B"]}', 'Usa siempre canales que tú controlas: la app oficial o el número impreso en tu tarjeta, nunca los datos que vienen en el mensaje.', 10, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz: Vishing — Phishing por Voz
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz: Vishing — Phishing por Voz', 'Evalúa cómo identificas y respondes a llamadas fraudulentas.', c.id, 'BEGINNER', 600, 70, 50, TRUE
FROM educational_content c WHERE c.slug = 'vishing-phishing-por-voz'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'vishing-phishing-por-voz',
(VALUES
    ('¿Qué es el Caller ID spoofing?', 'SINGLE_CHOICE', '{"A": "Grabar una llamada sin permiso", "B": "Falsificar el número de origen para que la llamada parezca legítima", "C": "Bloquear llamadas de números desconocidos", "D": "Desviar llamadas a un buzón de voz"}', '{"correct": ["B"]}', 'Con spoofing el atacante hace que en tu pantalla aparezca el número de tu banco o de una entidad oficial.', 10, 1),
    ('Alguien que dice ser de tu banco te pide el código que te acaba de llegar por SMS para verificar tu identidad. ¿Qué haces?', 'SINGLE_CHOICE', '{"A": "Se lo doy porque la llamada parece oficial", "B": "Le doy solo la mitad del código", "C": "Cuelgo: los códigos de verificación nunca se comparten con nadie", "D": "Le pido que me lo envíe por correo primero"}', '{"correct": ["C"]}', 'Ningún banco te pedirá un código 2FA por teléfono. Quien lo pide está intentando entrar a tu cuenta.', 10, 2),
    ('¿Cuáles son señales de alerta de vishing? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Llamada no solicitada de soporte técnico", "B": "Piden acceso remoto a tu dispositivo", "C": "Piden pago con tarjetas de regalo o criptomonedas", "D": "Tú llamaste al número oficial que aparece en la web del banco", "E": "Amenazas de consecuencias inmediatas"}', '{"correct": ["A", "B", "C", "E"]}', 'Si tú iniciaste la llamada al número oficial no es una señal de alerta; las demás sí lo son.', 10, 3),
    ('Verdadero o falso: si el identificador de llamadas muestra el número oficial del banco, la llamada es legítima.', 'TRUE_FALSE', '{"A": "Verdadero", "B": "Falso"}', '{"correct": ["B"]}', 'Falso. El número puede estar falsificado (spoofing). Cuelga y llama tú al número oficial.', 10, 4),
    ('Un supuesto técnico de Microsoft te llama diciendo que tu computador tiene virus y te pide instalar un programa de acceso remoto. ¿Qué haces?', 'SINGLE_CHOICE', '{"A": "Lo instalo si el programa es conocido", "B": "Pido su número de empleado y continúo", "C": "Cuelgo: Microsoft no hace llamadas no solicitadas de soporte", "D": "Le doy acceso solo por 5 minutos"}', '{"correct": ["C"]}', 'Las empresas tecnológicas no llaman sin que lo solicites. Dar acceso remoto le entrega el control total de tu equipo al atacante.', 10, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz: 10 Buenas Prácticas de Ciberseguridad
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz: 10 Buenas Prácticas de Ciberseguridad', 'Repasa los hábitos básicos que te protegen día a día.', c.id, 'BEGINNER', 600, 70, 50, TRUE
FROM educational_content c WHERE c.slug = '10-buenas-practicas-ciberseguridad'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = '10-buenas-practicas-ciberseguridad',
(VALUES
    ('Según el módulo, ¿cuál es la longitud mínima recomendada para una contraseña?', 'SINGLE_CHOICE', '{"A": "6 caracteres", "B": "8 caracteres", "C": "12 caracteres", "D": "4 caracteres"}', '{"correct": ["C"]}', 'El módulo recomienda mínimo 12 caracteres combinando letras, números y símbolos.', 10, 1),
    ('¿Por qué conviene activar la autenticación de dos factores (2FA)?', 'SINGLE_CHOICE', '{"A": "Porque hace que la contraseña sea más corta", "B": "Porque aunque roben tu contraseña no podrán acceder a tu cuenta", "C": "Porque elimina la necesidad de actualizar el sistema", "D": "Porque bloquea todos los correos de spam"}', '{"correct": ["B"]}', 'El segundo factor impide el acceso aunque el atacante conozca tu contraseña.', 10, 2),
    ('¿Cuáles de estas son buenas prácticas recomendadas? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Usar una contraseña diferente para cada servicio", "B": "Usar una VPN en redes Wi-Fi públicas", "C": "Posponer las actualizaciones hasta que algo falle", "D": "Hacer copias de seguridad regulares", "E": "Publicar tu fecha de nacimiento en tu perfil público"}', '{"correct": ["A", "B", "D"]}', 'Contraseñas únicas, VPN en redes públicas y backups son prácticas clave. Postergar actualizaciones y exponer datos personales aumentan el riesgo.', 10, 3),
    ('¿Qué práctica le quita más poder a un ataque de ransomware?', 'SINGLE_CHOICE', '{"A": "Usar una VPN", "B": "Tener copias de seguridad de tus datos", "C": "Cambiar el fondo de pantalla", "D": "Usar un navegador diferente"}', '{"correct": ["B"]}', 'Si tienes backups puedes recuperar tus datos sin pagar el rescate.', 10, 4),
    ('Verdadero o falso: los atacantes pueden usar la información pública de tus redes sociales (OSINT) para preparar ataques dirigidos.', 'TRUE_FALSE', '{"A": "Verdadero", "B": "Falso"}', '{"correct": ["A"]}', 'Verdadero. Por eso conviene limitar la información personal que publicas.', 10, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz OWASP A03: Inyección y Manipulación de URLs
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz OWASP A03: Inyección y Manipulación de URLs', 'Pon a prueba tu capacidad para detectar URLs manipuladas con técnicas de inyección.', c.id, 'ADVANCED', 600, 70, 100, TRUE
FROM educational_content c WHERE c.slug = 'owasp-a03-inyeccion-phishing-manipulacion-urls'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'owasp-a03-inyeccion-phishing-manipulacion-urls',
(VALUES
    ('En la URL https://secure-login.paypal.com.attacker.xyz/verify, ¿cuál es el dominio real?', 'SINGLE_CHOICE', '{"A": "paypal.com", "B": "secure-login.paypal.com", "C": "attacker.xyz", "D": "verify"}', '{"correct": ["C"]}', 'El dominio se lee de derecha a izquierda antes de la primera barra: el dominio real es attacker.xyz.', 15, 1),
    ('Un correo contiene el enlace <a href=''https://sitio-malicioso.xyz''>https://banco-oficial.com</a>. Si haces clic, ¿a dónde te lleva?', 'SINGLE_CHOICE', '{"A": "A banco-oficial.com", "B": "A sitio-malicioso.xyz", "C": "Depende del navegador", "D": "A ninguna parte, el enlace está roto"}', '{"correct": ["B"]}', 'Esto es HTML Injection: el texto visible dice una cosa, pero el destino real está en el atributo href.', 15, 2),
    ('¿Por qué es peligrosa una URL como https://www.google.com/url?q=https://phishing-site.xyz?', 'SINGLE_CHOICE', '{"A": "Porque google.com es un sitio inseguro", "B": "Porque usa un parámetro de redirección para llevarte a otro sitio aunque empiece con google.com", "C": "Porque no tiene HTTPS", "D": "No es peligrosa, Google la verificó"}', '{"correct": ["B"]}', 'Es una redirección abierta: la víctima ve un dominio confiable y termina en el sitio del atacante.', 15, 3),
    ('¿Qué reglas ayudan a identificar una URL inyectada? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Leer el dominio de derecha a izquierda antes de la primera /", "B": "Verificar cada carácter del dominio", "C": "Confiar si la URL tiene HTTPS", "D": "Pasar el cursor sobre el enlace sin hacer clic", "E": "Confiar si el dominio contiene el nombre de la marca"}', '{"correct": ["A", "B", "D"]}', 'HTTPS solo indica cifrado, no legitimidad, y cualquiera puede registrar un dominio que contenga el nombre de una marca.', 15, 4),
    ('Verdadero o falso: pаypal.com escrito con una ''а'' cirílica es el mismo dominio que paypal.com.', 'TRUE_FALSE', '{"A": "Verdadero", "B": "Falso"}', '{"correct": ["B"]}', 'Falso. Es un ataque homógrafo: la letra se ve igual pero es otro carácter Unicode, por lo tanto otro dominio.', 15, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz OWASP A07: Fallos de Autenticación
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz OWASP A07: Fallos de Autenticación', 'Evalúa lo que sabes sobre el robo de credenciales y cómo proteger tus cuentas.', c.id, 'BEGINNER', 600, 70, 50, TRUE
FROM educational_content c WHERE c.slug = 'owasp-a07-fallos-autenticacion-objetivo-phishing'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'owasp-a07-fallos-autenticacion-objetivo-phishing',
(VALUES
    ('Según el informe Verizon DBIR 2023 citado en el módulo, ¿qué porcentaje de las brechas de datos involucra credenciales robadas?', 'SINGLE_CHOICE', '{"A": "12%", "B": "35%", "C": "83%", "D": "99%"}', '{"correct": ["C"]}', 'El 83% de las brechas involucra credenciales robadas, y el phishing es el principal método para robarlas.', 10, 1),
    ('¿Por qué un gestor de contraseñas ayuda a protegerte del phishing?', 'SINGLE_CHOICE', '{"A": "Porque cambia tus contraseñas cada día", "B": "Porque no autocompleta tus credenciales en un dominio falso", "C": "Porque bloquea todos los correos sospechosos", "D": "Porque oculta tu dirección IP"}', '{"correct": ["B"]}', 'El gestor asocia cada contraseña a un dominio exacto; en un sitio clonado no la ofrecerá, y esa es una señal de alerta.', 10, 2),
    ('¿Por qué herramientas como Evilginx son especialmente peligrosas?', 'SINGLE_CHOICE', '{"A": "Porque solo funcionan en computadores antiguos", "B": "Porque actúan como proxy transparente y pueden capturar incluso tokens MFA", "C": "Porque borran el disco duro de la víctima", "D": "Porque envían spam masivo"}', '{"correct": ["B"]}', 'Al ubicarse entre la víctima y el sitio real, capturan credenciales y tokens de sesión en tiempo real.', 10, 3),
    ('¿Cuáles son recomendaciones de NIST SP 800-63B según el módulo? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Verificar las contraseñas contra listas negras", "B": "No expirar contraseñas periódicamente, solo al detectar compromiso", "C": "Obligar a cambiar la contraseña cada 30 días", "D": "Favorecer la longitud sobre la complejidad", "E": "Limitar las contraseñas a máximo 12 caracteres"}', '{"correct": ["A", "B", "D"]}', 'NIST desaconseja la expiración periódica y los límites cortos; prioriza longitud y listas negras.', 10, 4),
    ('¿Cuál es el orden correcto de las fases del robo de credenciales descritas en el módulo?', 'SINGLE_CHOICE', '{"A": "Distribución → Clonación → Reconocimiento → Captura", "B": "Reconocimiento → Clonación → Distribución → Captura", "C": "Clonación → Reconocimiento → Captura → Distribución", "D": "Captura → Distribución → Clonación → Reconocimiento"}', '{"correct": ["B"]}', 'Primero se estudia el objetivo, luego se clona la página, se distribuye el enlace y finalmente se capturan las credenciales.', 10, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz NIST SP 800-30: Evaluación de Riesgos
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz NIST SP 800-30: Evaluación de Riesgos', 'Comprueba si sabes aplicar la metodología de evaluación de riesgos de NIST al phishing.', c.id, 'ADVANCED', 600, 70, 100, TRUE
FROM educational_content c WHERE c.slug = 'nist-sp-800-30-evaluacion-riesgos-phishing'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'nist-sp-800-30-evaluacion-riesgos-phishing',
(VALUES
    ('¿Cuál es el primer paso de una evaluación de riesgos según NIST SP 800-30?', 'SINGLE_CHOICE', '{"A": "Comunicación de resultados", "B": "Preparación de la evaluación", "C": "Mantenimiento de la evaluación", "D": "Ejecución de la evaluación"}', '{"correct": ["B"]}', 'Los pasos son: Preparación, Ejecución, Comunicación y Mantenimiento.', 15, 1),
    ('Según las tablas del módulo, un ejecutivo de alto nivel (C-suite) sin MFA tiene una probabilidad de ataque:', 'SINGLE_CHOICE', '{"A": "Baja", "B": "Moderada", "C": "Alta", "D": "Muy alta"}', '{"correct": ["D"]}', 'Los ejecutivos son objetivos de whaling y sin MFA el ataque es casi seguro: probabilidad muy alta.', 15, 2),
    ('En la matriz de riesgo del módulo, ¿qué resultado da una probabilidad ALTA con impacto ALTO?', 'SINGLE_CHOICE', '{"A": "Verde (riesgo bajo)", "B": "Amarillo (riesgo moderado)", "C": "Naranja (riesgo considerable)", "D": "Rojo (riesgo alto)"}', '{"correct": ["D"]}', 'Probabilidad alta × impacto alto se ubica en la zona roja de la matriz.', 15, 3),
    ('¿Cuáles de estas son VULNERABILIDADES (no amenazas) en el contexto del phishing? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Falta de MFA", "B": "Contraseñas débiles", "C": "Spear phishing", "D": "Desconocimiento de las señales de alerta", "E": "Smishing"}', '{"correct": ["A", "B", "D"]}', 'Spear phishing y smishing son amenazas; las vulnerabilidades son las debilidades que esas amenazas explotan.', 15, 4),
    ('¿Qué es el riesgo residual?', 'SINGLE_CHOICE', '{"A": "El riesgo antes de aplicar cualquier control", "B": "El riesgo que queda después de aplicar los controles", "C": "El riesgo de un sistema que ya fue atacado", "D": "El riesgo que asume el proveedor de internet"}', '{"correct": ["B"]}', 'Ningún control elimina el riesgo por completo; lo que queda después de aplicarlos es el riesgo residual.', 15, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- Quiz: Ciberhigiene con OWASP y NIST
INSERT INTO quiz (title, description, content_id, difficulty, time_limit_sec, passing_score, points_reward, is_active)
SELECT 'Quiz: Ciberhigiene con OWASP y NIST', 'Repasa las prácticas cotidianas de seguridad que recomiendan OWASP y NIST.', c.id, 'BEGINNER', 600, 70, 50, TRUE
FROM educational_content c WHERE c.slug = 'buenas-practicas-owasp-nist-ciberhigiene'
  AND NOT EXISTS (SELECT 1 FROM quiz WHERE content_id = c.id);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answers, explanation, points, order_index)
SELECT q.id, v.question_text, v.question_type, v.options::JSONB, v.correct_answers::JSONB, v.explanation, v.points, v.order_index
FROM quiz q JOIN educational_content c ON c.id = q.content_id AND c.slug = 'buenas-practicas-owasp-nist-ciberhigiene',
(VALUES
    ('¿Cuál es el orden de preferencia de métodos MFA recomendado en el módulo?', 'SINGLE_CHOICE', '{"A": "SMS > Apps TOTP > Llaves físicas", "B": "Llaves físicas (FIDO2) > Apps TOTP > SMS", "C": "Apps TOTP > SMS > Llaves físicas", "D": "Todos los métodos son igual de seguros"}', '{"correct": ["B"]}', 'Las llaves FIDO2/WebAuthn son resistentes al phishing; el SMS es el método más débil.', 10, 1),
    ('¿Qué significa la regla 3-2-1 de copias de seguridad?', 'SINGLE_CHOICE', '{"A": "3 contraseñas, 2 correos, 1 teléfono", "B": "3 copias, en 2 medios diferentes, 1 fuera del sitio o en la nube", "C": "Hacer backup 3 veces al día, 2 días a la semana, 1 mes al año", "D": "3 discos, 2 computadores, 1 servidor"}', '{"correct": ["B"]}', 'Tres copias, dos medios distintos y una fuera del lugar protegen tus datos incluso ante ransomware.', 10, 2),
    ('Según la regla de oro del módulo, ¿qué debes hacer cuando un mensaje te genera urgencia?', 'SINGLE_CHOICE', '{"A": "Actuar de inmediato para no perder el acceso", "B": "Esperar 5 minutos antes de actuar", "C": "Reenviarlo a tus contactos", "D": "Responder pidiendo más plazo"}', '{"correct": ["B"]}', 'La urgencia es una herramienta del atacante. Un problema real no desaparece si esperas unos minutos.', 10, 3),
    ('¿Cuáles son buenas prácticas de permisos y separación de cuentas? (Selecciona todas las correctas)', 'MULTIPLE_CHOICE', '{"A": "Revocar el acceso OAuth de apps que ya no usas", "B": "Usar alias de correo para registrarte en sitios de terceros", "C": "Usar el mismo correo para el trabajo y lo personal", "D": "Aplicar el principio de mínimo privilegio", "E": "Dar todos los permisos a las apps para que funcionen mejor"}', '{"correct": ["A", "B", "D"]}', 'Separar cuentas y limitar permisos reduce el daño si una cuenta o una app es comprometida.', 10, 4),
    ('Si estás en Colombia, ¿a dónde puedes reportar un incidente de ciberseguridad según el módulo?', 'SINGLE_CHOICE', '{"A": "incidencias@incibe-cert.es", "B": "incidentes@colcert.gov.co", "C": "cgsi@sspc.gob.mx", "D": "soporte@google.com"}', '{"correct": ["B"]}', 'ColCERT es el equipo de respuesta a incidentes de Colombia. INCIBE es de España y CGSI de México.', 10, 5)
) AS v(question_text, question_type, options, correct_answers, explanation, points, order_index)
WHERE NOT EXISTS (SELECT 1 FROM quiz_questions qq WHERE qq.quiz_id = q.id);

-- 4. Un módulo = un quiz
CREATE UNIQUE INDEX IF NOT EXISTS uq_quiz_content ON quiz(content_id) WHERE content_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_quiz_results_user_quiz ON quiz_results(user_id, quiz_id);

-- 5. Actualizar la fórmula de riesgo descrita en el módulo NIST SP 800-30 (el motor ahora usa noisy-OR)
UPDATE educational_content SET content = replace(content,
  'Score final = GSB_score(60%) + VT_score(30%) + Heuristic_score(10%)
```
Donde cada fuente representa una dimensión de probabilidad e impacto combinados.',
  'Riesgo = 1 − Π (1 − pᵢ)
```
Cada fuente (Google Safe Browsing, VirusTotal, heurística de URL, contenido de la página y antigüedad del dominio) aporta una probabilidad pᵢ. Se combinan de forma que una señal fuerte nunca se diluye y varias señales débiles se refuerzan entre sí.')
WHERE slug = 'nist-sp-800-30-evaluacion-riesgos-phishing';
