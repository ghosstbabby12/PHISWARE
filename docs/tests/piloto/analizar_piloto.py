#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PHISHWARE - Prueba piloto del modulo educativo (numeral 4.5.2 / H2 / H0).

Entrada: CSV por participante con puntajes pre y post (0-100):
  codigo,pre,post[,I1_pre,I1_post,I2_pre,I2_post,I3_pre,I3_post,I4_pre,I4_post]

  Plantilla: docs/tests/piloto/plantilla_resultados_piloto.csv tiene los
  indicadores; agregue una fila por participante con sus puntajes.

Estadistica: prueba de Wilcoxon para muestras relacionadas (alfa = 0,05,
implementada solo con la biblioteca estandar) + tamano del efecto r = z/sqrt(n).
Sin grupo de control: la limitacion se declara en las conclusiones.

Uso:
  python analizar_piloto.py --csv piloto/resultados_piloto.csv
"""
import argparse
import csv
import math
import sys


def rank_sum_signed(diffs):
    """Wilcoxon signed-rank. Retorna (W+, n) ignorando diferencias cero."""
    nz = [(abs(d), 1 if d > 0 else -1) for d in diffs if d != 0]
    n = len(nz)
    if n == 0:
        return 0.0, 0
    order = sorted(range(n), key=lambda i: nz[i][0])
    ranks = [0.0] * n
    i = 0
    while i < n:
        j = i
        while j + 1 < n and nz[order[j + 1]][0] == nz[order[i]][0]:
            j += 1
        avg = (i + 1 + j + 1) / 2.0
        for k in range(i, j + 1):
            ranks[order[k]] = avg
        i = j + 1
    w_pos = sum(r for (_, s), r in zip(nz, ranks) if s > 0)
    return w_pos, n


def wilcoxon_p(w_pos, n):
    """Aproximacion normal bilateral (valida para n >= 10)."""
    if n < 10:
        return None
    mean = n * (n + 1) / 4.0
    var = n * (n + 1) * (2 * n + 1) / 24.0
    z = (w_pos - mean) / math.sqrt(var)
    p = math.erfc(abs(z) / math.sqrt(2.0))
    return p, z


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--csv", default="piloto/resultados_piloto.csv")
    ap.add_argument("--alfa", type=float, default=0.05)
    a = ap.parse_args()

    try:
        rows = list(csv.DictReader(open(a.csv, encoding="utf-8")))
    except FileNotFoundError:
        print(f"[error] no existe {a.csv}")
        print("Cree el archivo con columnas: codigo,pre,post")
        print("(una fila por participante con su puntaje total 0-100 antes y despues).")
        print("No se inventan resultados: complete la prueba piloto primero.")
        sys.exit(2)

    pairs = []
    for r in rows:
        try:
            pre, post = float(r["pre"]), float(r["post"])
        except (KeyError, ValueError, TypeError):
            print(f"[aviso] fila ignorada (pre/post invalidos): {r}")
            continue
        pairs.append((pre, post))

    n = len(pairs)
    print(f"[info] participantes validos: {n} (minimo 15)")
    if n == 0:
        print("[error] sin datos para analizar.")
        sys.exit(3)

    pres = [p for p, _ in pairs]
    posts = [q for _, q in pairs]
    med_pre = sorted(pres)[n // 2]
    med_post = sorted(posts)[n // 2]
    diffs = [q - p for p, q in pairs]
    mejora = sum(1 for d in diffs if d > 0)

    w_pos, n_eff = rank_sum_signed(diffs)
    print(f"Mediana antes={med_pre:.1f} despues={med_post:.1f} | mejoras: {mejora}/{n}")
    print(f"Wilcoxon: W+={w_pos:.1f} (n efectivo={n_eff}, sin empates en cero)")

    if n_eff < 10:
        print(f"[aviso] n efectivo < 10: la aproximacion normal no aplica; "
              f"reporte W+={w_pos:.1f} y amplie la muestra.")
        return

    p_val, z = wilcoxon_p(w_pos, n_eff)
    r_effect = abs(z) / math.sqrt(n_eff)
    print(f"z={z:.3f} p={p_val:.4f} (bilateral, alfa={a.alfa}) r={r_effect:.3f}")
    if p_val < a.alfa and sum(diffs) > 0:
        print("RESULTADO: diferencia significativa a favor del post -> se rechaza H0; H2 se confirma.")
    else:
        print("RESULTADO: sin diferencia significativa -> no se rechaza H0; H2 no se confirma.")


if __name__ == "__main__":
    main()
