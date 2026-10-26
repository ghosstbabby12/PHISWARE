# PHISHWARE — Kit de pruebas para llenar Capítulo 4 (4.3 → 4.6)
## Mapeo exacto documento ↔ herramienta ↔ archivo de este folder

| Sección documento | Qué pide | Cómo llenarla con este kit |
|---|---|---|
| 4.3.1 Conjunto de prueba | Total, phishing, legítimas, fuente | `urls_prueba.csv` = 100 (50/50). Contar y pegar. Fuente: sintéticas trazables a 4.1.1 + validar submuestra con PhishTank |
| 4.3.2 Clasificación | VP, VN, FP, FN, precisión % | `python evaluar_clasificacion.py --register` → consola + `resultados_clasificacion.csv` + `resumen_metricas.json` |
| 4.3.3 Rendimiento | Tiempo promedio + disponibilidad | Opción A (jurado): JMeter `phishware-rendimiento.jmx` SG1/SG2/SG3. Opción B (hoy): `python medir_rendimiento.py --token <JWT>` y `--disponibilidad` |
| 4.4.1 / 4.4.2 / 4.4.3 Piloto | N participantes, antes/después, percepción | `piloto/cuestionario_pre_post.md` (10 preguntas) + `piloto/participantes.csv` + `piloto/plantilla_resultados_piloto.csv` |
| 4.5 Contraste H1-H4/H0 | Estado + párrafo por hipótesis | `plantilla_contraste_hipotesis.md` + cifras de 4.3/4.4 |
| 4.6.2 / 4.6.3 ISO 25010 | Calificación 1-5 + síntesis | `iso25010_rubrica.csv` (director + 1 técnico) |
| Gráficas | Barras antes/después, matriz | `python generar_graficas.py` → `fig_*.svg` listos para Word |

## Orden de ejecución (2-3 días)

### Día 1 — Backend + clasificación (2 h)
```bat
cd phishware-backend
docker-compose up -d postgres ^  (o desde raiz PHISWARE\docker-compose.yml)
$env:DB_USERNAME="phishware"; $env:DB_PASSWORD="phishware2024"
mvn spring-boot:run
:: en otra terminal:
cd ..\docs\tests
python evaluar_clasificacion.py --register --username tesista --email tesista@phishware.com
:: anotar salida "===== PARA PEGAR EN DOCUMENTO =====" -> tablas 4.3.1/4.3.2/4.3.3 (tiempo)
```

### Día 1-2 — JMeter (evidencia formal rendimiento)
1. Instalar JMeter 5.6.3 (https://jmeter.apache.org/download_jmeter.cgi, requiere Java 17+ ya instalado).
2. Conseguir JWT: POST `http://localhost:8080/api/auth/login` {"usernameOrEmail":"tesista@phishware.com","password":"..."} → copiar `accessToken`.
3. Ejecutar:
```
jmeter -n -t phishware-rendimiento.jmx -JTOKEN=<JWT> -l resultados.jtl -e -o dashboard-html
```
4. De `dashboard-html`: capturar `Average Response Time`, `p95`, `%Error` → Tabla 4.3.3. SG3 (180×10s ≈ 30 min) → disponibilidad → HU-NF07. SG4 (deshabilitado por defecto, activar sin keys) → HU-NF08.
5. Si JMeter falla hoy: `python medir_rendimiento.py --token <JWT> --n 100` y `--disponibilidad --minutos 30` como respaldo (anotar que la medición formal se repitió en JMeter).

### Día 2-3 — Piloto usuarios (H2)
1. Google Forms con `piloto/cuestionario_pre_post.md`. Aplicar pre → usar PHISHWARE 30 min → aplicar post.
2. Vaciar promedios en `piloto/plantilla_resultados_piloto.csv` y `python generar_graficas.py`.
3. Redactar 4.4.3 con 2-3 citas textuales.

### Cierre — 4.5 y 4.6
1. Copiar `plantilla_contraste_hipotesis.md` al Word y reemplazar `__%` con cifras reales + 1 párrafo por hipótesis.
2. Llenar `iso25010_rubrica.csv` con director, pegar tabla 4.6.2 y redactar síntesis 4.6.3.

## Notas para que el jurado no observe
- Sin API keys GSB/VT el modo es `HEURISTIC_ONLY` (ver `/api/system/api-status` como ADMIN). Declararlo en 4.3.1: "análisis heurístico + contenido + RDAP; GSB/VT como refuerzo cuando hay key (HU-08)". No prometer PhishTank en vivo si no se usa: el doc nombra PhishTank pero el código usa VirusTotal — justificar sustitución (VT agrega 90+ motores vs lista única).
- Las 50 phishing sintéticas son válidas porque cada una instancia una técnica de tu 4.1.1 (columna `nota`). Para blindaje: validar 10 de ellas contra PhishTank online el día de la prueba y anotar fecha.
- Tiempo <5s: medir con `analysisTimeMs` del servidor + `http_ms` total; reportar ambos.
