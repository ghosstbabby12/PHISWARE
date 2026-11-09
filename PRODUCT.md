# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Everyday users, especially university students and staff, who receive suspicious links through WhatsApp, email, or campus portals. They want to check a link before opening it, get a quick safety assessment, and learn to recognize phishing.

## Product Purpose

PHISHWARE helps people detect and prevent phishing through multi-source URL analysis, preventive alerts, and cybersecurity education. Success means users can make a more informed decision about a suspicious link in seconds and strengthen their ability to spot phishing.

## Positioning

PHISHWARE combines six analysis sources using a noisy-OR approach, community reports, and education mapped to persuasion principles. This combination is the product's stated differentiator.

## Operating Context

PHISHWARE has distinct clients sharing a REST API under `/api`: a React web app, a native Android app, and a Manifest V3 browser extension. The web app is the currently verified client. Users commonly encounter links through messaging, email, and campus portals. The product includes JWT-based sessions and administrator workflows.

## Capabilities and Constraints

- The Spring Boot API and React web app are usable; the web app's sections have been verified through Docker.
- Available web capabilities include multi-source URL analysis, dashboard, history, education and quizzes, gamification, community reports, alerts, and administration.
- Google Safe Browsing and VirusTotal integrations are configured; RDAP does not require a key. Do not expose credentials.
- The Android app and browser extension are present in the repository but have not been tested end to end; do not describe them as verified.
- The clients are distinct applications, not a single adaptive interface.

## Brand Commitments

- Use PHISHWARE as the product name.
- Spanish is the primary UI language, with Spanish/English internationalization.
- Preserve the existing logo and shield assets and dark-mode support.
- Preserve the OWASP/NIST-informed security messaging tone.

## Evidence on Hand

- The React web app is verified via Docker; the API, URL analysis integrations, and feature set are described as usable by the product owner.
- The repository includes the product logo and shield assets, including `imagenes/Logotipo tecnológico PHISWARE geométrico.png` and the frontend's `/shield.svg` favicon.
- The Android app and Manifest V3 extension are in the repository, but end-to-end verification is outstanding.
- No accessibility conformance claim has been established; do not invent one.

## Product Principles

- Help users make a timely, informed decision before opening a suspicious link.
- Explain security assessments without overstating certainty.
- Combine multiple threat signals and community reports.
- Pair detection with practical phishing education.
- Keep Spanish-first language and the shared REST API compatible across distinct clients.
