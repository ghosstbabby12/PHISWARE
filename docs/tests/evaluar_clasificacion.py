#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PHISHWARE - Evaluacion tecnica 4.3.2 (clasificacion) y 4.3.3 (tiempos).
Llena: Tabla 4.3.1 (conteo), Tabla 4.3.2 (VP/VN/FP/FN + precision), Tabla 4.3.3 (tiempo promedio).

Uso:
  python evaluar_clasificacion.py --base http://localhost:8080/api --csv urls_prueba.csv --out resultados_clasificacion.csv
  # El script hace login o usa --token. Si no hay usuario, crea uno con --register.
  python evaluar_clasificacion.py --register --username tesista --email tesista@phishware.com --password 'Tesista@2026'

Mapeo decision (documento 4.2 / HU-05..HU-08):
  DANGEROUS o SUSPICIOUS + isPhishing=true  -> predicho = phishing
  SAFE                                      -> predicho = legitima
  Regla documento: umbrales >=50 DANGEROUS, >=20 SUSPICIOUS (ver UrlAnalysisService.calculateCombinedRisk).

Requiere: pip install requests  (solo stdlib + requests, SIN matplotlib obligatorio)
"""
import argparse, csv, json, sys, time, getpass
from collections import Counter
import urllib.request  # fallback si no hay requests

try:
    import requests
    HAS_REQUESTS = True
except ImportError:
    HAS_REQUESTS = False
    print("ERROR: pip install requests", file=sys.stderr); sys.exit(1)

def api_login(base, user_or_email, password):
    r = requests.post(f"{base}/auth/login", json={"usernameOrEmail": user_or_email, "password": password}, timeout=15)
    if r.status_code != 200:
        raise RuntimeError(f"Login fallo {r.status_code}: {r.text[:300]}")
    return r.json()["accessToken"]

def api_register(base, username, email, password):
    r = requests.post(f"{base}/auth/register",
        json={"username": username, "email": email, "password": password,
              "firstName": "Tesista", "lastName": "PhishWare"}, timeout=15)
    if r.status_code not in (200, 201):
        # puede que ya exista -> intentar login
        print(f"[register] {r.status_code} {r.text[:200]} -> intento login")
        return api_login(base, email, password)
    return r.json()["accessToken"]

def analizar(base, token, url):
    t0 = time.perf_counter()
    try:
        r = requests.post(f"{base}/analysis", json={"url": url},
            headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"}, timeout=60)
        dt_ms = int((time.perf_counter() - t0) * 1000)
        if r.status_code != 200:
            return {"ok": False, "http": r.status_code, "error": r.text[:300], "http_ms": dt_ms}
        j = r.json()
        nivel = (j.get("riskLevel") or "").upper()
        predicho = "phishing" if (nivel in ("DANGEROUS", "SUSPICIOUS") or j.get("isPhishing")) else "legitima"
        return {"ok": True, "http": 200, "riskLevel": nivel,
                "riskScore": j.get("riskScore"), "isPhishing": j.get("isPhishing"),
                "analysisTimeMs": j.get("analysisTimeMs"), "predicho": predicho,
                "http_ms": dt_ms, "domain": j.get("domain")}
    except Exception as e:
        return {"ok": False, "http": -1, "error": str(e)[:300], "http_ms": int((time.perf_counter()-t0)*1000)}

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default="http://localhost:8080/api")
    ap.add_argument("--csv", default="urls_prueba.csv")
    ap.add_argument("--out", default="resultados_clasificacion.csv")
    ap.add_argument("--token", default="")
    ap.add_argument("--register", action="store_true")
    ap.add_argument("--username", default="tesista")
    ap.add_argument("--email", default="tesista@phishware.com")
    ap.add_argument("--password", default="")
    args = ap.parse_args()

    token = args.token
    if not token:
        if args.register:
            pwd = args.password or getpass.getpass("Password nuevo usuario (min 8, May+min+num+especial): ")
            token = api_register(args.base, args.username, args.email, pwd)
            print("[ok] usuario listo, token obtenido")
        else:
            # intenta login con admin seed si existe
            print("Tip: usa --register la primera vez o --token <JWT>")
            print("Intentando login admin seed admin@phishware.com ...")
            try:
                token = api_login(args.base, "admin@phishware.com", "Admin@phishware2024")
            except Exception as e:
                print(f"No se pudo autenticar: {e}"); sys.exit(2)

    # health check
    try:
        h = requests.get(f"{args.base}/actuator/health", timeout=10)
        print(f"[health] {h.status_code} {h.text[:120]}")
    except Exception as e:
        print(f"[health] no responde: {e}"); sys.exit(3)

    rows = list(csv.DictReader(open(args.csv, encoding="utf-8")))
    print(f"[info] URLs a evaluar: {len(rows)}")
    out = []
    for i, row in enumerate(rows, 1):
        url = row["url"].strip()
        esperado = row["tipo_esperado"].strip().lower()  # phishing | legitima
        res = analizar(args.base, token, url)
        if res.get("ok"):
            pred = res["predicho"]
            acierto = "VP" if (esperado == "phishing" and pred == "phishing") else \
                      "VN" if (esperado == "legitima" and pred == "legitima") else \
                      "FP" if (esperado == "legitima" and pred == "phishing") else "FN"
        else:
            pred, acierto = "ERROR", "ERROR"
        out.append({"n": i, "url": url, "esperado": esperado, "predicho": pred,
            "acierto": acierto, "riskLevel": res.get("riskLevel", ""),
            "riskScore": res.get("riskScore", ""), "analysisTimeMs": res.get("analysisTimeMs", res.get("http_ms", "")),
            "http": res.get("http", ""), "fuente": row.get("fuente", ""), "error": res.get("error", "")})
        print(f"{i:3d}/{len(rows)} {acierto:5s} esp={esperado:8s} pred={pred:8s} {res.get('riskLevel','')} {res.get('riskScore','')} | {url[:70]}")
        time.sleep(0.3)  # no saturar VirusTotal free (500/dia)

    with open(args.out, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(out[0].keys())); w.writeheader(); w.writerows(out)

    c = Counter(r["acierto"] for r in out)
    vp, vn, fp, fn = c.get("VP", 0), c.get("VN", 0), c.get("FP", 0), c.get("FN", 0)
    total = vp + vn + fp + fn
    precision = (vp + vn) / total if total else 0
    prec_ph = vp / (vp + fp) if (vp + fp) else 0
    recall = vp / (vp + fn) if (vp + fn) else 0
    tiempos = [int(r["analysisTimeMs"]) for r in out if str(r["analysisTimeMs"]).isdigit()]
    tprom = sum(tiempos) / len(tiempos) if tiempos else 0
    print("\n===== PARA PEGAR EN DOCUMENTO =====")
    print(f"4.3.1: Total={len(rows)} | phishing={sum(1 for r in out if r['esperado']=='phishing')} | legitimas={sum(1 for r in out if r['esperado']=='legitima')}")
    print(f"4.3.2: VP={vp} ({vp/total:.1%}) | VN={vn} ({vn/total:.1%}) | FP={fp} ({fp/total:.1%}) | FN={fn} ({fn/total:.1%}) | Precision general={precision:.1%} | Precision phishing={prec_ph:.1%} | Recall={recall:.1%}")
    print(f"4.3.3: Tiempo promedio={tprom:.0f} ms | max={max(tiempos) if tiempos else 0} ms | HU-NF01 (<5000ms): {'CUMPLE' if tprom < 5000 else 'NO CUMPLE'}")
    print(f"CSV detalle: {args.out}")
    # resumen JSON para generar_graficas.py
    json.dump({"vp": vp, "vn": vn, "fp": fp, "fn": fn, "precision": precision,
               "precision_phishing": prec_ph, "recall": recall, "t_prom_ms": tprom,
               "total": len(rows)}, open("resumen_metricas.json", "w"), indent=2)
    print("resumen_metricas.json generado -> usar con generar_graficas.py")

if __name__ == "__main__":
    main()
