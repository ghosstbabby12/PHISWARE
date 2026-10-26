# Piloto educativo PHISHWARE — Cuestionario pre/post (10 preguntas)
## Llena 4.4.1, 4.4.2, 4.4.3 + H2 + variables 2.3.4.2

**Cómo aplicarlo (30-40 min por participante):**
1. Crear Google Forms con estas 10 preguntas (opción múltiple, 1 correcta). Aplicar ANTES de usar PHISHWARE.
2. Intervención: 20 min lecciones `/education` (mínimo: Phishing, Ingeniería social, Smishing, Buenas prácticas) + 1 quiz + analizar 3 URLs en `/analysis`.
3. Aplicar el MISMO test DESPUÉS. Registrar en `plantilla_resultados_piloto.csv`.
4. n mínimo defendible: **15 participantes** (ideal 20). Perfil 4.4.1: usuarios generales Pasto, 18-45 años, uso internet diario, sin formación en ciberseguridad.

### Preguntas (mapeadas a indicadores 4.4.2)

**I1. Reconocimiento de enlaces o sitios sospechosos**
- P1. ¿Cuál URL es más sospechosa? a) bancolombia.com b) banco.seguridad-login.net c) dian.gov.co d) No sé
  - Correcta: b (subdominio engañoso, numeral 4.1.1)
- P2. Recibes SMS "NEQUI: su cuenta será bloqueada en 24h, verifique en nequi-verificacion.tk". ¿Qué haces? a) Clic inmediato b) Verifico dominio oficial por otro canal c) Reenvío d) No sé
  - Correcta: b

**I2. Comprensión de las alertas preventivas**
- P3. PHISHWARE marca una URL como SUSPICIOUS (amarillo). ¿Qué significa? a) Segura b) Precaución, revisar antes de continuar c) Siempre fraude d) No sé — Correcta: b
- P4. Ante alerta DANGEROUS (rojo) que pide credenciales, lo correcto es: a) Continuar igual b) Cerrar e ingresar por canal oficial c) Desactivar alerta d) No sé — Correcta: b

**I3. Identificación de características de phishing**
- P5. Señal típica de typosquatting: a) https + candado b) paypa1.com con "1" en vez de "l" c) Dominio .gov.co d) No sé — Correcta: b
- P6. ¿Qué indica `https://bancolombia.com@malicioso.ru/login`? a) Sitio oficial b) Truco userinfo: destino real malicioso.ru c) Error tipográfico d) No sé — Correcta: b
- P7. Un correo apela a "autoridad del banco + urgencia 24h". Según Cialdini (2.2.2.1) es: a) Cortesía b) Persuasión por autoridad/escasez c) Coincidencia d) No sé — Correcta: b

**I4. Aplicación de buenas prácticas**
- P8. Antes de ingresar tu contraseña debes: a) Verificar dominio carácter por carácter b) Confiar en el logo c) Usar la misma clave en todo d) No sé — Correcta: a
- P9. Si sospechas phishing debes: a) Reportarlo en PHISHWARE (/reports) y avisar por canal oficial b) Ignorarlo siempre c) Compartirlo por WhatsApp d) No sé — Correcta: a
- P10. Según Ley 1581 (2.5.2), tus datos en PHISHWARE: a) Se venden b) Se tratan con autorización y seguridad c) Son públicos d) No sé — Correcta: b

Cada acierto = 10 pts (escala 0-100 por indicador: I1=P1-P2, I2=P3-P4, I3=P5-P7, I4=P8-P10).

### Percepción utilidad (4.4.3, Likert 1-5, solo DESPUÉS)
- U1. Las alertas fueron claras. U2. El módulo educativo me sirve. U3. Recomendaría PHISHWARE.
- Abierta: ¿Qué mejorarías? (citar 2-3 respuestas textuales en el documento)
