#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PHISHWARE - Descarga del conjunto de prueba REAL (numeral 4.4.1 del plan v1.1).

- Phishing: feed publico OpenPhish (https://openphish.com/feed.txt), tomado el
  mismo dia de la prueba (los sitios de phishing duran horas o pocos dias).
- Legitimas: lista Tranco (https://tranco-list.eu/top-1m.csv.zip) + sitios
  colombianos de referencia (bancos, gobierno, comercio).

Uso:
  pip install requests
  python build_dataset.py --max-por-clase 200 --out urls_prueba_real.csv
  python build_dataset.py --max-por-clase 200 --out urls_prueba_real.csv --insecure

Salida CSV: url,tipo_esperado,fuente,fecha_descarga
  tipo_esperado: phishing | legitima
  fuente: openphish | tranco | banco_co | gob_co | ...

Criterios: minimo 200 URL por clase; registra fecha/hora de descarga;
las sinteticas de urls_prueba.csv quedan SOLO como prueba unitaria de reglas.
"""
import argparse
import csv
import datetime as dt
import io
import sys
import zipfile

try:
    import requests
except ImportError:
    print("ERROR: pip install requests", file=sys.stderr)
    sys.exit(1)

OPENPHISH_FEED = "https://openphish.com/feed.txt"
TRANCO_TOP1M = "https://tranco-list.eu/top-1m.csv.zip"

COLOMBIANAS = [
    ("https://www.bancolombia.com", "banco_co"),
    ("https://www.davivienda.com", "banco_co"),
    ("https://www.nequi.com.co", "banco_co"),
    ("https://www.bbva.com.co", "banco_co"),
    ("https://www.dian.gov.co", "gob_co"),
    ("https://www.gov.co", "gob_co"),
    ("https://www.mercadolibre.com.co", "ecommerce_co"),
    ("https://www.elespectador.com", "prensa_co"),
    ("https://www.unal.edu.co", "edu_co"),
    ("https://www.udenar.edu.co", "edu_co"),
    ("https://www.unicauca.edu.co", "edu_co"),
    ("https://www.icesi.edu.co", "edu_co"),
    ("https://www.eltiempo.com", "prensa_co"),
    ("https://www.exito.com", "ecommerce_co"),
    ("https://www.bancodeoccidente.com.co", "banco_co"),
    ("https://www.bancodebogota.com", "banco_co"),
]


def fetch_text(url, timeout=60, verify=True):
    r = requests.get(url, timeout=timeout, verify=verify,
                      headers={"User-Agent": "PhishWare-eval/1.1"})
    r.raise_for_status()
    return r.text


def phishing_desde_openphish(feed_url, max_n, verify):
    print(f"[openphish] descargando {feed_url} ...")
    text = fetch_text(feed_url, verify=verify)
    urls = []
    for line in text.splitlines():
        u = line.strip()
        if u.startswith("http://") or u.startswith("https://"):
            urls.append(u)
        if len(urls) >= max_n:
            break
    print(f"[openphish] {len(urls)} URL de phishing")
    return urls


def legitimas_desde_tranco(tranco_url, max_n, verify):
    print(f"[tranco] descargando {tranco_url} (puede tardar) ...")
    r = requests.get(tranco_url, timeout=180, verify=verify,
                     headers={"User-Agent": "PhishWare-eval/1.1"})
    r.raise_for_status()
    urls = []
    with zipfile.ZipFile(io.BytesIO(r.content)) as z:
        name = z.namelist()[0]
        with z.open(name) as f:
            for i, raw in enumerate(io.TextIOWrapper(f, encoding="utf-8")):
                if i >= max_n:
                    break
                parts = raw.strip().split(",")
                if len(parts) == 2 and parts[1]:
                    urls.append("https://" + parts[1].strip())
    print(f"[tranco] {len(urls)} dominios legitimos")
    return urls


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--max-por-clase", type=int, default=200)
    ap.add_argument("--out", default="urls_prueba_real.csv")
    ap.add_argument("--insecure", action="store_true",
                    help="no verificar TLS (solo red de laboratorio)")
    ap.add_argument("--phishing-feed", default=OPENPHISH_FEED)
    ap.add_argument("--tranco-url", default=TRANCO_TOP1M)
    a = ap.parse_args()
    verify = not a.insecure

    feed, tranco = a.phishing_feed, a.tranco_url

    fecha = dt.datetime.now().isoformat(timespec="seconds")
    rows = []

    for u in phishing_desde_openphish(feed, a.max_por_clase, verify):
        rows.append({"url": u, "tipo_esperado": "phishing",
                     "fuente": "openphish", "fecha_descarga": fecha})

    legit = legitimas_desde_tranco(tranco, max(0, a.max_por_clase - len(COLOMBIANAS)), verify)
    for u in legit:
        rows.append({"url": u, "tipo_esperado": "legitima",
                     "fuente": "tranco", "fecha_descarga": fecha})
    for u, fuente in COLOMBIANAS:
        rows.append({"url": u, "tipo_esperado": "legitima",
                     "fuente": fuente, "fecha_descarga": fecha})

    n_ph = sum(1 for r in rows if r["tipo_esperado"] == "phishing")
    n_le = sum(1 for r in rows if r["tipo_esperado"] == "legitima")
    with open(a.out, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["url", "tipo_esperado", "fuente", "fecha_descarga"])
        w.writeheader()
        w.writerows(rows)
    print(f"[ok] {a.out}: {len(rows)} filas ({n_ph} phishing, {n_le} legitimas) el {fecha}")
    if n_ph < 200 or n_le < 200:
        print("[aviso] menos de 200 por clase: amplia --max-por-clase o reintenta (los feeds rotan)")


if __name__ == "__main__":
    main()
