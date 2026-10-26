#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PHISHWARE - Alternativa a JMeter en Python (por si el jurado pide JMeter pero aun no esta instalado).
Mide 4.3.3 + HU-NF01 + HU-NF07 sin instalar nada: solo `pip install requests`.

Uso:
  python medir_rendimiento.py --base http://localhost:8080/api --token <JWT> --n 100
  python medir_rendimiento.py --disponibilidad --minutos 30   # HU-NF07 95%

Salida: resultados_rendimiento.csv + resumen en consola listo para Tabla 4.3.3.
Los valores JMeter (Aggregate Report) son la evidencia formal; este script es el respaldo
para avanzar hoy mismo.
"""
import argparse, csv, time, statistics
import requests

URLS_CARGA = ["https://www.google.com", "https://www.bancolombia.com",
              "https://paypa1.com/login", "https://www.github.com"]

def medir_once(base, token, url):
    t0 = time.perf_counter()
    try:
        r = requests.post(f"{base}/analysis", json={"url": url},
            headers={"Authorization": f"Bearer {token}"}, timeout=60)
        dt = (time.perf_counter() - t0) * 1000
        return {"ok": r.status_code == 200, "http": r.status_code, "ms": dt,
                "server_ms": (r.json().get("analysisTimeMs") if r.status_code == 200 else None)}
    except Exception as e:
        return {"ok": False, "http": -1, "ms": (time.perf_counter() - t0) * 1000, "error": str(e)[:200]}

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default="http://localhost:8080/api")
    ap.add_argument("--token", required=True)
    ap.add_argument("--n", type=int, default=100)
    ap.add_argument("--disponibilidad", action="store_true")
    ap.add_argument("--minutos", type=int, default=30)
    ap.add_argument("--out", default="resultados_rendimiento.csv")
    a = ap.parse_args()

    if a.disponibilidad:
        total = ok = 0
        fin = time.time() + a.minutos * 60
        while time.time() < fin:
            total += 1
            try:
                r = requests.get(f"{a.base}/actuator/health", timeout=10)
                if r.status_code == 200: ok += 1
            except Exception: pass
            time.sleep(10)
        disp = ok / total * 100 if total else 0
        print(f"Disponibilidad: {ok}/{total} = {disp:.2f}% | HU-NF07 (>=95%): {'CUMPLE' if disp>=95 else 'NO CUMPLE'}")
        return

    filas = []
    for i in range(a.n):
        url = URLS_CARGA[i % len(URLS_CARGA)]
        m = medir_once(a.base, a.token, url)
        filas.append({"i": i+1, "url": url, **m})
        print(f"{i+1}/{a.n} ok={m['ok']} http={m['http']} total_ms={m['ms']:.0f} server_ms={m.get('server_ms')}")
    with open(a.out, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(filas[0].keys())); w.writeheader(); w.writerows(filas)
    ms = [f["ms"] for f in filas if f["ok"]]
    ok = sum(1 for f in filas if f["ok"])
    print("\n===== TABLA 4.3.3 =====")
    print(f"Tiempo promedio por URL: {statistics.mean(ms):.0f} ms | p50={statistics.median(ms):.0f} | p95={sorted(ms)[int(len(ms)*0.95)-1]:.0f} ms | max={max(ms):.0f} ms")
    print(f"Exito: {ok}/{len(filas)} ({ok/len(filas):.1%}) | HU-NF01 (<5000 ms promedio): {'CUMPLE' if statistics.mean(ms)<5000 else 'NO CUMPLE'}")

if __name__ == "__main__":
    main()
