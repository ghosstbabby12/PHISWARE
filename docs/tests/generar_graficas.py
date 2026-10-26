#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PHISHWARE - Genera las 3 graficas exigidas por el documento SIN matplotlib.
Lee resumen_metricas.json (de evaluar_clasificacion.py) y resultados_piloto.csv,
y genera archivos SVG listos para pegar en Word como Figuras 4.x.

Uso:
  python generar_graficas.py
Salida: fig_matriz_confusion.svg, fig_piloto_antes_despues.svg, fig_tiempos.svg
"""
import csv, json, os

def svg_bar(path, titulo, series, ylabel=""):
    # series: [(etiqueta, valor, color)]
    W, H, pad = 640, 360, 60
    maxv = max(v for _, v, _ in series) or 1
    bw = (W - pad*2) / len(series)
    s = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}">',
         f'<rect width="{W}" height="{H}" fill="white"/>',
         f'<text x="{W//2}" y="28" text-anchor="middle" font-size="16" font-family="Arial" font-weight="bold">{titulo}</text>']
    for i, (lab, val, col) in enumerate(series):
        h = (H - pad - 80) * val / maxv
        x = pad + i * bw + 12; y = H - pad - h
        s.append(f'<rect x="{x:.0f}" y="{y:.0f}" width="{bw-24:.0f}" height="{h:.0f}" fill="{col}"/>')
        s.append(f'<text x="{x+(bw-24)/2:.0f}" y="{y-6:.0f}" text-anchor="middle" font-size="13" font-family="Arial">{val}</text>')
        s.append(f'<text x="{x+(bw-24)/2:.0f}" y="{H-pad+18:.0f}" text-anchor="middle" font-size="12" font-family="Arial">{lab}</text>')
    s.append(f'<text x="14" y="{H//2}" font-size="11" font-family="Arial" fill="#555">{ylabel}</text></svg>')
    open(path, "w", encoding="utf-8").write("\n".join(s))
    print("OK", path)

def main():
    if os.path.exists("resumen_metricas.json"):
        m = json.load(open("resumen_metricas.json", encoding="utf-8"))
        svg_bar("fig_matriz_confusion.svg", "Figura 4.x - Resultados de clasificacion (4.3.2)",
            [("VP", m["vp"], "#16a34a"), ("VN", m["vn"], "#2563eb"),
             ("FP", m["fp"], "#f59e0b"), ("FN", m["fn"], "#dc2626")], "cantidad")
    else:
        print("Falta resumen_metricas.json -> ejecutar evaluar_clasificacion.py primero")
    if os.path.exists("resultados_piloto.csv"):
        rows = list(csv.DictReader(open("resultados_piloto.csv", encoding="utf-8")))
        # espera columnas: indicador,antes_pct,despues_pct
        for r in rows:
            pass
        antes = sum(float(r["antes_pct"]) for r in rows) / len(rows) if rows else 0
        desp = sum(float(r["despues_pct"]) for r in rows) / len(rows) if rows else 0
        svg_bar("fig_piloto_antes_despues.svg", "Figura 4.x - Piloto educativo antes vs despues (4.4.2)",
            [("Antes", round(antes, 1), "#94a3b8"), ("Despues", round(desp, 1), "#16a34a")], "% aciertos")
    else:
        print("Falta resultados_piloto.csv -> ver piloto/plantilla_resultados_piloto.csv")
    print("Listo. Insertar los SVG en el Word como Figuras del capitulo 4.")

if __name__ == "__main__":
    main()
