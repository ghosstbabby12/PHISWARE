# Plantilla contraste de hipótesis — Sección 4.5 (copiar al documento y completar)

| Hipótesis | Evidencia relacionada | Estado |
|---|---|---|
| H1 — Implementación integral mejora respuesta ante phishing | 4.3.2 (precisión __%) + 4.4.2 (mejora __pp) | (completar: confirmada / parcialmente / no confirmada) |
| H2 — Módulo educativo mejora reconocimiento | 4.4.2 I1+I3: antes __% → después __% | (completar) |
| H3 — Análisis en tiempo real reduce exposición | 4.3.3 t_prom __ms (<5000) + U1 percepción __/5 | (completar) |
| H4 — Clasificación automática es confiable | 4.3.2 precisión __%, recall __%, FP __% | (completar) |
| H0 — No hay reducción significativa vs convencionales | Limitaciones GSB/PhishTank 2.1.2 vs precisión propia __% | (se rechaza / no se rechaza) |

Párrafo modelo por hipótesis (el documento exige un párrafo después de cada tabla):
> "H4 se confirma parcialmente: el sistema alcanzó una precisión general de __% (VP=__, VN=__, FP=__, FN=__) sobre N=__ URLs (4.3.2), con un tiempo promedio de __ms (4.3.3). Los FP (__%) afectan usabilidad pero los FN (__%) —el riesgo crítico— se mantienen bajo __%, por lo que la clasificación orienta correctamente al usuario según niveles Bajo/Medio/Alto (2.2.7)..."

## Síntesis calidad 4.6.3 (modelo)
> "La mejor calificada fue Mantenibilidad (__/5) por la arquitectura en capas (4.2); la más baja Adecuación (__/5), coherente con la delimitación 1.8 (solo reglas+APIs sin IA). Eficiencia (__/5) respaldada por p95 <5s (4.3.3 HU-NF01) y Fiabilidad (__/5) por disponibilidad __% (HU-NF07) con fallback ante fallo de fuente externa (HU-NF08)..."
