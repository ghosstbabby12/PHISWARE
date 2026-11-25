-- ============================================================
-- V9: Contenido educativo bilingüe (ES/EN)
-- Columnas *_en opcionales: si están vacías, la API responde en español.
-- El idioma se elige con la cabecera Accept-Language (ver ContentLanguage.java).
-- Las preguntas se identifican por módulo (slug) + order_index, no por su texto.
-- ============================================================

ALTER TABLE educational_content
    ADD COLUMN title_en   VARCHAR(200),
    ADD COLUMN summary_en VARCHAR(500),
    ADD COLUMN content_en TEXT,
    ADD COLUMN tags_en    TEXT[];

ALTER TABLE quiz
    ADD COLUMN title_en       VARCHAR(200),
    ADD COLUMN description_en TEXT;

ALTER TABLE quiz_questions
    ADD COLUMN question_text_en TEXT,
    ADD COLUMN options_en       JSONB,
    ADD COLUMN explanation_en   TEXT;

-- ════════════════════════════════════════════════════════════
-- ARTÍCULOS
-- ════════════════════════════════════════════════════════════

UPDATE educational_content SET
  title_en   = $$What Is Phishing? A Complete Guide$$,
  summary_en = $$Learn what phishing is, how it works and why it is one of the most dangerous threats on the Internet.$$,
  tags_en    = ARRAY['phishing', 'security', 'basics', 'email'],
  content_en = $$# What Is Phishing?

**Phishing** is a cyberattack technique in which attackers pose as trusted entities to trick victims into handing over sensitive information such as passwords, banking details or personal data.

## How does it work?

A typical phishing attack follows these steps:

1. **Preparation**: The attacker creates a fake website or email that imitates a legitimate organization.
2. **Distribution**: The malicious link is sent massively to potential victims.
3. **Deception**: The victim clicks, believing it is legitimate.
4. **Theft**: The victim enters their credentials on the fake site.
5. **Exploitation**: The attacker uses the stolen information.

## Types of Phishing

### Email Phishing
The most common form. Mass emails with fake urgency.

### Spear Phishing
Targeted attacks on specific people or companies using personalized information.

### Clone Phishing
An exact replica of a legitimate email with modified links.

## Warning Signs

- URLs with spelling tricks (paypa1.com, amaz0n.com)
- Artificial urgency ("Your account will be suspended in 24h")
- Requests for sensitive information by email
- Unknown or suspicious sender
- Unexpected attachments

## How to protect yourself

✅ Always check the URL before entering any data
✅ Use two-factor authentication (2FA)
✅ Keep your antivirus up to date
✅ Report suspicious emails
✅ Use a password manager$$
WHERE slug = 'que-es-el-phishing-guia-completa';

UPDATE educational_content SET
  title_en   = $$Social Engineering: The Art of Deception$$,
  summary_en = $$Explore the psychological manipulation techniques attackers use to gain unauthorized access.$$,
  tags_en    = ARRAY['social engineering', 'manipulation', 'psychology', 'attacks'],
  content_en = $$# Social Engineering

**Social engineering** is the psychological manipulation of people into performing actions or disclosing confidential information. It is "human hacking".

## Psychological Principles Exploited

### 1. Authority
Attackers pose as authority figures: managers, IT technicians, bank employees.

### 2. Urgency
They create panic: "Your account will be blocked right now".

### 3. Reciprocity
They offer something of value first and then ask for something in return.

### 4. Scarcity
"Only 2 spots left, act now."

### 5. Social Proof
"Thousands of users already did it, join them."

## Common Techniques

### Pretexting
Building an elaborate story to justify a request for information.

### Baiting
Leaving infected USB drives in public places, hoping someone plugs them in.

### Quid Pro Quo
Offering a service in exchange for information.

### Tailgating
Gaining physical access by following an authorized person.

## Defense

- Verify identities before giving out information
- Do not act under emotional pressure
- Follow established security protocols
- Ongoing team training$$
WHERE slug = 'ingenieria-social-arte-del-engano';

UPDATE educational_content SET
  title_en   = $$Smishing: Phishing via SMS$$,
  summary_en = $$Discover how attackers use text messages to steal information and how to protect yourself.$$,
  tags_en    = ARRAY['smishing', 'sms', 'mobile', 'phishing'],
  content_en = $$# Smishing: Phishing via SMS

**Smishing** = SMS + Phishing. Phishing attacks carried out through text messages.

## Why is it effective?

- People trust SMS more than email
- Text messages have open rates of 98%
- Short URLs hide the real destination
- Fewer spam filters than email

## Real Examples

### Fake Banks
"ALERT: Your card has been blocked. Verify at: bit.ly/xyz"

### Package Deliveries
"Your package could not be delivered. Reschedule here: [link]"

### Fake Prizes
"You won an iPhone! Claim it within 24 hours: [link]"

## Smishing Signs

- Unknown or strange numbers
- Shortened URLs you cannot verify
- Exaggerated urgency
- Grammar mistakes
- Requests for personal data

## Protection

✅ Never click links in unsolicited SMS
✅ Call the organization directly
✅ Use official banking apps
✅ Turn on your phone's spam filter$$
WHERE slug = 'smishing-phishing-por-sms';

UPDATE educational_content SET
  title_en   = $$Vishing: Voice Phishing$$,
  summary_en = $$Learn about phishing attacks carried out over phone calls and how to spot them.$$,
  tags_en    = ARRAY['vishing', 'calls', 'phone fraud', 'voice'],
  content_en = $$# Vishing: Voice Phishing

**Vishing** = Voice + Phishing. Fraud carried out through phone calls.

## How It Works

The attacker calls pretending to be:
- A bank technician
- Microsoft or Apple support
- A government employee
- A company representative

## Vishing Techniques

### Caller ID Spoofing
Faking the caller's number so the call looks legitimate.

### Authentication Theft
"We are calling to verify your identity, please tell me the code you just received by SMS."

### Fear of Consequences
"Your social security number has been compromised, we need to act now."

## Warning Signs

- Unsolicited "tech support" calls
- Requests for verification codes
- Urgency and threats
- Requests for remote access to your device
- Requests for payment by unusual methods (gift cards, cryptocurrency)

## Defense

- Hang up and call the official number
- Never share 2FA codes over the phone
- Never grant remote access on unsolicited calls
- Register your number on do-not-call lists$$
WHERE slug = 'vishing-phishing-por-voz';

UPDATE educational_content SET
  title_en   = $$10 Cybersecurity Best Practices$$,
  summary_en = $$The 10 essential actions every user should take to browse the Internet safely.$$,
  tags_en    = ARRAY['best practices', 'passwords', '2FA', 'basic security'],
  content_en = $$# 10 Cybersecurity Best Practices

## 1. Strong, Unique Passwords
Use passwords of at least 12 characters combining letters, numbers and symbols. A different password for every service.

## 2. Two-Factor Authentication (2FA)
Turn on 2FA for all your important accounts. Even if your password is stolen, attackers cannot get in.

## 3. Constant Updates
Keep your operating system, browser and apps up to date. Patches fix vulnerabilities.

## 4. Password Manager
Use tools such as Bitwarden or 1Password to generate and store secure passwords.

## 5. VPN on Public Networks
On public Wi-Fi, use a VPN to encrypt your traffic.

## 6. Check URLs Before Clicking
Always review the full URL. A single changed character can take you to a malicious site.

## 7. Do Not Open Suspicious Attachments
Attachments can contain malware. Check the sender before opening them.

## 8. Backups
Make regular backups. Ransomware loses its power if you have copies of your data.

## 9. Privacy on Social Media
Limit the personal information you make public. Attackers use OSINT for targeted attacks.

## 10. Continuous Learning
Threats evolve constantly. Stay informed about new attack techniques.$$
WHERE slug = '10-buenas-practicas-ciberseguridad';

UPDATE educational_content SET
  title_en   = $$OWASP Top 10: The Web Threats That Enable Phishing$$,
  summary_en = $$How the OWASP Top 10 vulnerabilities make phishing attacks more convincing, and how PHISHWARE defends against them.$$,
  tags_en    = ARRAY['owasp', 'top10', 'phishing', 'web-security', 'vulnerabilities'],
  content_en = $$## What is the OWASP Top 10?

The **Open Web Application Security Project (OWASP)** publishes, every few years, the list of the 10 most critical vulnerabilities in web applications. Phishing attackers exploit these same vulnerabilities to make their attacks more convincing and harder to detect.

## How OWASP relates to phishing

### A01 — Broken Access Control
Phishing sites often imitate portals where **broken access control** lets them escalate privileges. If a real bank has this vulnerability, attackers exploit it to redirect users to fake pages.

**Example:** An email saying "Your account was compromised, log in HERE to verify" leads to a URL similar to the real bank's but with a different subdomain.

### A03 — Injection
Attackers use injection techniques to modify legitimate URLs and redirect to fake sites. **Tampered query parameters** can execute code in the user's browser.

**Example:** `https://real-bank.com/redirect?url=https://fake-bank.xyz`

### A05 — Security Misconfiguration
Misconfigured servers let attackers host fraudulent content. Missing security headers such as `X-Frame-Options` enable **clickjacking** attacks that visually deceive the user.

### A07 — Authentication Failures
Phishing is, at its core, an attack aimed at stealing authentication credentials. Fake sites imitate login pages exactly to capture usernames and passwords.

**Warning signs:**
- The URL does not exactly match the official domain
- The SSL certificate belongs to an unknown entity or does not exist
- The page asks for more data than usual

### A10 — Server-Side Request Forgery (SSRF)
SSRF lets an attacker make the victim server send HTTP requests to internal resources. In the context of phishing, it is used to exfiltrate data from internal corporate networks.

## How PHISHWARE protects you

PHISHWARE implements specific controls for each OWASP category:
- **Input sanitization** (A03) on every analyzed URL
- **Scheme validation** that blocks open redirects (A01)
- **Rate limiting** to prevent mass abuse (A04)
- **HTTP security headers** on every response (A05)
- **SSRF blocking** before calling external APIs (A10)

## Personal security checklist

- [ ] Check that the URL starts with `https://` and the padlock is present
- [ ] Make sure the domain is exactly right (no variations)
- [ ] Be wary of emails that create urgency ("your account will be suspended")
- [ ] Never click "forgot password" from a link received by email
- [ ] Use a password manager: it will warn you if the URL does not match$$
WHERE slug = 'owasp-top-10-amenazas-web-phishing';

UPDATE educational_content SET
  title_en   = $$OWASP A03: Injection and Phishing — How Attackers Manipulate URLs$$,
  summary_en = $$Open redirects, HTML injection, homograph domains and subdomain tricks: learn to read a URL like an attacker.$$,
  tags_en    = ARRAY['owasp', 'injection', 'url-manipulation', 'homograph', 'phishing'],
  content_en = $$## Injection in the context of phishing

**Injection** ranks third in the OWASP Top 10:2021. In phishing, attackers use injection techniques in many ways to deceive both users and security systems.

## Types of injection used in phishing

### 1. URL parameter injection
Attackers modify redirect parameters in legitimate URLs:

```
https://www.google.com/url?q=https://phishing-site.xyz
https://l.facebook.com/l.php?u=https://fake-login.com
```

Users see "google.com" and trust the link, without noticing the redirect at the end.

### 2. HTML injection in emails
The email body contains injected HTML that shows a URL different from the link's real target:

```html
<a href="https://malicious-site.xyz">https://official-bank.com</a>
```

The user sees `official-bank.com` but the link goes to the malicious site.

### 3. Unicode injection (Homograph Attack)
Attackers use Unicode characters that look identical to Latin letters:

| What you see | What it really is | Difference |
|------------|---------|------------|
| paypal.com | pаypal.com | The "а" is Cyrillic (U+0430) |
| apple.com | аpple.com | The "а" is Cyrillic |
| microsoft.com | micrоsoft.com | The "о" is Cyrillic (U+043E) |

### 4. Subdomain Injection
```
https://secure-login.paypal.com.attacker.xyz/verify
```
The browser resolves the domain from the right: the real domain is `attacker.xyz`.

## How to spot an injected URL

**Rule 1:** Read the URL from right to left, before the first `/`
**Rule 2:** Check every character in the domain — especially look-alike letters
**Rule 3:** Paste the link into https://phishware.pro before clicking
**Rule 4:** In emails, hover over the link without clicking

## Technical control in PHISHWARE

```java
// OWASP A03: PHISHWARE blocks injection patterns in URLs
owaspValidator.validateUrlForInjection(url);
// Detects: SQL Injection, Command Injection, UNION SELECT, etc.
```

The service also detects homograph attacks by normalizing Unicode before the analysis.$$
WHERE slug = 'owasp-a03-inyeccion-phishing-manipulacion-urls';

UPDATE educational_content SET
  title_en   = $$OWASP A07: Authentication Failures — Phishing's Main Target$$,
  summary_en = $$Why stolen credentials are the #1 goal of phishing and how NIST SP 800-63B helps you protect them.$$,
  tags_en    = ARRAY['owasp', 'authentication', 'credentials', 'mfa', 'passwords'],
  content_en = $$## Why authentication is target #1

OWASP A07:2021 — *Identification and Authentication Failures* describes exactly what phishing tries to exploit: **stealing authentication credentials**.

According to the Verizon DBIR 2023 report, **83% of data breaches** involve stolen credentials. Phishing is the number one theft method.

## How credential theft works

### Phase 1 — Reconnaissance
The attacker picks the service to impersonate (bank, email, social network) and studies the original login page.

### Phase 2 — Cloning
Tools such as *SET (Social Engineering Toolkit)* or *Evilginx* can:
- Clone the login page pixel by pixel
- Act as a transparent proxy intercepting credentials in real time
- Capture even MFA (Multi-Factor Authentication) tokens

### Phase 3 — Distribution
The malicious URL is spread through:
- Email (personalized spear phishing)
- SMS (smishing)
- Direct messages on social networks
- Paid search-engine ads (SEO poisoning)

### Phase 4 — Capture
The user enters their credentials, the attacker receives them in real time and can:
- Log in immediately, before the user notices anything
- Change the password and lock out the legitimate user
- Exfiltrate sensitive data

## NIST SP 800-63B: How to protect your credentials

The NIST password standard recommends:

| NIST recommendation | Description |
|-------------------|-------------|
| At least 8 characters | For regular accounts |
| At least 15 characters | For privileged accounts |
| No arbitrary complexity rules | Favors length over complexity |
| Check against blocklists | Block known passwords |
| No periodic expiration | Only when compromise is detected |

## Signs of a fake login site

1. **Different URL:** `login-paypal-secure.com` instead of `paypal.com`
2. **No HTTPS or an invalid certificate**
3. **Asks for extra information:** Phone number, security question, PIN
4. **Slightly different design:** Fonts, colors, logos with small differences
5. **No autofill:** Password managers do not recognize the domain

## Your defense plan

- **Turn on MFA/2FA** on all your important accounts
- Use a **password manager** — it cannot be fooled by fake domains
- Set up **login alerts** on your accounts
- Never reuse the same password across sites
- Consider a **physical security key** (YubiKey) for critical accounts$$
WHERE slug = 'owasp-a07-fallos-autenticacion-objetivo-phishing';

UPDATE educational_content SET
  title_en   = $$NIST Cybersecurity Framework: Your Defense Framework Against Phishing$$,
  summary_en = $$The five NIST CSF functions — Identify, Protect, Detect, Respond, Recover — applied to phishing.$$,
  tags_en    = ARRAY['nist', 'csf', 'framework', 'cybersecurity', 'risk-management'],
  content_en = $$## What is the NIST Cybersecurity Framework?

The **National Institute of Standards and Technology (NIST)** published the Cybersecurity Framework (CSF) as voluntary guidance so organizations of any size can manage and reduce cybersecurity risk.

The framework is organized into **5 core functions** that form a continuous improvement cycle:

```
IDENTIFY → PROTECT → DETECT → RESPOND → RECOVER
    ↑                                        ↓
    └─────────── (continuous cycle) ─────────┘
```

## The 5 Functions Applied to Phishing

### 🔍 IDENTIFY — ID
*"Develop the organizational understanding to manage risk"*

In the context of phishing:
- **ID.AM:** Inventory which personal and corporate data could be targeted
- **ID.RA:** Assess the risk of receiving phishing by email, SMS or calls
- **ID.SC:** Understand the digital communications supply chain

**In PHISHWARE:** The heuristic engine classifies each URL by risk indicators before querying external APIs.

### 🛡️ PROTECT — PR
*"Implement the appropriate safeguards"*

- **PR.AC:** Control who accesses your systems and data
- **PR.AT:** Train staff on phishing and social engineering
- **PR.DS:** Protect data in transit and at rest
- **PR.IP:** Implement information security policies

**In PHISHWARE:** JWT authentication, BCrypt for passwords, HTTPS, HTTP security headers.

### 🔎 DETECT — DE
*"Develop activities to identify the occurrence of an event"*

- **DE.AE:** Detect anomalies and security events
- **DE.CM:** Continuously monitor suspicious activity
- **DE.DP:** Keep detection processes up to date

**In PHISHWARE:** Simultaneous queries to Google Safe Browsing and VirusTotal, plus heuristic analysis of phishing indicators.

### 🚨 RESPOND — RS
*"Develop activities to take action when an incident is detected"*

- **RS.CO:** Communicate the incident to the appropriate parties
- **RS.AN:** Analyze the incident to understand its impact
- **RS.MI:** Mitigate the impact of the incident

**In PHISHWARE:** Automatic alerts, real-time notifications, personalized recommendations.

### 🔄 RECOVER — RC
*"Develop activities to maintain resilience"*

- **RC.RP:** Execute the recovery plan
- **RC.IM:** Improve based on lessons learned
- **RC.CO:** Coordinate recovery communications

**In PHISHWARE:** Gamified education module — learn from incidents to prevent future attacks.

## Why does the NIST CSF matter?

1. **Common language:** Lets technical teams and executives talk about risk
2. **Scalable:** Applies equally to individuals, SMBs and large corporations
3. **Non-prescriptive:** Describes WHAT to do, not exactly HOW
4. **Internationally recognized:** The basis of many standards and regulations

## Your personal NIST action plan

| Function | Personal action |
|---------|----------------|
| IDENTIFY | List your critical accounts (bank, email, work) |
| PROTECT | Turn on MFA everywhere, use unique passwords |
| DETECT | Install PHISHWARE, review login alerts |
| RESPOND | If you fell for phishing: change passwords, notify your bank |
| RECOVER | Learn to identify the attack type to avoid the next one |$$
WHERE slug = 'nist-cybersecurity-framework-defensa-phishing';

UPDATE educational_content SET
  title_en   = $$NIST SP 800-30: Assessing Phishing Risk in Your Organization$$,
  summary_en = $$Apply the NIST risk assessment methodology (likelihood × impact) to phishing, step by step.$$,
  tags_en    = ARRAY['nist', 'sp800-30', 'risk', 'assessment', 'methodology'],
  content_en = $$## What is NIST SP 800-30?

Special Publication **NIST SP 800-30 Rev. 1** — *Guide for Conducting Risk Assessments* — provides guidance for assessing information security risks. PHISHWARE uses this methodology to compute the risk score of every analyzed URL.

## The 4 steps of a risk assessment

### Step 1 — Prepare for the assessment
Define the context: which assets are you protecting? What is your threat profile?

For personal phishing:
- Assets: bank accounts, corporate email, social networks, customer data
- Threats: spear phishing, whaling, smishing, vishing
- Vulnerabilities: no MFA, weak passwords, not knowing the warning signs

### Step 2 — Conduct the assessment
Identify threat sources and events. NIST SP 800-30 defines:

**Likelihood:**
| Level | NIST definition | Phishing example |
|-------|----------------|-----------------|
| Very high | Attack almost certain | C-suite executive without MFA |
| High | High probability | User whose data appears in known breaches |
| Moderate | Some probability | Average corporate user |
| Low | Low probability | User with good security practices |

**Impact:**
| Level | NIST definition | Phishing example |
|-------|----------------|-----------------|
| Very high | Severe or catastrophic harm | Complete identity theft |
| High | Significant harm | Bank account emptied |
| Moderate | Moderate harm | Access to corporate email |
| Low | Limited harm | Exposure of non-sensitive data |

### Step 3 — Communicate the results
Risk is calculated as:

```
Risk = Likelihood × Impact
```

**How PHISHWARE implements this calculation:**
```
Risk = 1 − Π (1 − pᵢ)
```
Each source (Google Safe Browsing, VirusTotal, URL heuristics, page content and domain age) contributes a probability pᵢ. They are combined so that a strong signal is never diluted and several weak signals reinforce each other.

### Step 4 — Maintain the assessment
Risk changes constantly. PHISHWARE keeps the analysis current by querying:
- Google Safe Browsing (updated by Google every 30 minutes)
- VirusTotal (collective intelligence from 70+ antivirus engines)
- Local heuristic engine (instant analysis with no external dependencies)

## PHISHWARE risk matrix

```
         IMPACT
         LOW     MEDIUM  HIGH    CRITICAL
L  LOW  │  🟢  │  🟢  │  🟡  │  🟠  │
I  MED. │  🟢  │  🟡  │  🟠  │  🔴  │
K  HIGH │  🟡  │  🟠  │  🔴  │  🔴  │
E CRIT. │  🟠  │  🔴  │  🔴  │  🔴  │
```

## Applying it in your company

1. **Identify critical assets** — which information must never be stolen?
2. **Map phishing vectors** — email, SMS, calls, USB, QR codes
3. **Evaluate current controls** — do you have spam filters? MFA? training?
4. **Calculate residual risk** — after the controls, how much risk is left?
5. **Document and review** — at least yearly or after an incident

## Additional resources

- [NIST SP 800-30 Rev.1](https://csrc.nist.gov/publications/detail/sp/800-30/rev-1/final)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [NIST SP 800-63B — Digital Identity](https://pages.nist.gov/800-63-3/sp800-63b.html)$$
WHERE slug = 'nist-sp-800-30-evaluacion-riesgos-phishing';

UPDATE educational_content SET
  title_en   = $$OWASP and NIST Best Practices: Your Complete Cyber Hygiene Guide$$,
  summary_en = $$Ten everyday security habits, backed by OWASP and NIST, that stop most attacks before they happen.$$,
  tags_en    = ARRAY['nist', 'owasp', 'best-practices', 'cyber-hygiene', 'complete-guide'],
  content_en = $$## Cyber hygiene: the concept that unites OWASP and NIST

**Cyber hygiene** is the set of everyday security practices that, like washing your hands, prevent most problems before they happen.

OWASP defines the technical vulnerabilities you must avoid; NIST defines the management process you must follow. Together they form a complete defense.

## The 10 essential practices

### 1. Strong, unique passwords (OWASP A07 + NIST PR.AC-1)
- Use passwords of **at least 16 characters** (NIST favors length over complexity)
- **Never reuse** the same password across services
- Use a **password manager**: Bitwarden (free), 1Password, KeePass
- A manager cannot be fooled by fake domains — a key defense against phishing

### 2. Multi-factor authentication (OWASP A07 + NIST PR.AC-7)
Turning on MFA reduces the risk of compromise by **99.9%** (Microsoft, 2019):
- **Prefer:** Physical security keys (FIDO2/WebAuthn) > TOTP apps > SMS
- Turn on MFA for: email, bank, social networks, work, password managers

### 3. Check URLs before clicking (OWASP A03 + NIST DE.CM-1)
Before entering credentials, check:
- [ ] Protocol: `https://` (with S)
- [ ] Exact domain: `paypal.com`, not `paypal-secure.com`
- [ ] No suspicious subdomains: `secure.paypal.com` ✅ vs `paypal.com.login-secure.xyz` ❌
- [ ] Paste the URL into PHISHWARE if in doubt

### 4. Be wary of urgency (NIST ID.RA-1)
Phishing attacks exploit the **urgency cognitive bias**:
- "Your account will be suspended in 24 hours"
- "Payment pending — confirm now"
- "Suspicious activity detected — verify immediately"

**Golden rule:** If something creates urgency, wait 5 minutes. Real problems do not disappear if you wait.

### 5. Update software regularly (OWASP A06 + NIST PR.IP-12)
60% of breaches exploit vulnerabilities that already had patches:
- Turn on **automatic updates** for your OS and browser
- Keep your **antivirus and EDR** up to date
- Update browser extensions and installed apps

### 6. Backups (NIST RC.RP-1)
If you fall for ransomware delivered by phishing:
- **3-2-1** rule: 3 copies, 2 different media, 1 offsite/cloud
- Regularly test that you can restore from the backup
- Cloud backups without versioning do NOT protect you from ransomware

### 7. Review app permissions (OWASP A01 + NIST PR.AC-3)
- Audit which apps can access your email and social networks
- Revoke access for apps you no longer use (OAuth tokens)
- Apply the **least privilege** principle: do not grant more permissions than needed

### 8. Separate your accounts (NIST PR.AC-4)
- **Do not use the same email** for work and personal life
- Create a disposable email account for third-party sign-ups
- Use **email aliases** (SimpleLogin, Addy.io) to contain breaches

### 9. Report attacks (OWASP A09 + NIST RS.CO-2)
Reporting is not only for organizations:
- **Colombia:** incidentes@colcert.gov.co
- **Spain:** incidencias@incibe-cert.es
- **Mexico:** cgsi@sspc.gob.mx
- Report phishing URLs to: Google Safe Browsing, VirusTotal, PhishTank

### 10. Continuous learning (NIST RC.IM-1)
Phishing evolves constantly:
- Take the PHISHWARE quizzes regularly
- Share this knowledge with family and colleagues
- Subscribe to security alerts: CISA, INCIBE, COLCERT

## Monthly cyber hygiene checklist

```
□ Review PHISHWARE alerts from the last few weeks
□ Check whether your data appears in recent breaches (HaveIBeenPwned)
□ Audit apps with OAuth access to your accounts
□ Review active sessions on critical accounts
□ Update passwords on accounts without MFA
□ Complete a phishing quiz in PHISHWARE
```$$
WHERE slug = 'buenas-practicas-owasp-nist-ciberhigiene';

-- ── Microlecciones de principios de persuasión ──────────────────────────────

UPDATE educational_content SET
  title_en   = $$Authority: phishing's favorite disguise$$,
  summary_en = $$Learn to recognize when a message abuses authority so you obey without checking.$$,
  tags_en    = ARRAY['authority', 'Cialdini', 'micro-lesson', 'verification'],
  content_en = $$# Authority: phishing's favorite disguise

Attackers pose as your bank, your boss or an official agency (tax office, police) because we tend to obey authority without questioning it.

## Signs

- Messages claiming to come from your bank or a superior.
- Copied logos and formal language.
- They ask you to act now, without verifying through another channel.

## What to do

1. Always verify through an official channel that you open yourself (app, website or the phone number on the back of your card).
2. No bank asks for your passwords by email, SMS or phone call.
3. If in doubt, close the message and contact the organization directly.$$
WHERE slug = 'micro-principio-autoridad';

UPDATE educational_content SET
  title_en   = $$Urgency and scarcity: deciding without thinking$$,
  summary_en = $$Haste is the scammer's tool: learn to pause before you click.$$,
  tags_en    = ARRAY['urgency', 'scarcity', 'Cialdini', 'micro-lesson'],
  content_en = $$# Urgency and scarcity: deciding without thinking

"Your account will be blocked in 24 hours", "only 2 spots left": urgency and scarcity switch off your critical thinking.

## Signs

- Countdowns and threats of blocking.
- Offers that "expire today".
- Pressure not to consult anyone.

## What to do

1. Breathe: no real emergency is solved by clicking a link.
2. Verify through the official channel, without rushing.
3. A domain registered a few days ago with "urgent offers" is almost always a scam.$$
WHERE slug = 'micro-principio-escasez';

UPDATE educational_content SET
  title_en   = $$Liking and trust: the invisible hook$$,
  summary_en = $$If something feels too friendly or familiar, verify anyway: trust can be faked too.$$,
  tags_en    = ARRAY['liking', 'trust', 'typosquatting', 'micro-lesson'],
  content_en = $$# Liking and trust: the invisible hook

Attackers imitate the tone of a friend, a colleague or a beloved brand so you lower your guard: look-alike domains (typosquatting), familiar senders and copied designs.

## Signs

- Addresses with swapped letters (paypa1, micros0ft).
- Messages "from a friend" with odd links.
- Certificates or padlocks that guarantee nothing on their own.

## What to do

1. Read the domain letter by letter.
2. If a contact sends something strange, ask them through another channel.
3. The padlock only means encryption, not legitimacy.$$
WHERE slug = 'micro-principio-simpatia';

UPDATE educational_content SET
  title_en   = $$Social proof: if everyone falls for it, so will you$$,
  summary_en = $$Fake reviews and made-up counters push you to trust. Learn to doubt.$$,
  tags_en    = ARRAY['social proof', 'Cialdini', 'micro-lesson'],
  content_en = $$# Social proof: if everyone falls for it, so will you

"Thousands of people already did it" or perfect reviews may be fabricated to make you trust.

## Signs

- Identical or undated testimonials.
- "Download" counters with no source.
- Peer pressure in messages ("all your contacts already verified").

## What to do

1. Look for opinions outside the site itself.
2. Be suspicious of perfect unanimity.
3. Check the domain before the reviews.$$
WHERE slug = 'micro-principio-prueba-social';

UPDATE educational_content SET
  title_en   = $$Reciprocity: nothing is free$$,
  summary_en = $$Unexpected gifts and prizes create a debt that the scammer collects with your data.$$,
  tags_en    = ARRAY['reciprocity', 'prizes', 'Cialdini', 'micro-lesson'],
  content_en = $$# Reciprocity: nothing is free

Unexpected prizes, coupons and "gifts" trigger the need to return the favor: the payment is always your data.

## Signs

- Prizes you never asked for or competed for.
- "Gift" attachments (.apk, .exe, .zip).
- To "claim" them, they ask for data or small payments.

## What to do

1. If you did not take part, you did not win anything.
2. Never install attachments from unsolicited messages.
3. No real prize asks for your passwords.$$
WHERE slug = 'micro-principio-reciprocidad';

UPDATE educational_content SET
  title_en   = $$Commitment: the yes that ties you down$$,
  summary_en = $$Small steps lead to big handovers: spot the escalation before the damage.$$,
  tags_en    = ARRAY['commitment', 'consistency', 'Cialdini', 'micro-lesson'],
  content_en = $$# Commitment: the yes that ties you down

First they ask for something small (answer a survey, confirm your email) and then escalate to your credentials. Staying consistent with your first "yes" is the trap.

## Signs

- Multi-step forms that ask for more data each time.
- "Just one more detail to finish".
- Processes you cannot cancel halfway through.

## What to do

1. You can abandon any process at any time.
2. Review what you handed over at each step.
3. A legitimate site explains what each piece of data is for.$$
WHERE slug = 'micro-principio-compromiso';

UPDATE educational_content SET
  title_en   = $$Unity and identity: "your group already did it"$$,
  summary_en = $$Attacks that appeal to your group (company, university, family) seek automatic obedience.$$,
  tags_en    = ARRAY['unity', 'identity', 'Cialdini', 'micro-lesson'],
  content_en = $$# Unity and identity

Messages that appeal to your company, your university or your family ("everyone at the company already updated their details") use your group identity so you act without verifying.

## Signs

- "Internal" emails from external domains.
- Messaging groups with "official announcements".
- They ask you to forward the message to your contacts.

## What to do

1. Verify through your organization's official channel.
2. A real announcement does not arrive only via WhatsApp.
3. Do not forward without verifying: you break the chain.$$
WHERE slug = 'micro-principio-unidad';

-- ════════════════════════════════════════════════════════════
-- QUIZZES (título y descripción) — por el módulo al que pertenecen
-- ════════════════════════════════════════════════════════════

UPDATE quiz z SET title_en = v.title_en, description_en = v.description_en
FROM educational_content c, (VALUES
  ('que-es-el-phishing-guia-completa',               $$Phishing Fundamentals$$,                               $$Test your basic knowledge of phishing and how to spot it.$$),
  ('ingenieria-social-arte-del-engano',              $$Advanced Social Engineering$$,                         $$Put your understanding of advanced social engineering techniques to the test.$$),
  ('owasp-top-10-amenazas-web-phishing',             $$OWASP Quiz: Do You Know Phishing's Vulnerabilities?$$, $$Test how well you understand the link between the OWASP Top 10 and phishing attacks, and the defenses PHISHWARE implements. Read the OWASP articles first.$$),
  ('nist-cybersecurity-framework-defensa-phishing',  $$NIST CSF Quiz: Managing Phishing Risk$$,               $$Test your understanding of the NIST Cybersecurity Framework and how its 5 functions (Identify, Protect, Detect, Respond, Recover) apply to preventing and responding to phishing.$$),
  ('smishing-phishing-por-sms',                      $$Quiz: Smishing — Phishing via SMS$$,                   $$Check whether you can recognize and respond to fraudulent text messages.$$),
  ('vishing-phishing-por-voz',                       $$Quiz: Vishing — Voice Phishing$$,                      $$Evaluate how you identify and respond to fraudulent calls.$$),
  ('10-buenas-practicas-ciberseguridad',             $$Quiz: 10 Cybersecurity Best Practices$$,               $$Review the basic habits that protect you every day.$$),
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', $$OWASP A03 Quiz: Injection and URL Manipulation$$,     $$Test your ability to detect URLs manipulated with injection techniques.$$),
  ('owasp-a07-fallos-autenticacion-objetivo-phishing',$$OWASP A07 Quiz: Authentication Failures$$,            $$Evaluate what you know about credential theft and how to protect your accounts.$$),
  ('nist-sp-800-30-evaluacion-riesgos-phishing',     $$NIST SP 800-30 Quiz: Risk Assessment$$,                $$Check whether you can apply the NIST risk assessment methodology to phishing.$$),
  ('buenas-practicas-owasp-nist-ciberhigiene',       $$Quiz: Cyber Hygiene with OWASP and NIST$$,             $$Review the everyday security practices recommended by OWASP and NIST.$$)
) AS v(slug, title_en, description_en)
WHERE z.content_id = c.id AND c.slug = v.slug;

-- ════════════════════════════════════════════════════════════
-- PREGUNTAS — (slug del módulo, order_index, pregunta, opciones, explicación)
-- ════════════════════════════════════════════════════════════

UPDATE quiz_questions qq SET
  question_text_en = v.q,
  options_en       = v.o::JSONB,
  explanation_en   = v.e
FROM quiz z, educational_content c, (VALUES

  -- Fundamentos de Phishing
  ('que-es-el-phishing-guia-completa', 1,
   $$Which of the following is the MOST common indicator of a phishing email?$$,
   $${"A": "The email has a company logo", "B": "The sender uses exaggerated urgency and asks for personal data", "C": "The email is written in Spanish", "D": "The email contains images"}$$,
   $$Phishing attacks often use artificial urgency to pressure the victim into acting without thinking.$$),
  ('que-es-el-phishing-guia-completa', 2,
   $$What is "spear phishing"?$$,
   $${"A": "Phishing sent massively to millions of people", "B": "Phishing aimed specifically at an individual or organization using personalized information", "C": "Phishing carried out by SMS", "D": "Phishing carried out by phone call"}$$,
   $$Spear phishing is highly targeted and personalized, which makes it more dangerous than mass phishing.$$),
  ('que-es-el-phishing-guia-completa', 3,
   $$Which of these URLs could be a PayPal phishing attempt?$$,
   $${"A": "https://www.paypal.com/login", "B": "https://www.paypa1.com/login", "C": "https://paypal.com/security", "D": "https://api.paypal.com/v1"}$$,
   $$The URL uses the number "1" instead of the letter "l" in paypal. This typosquatting is a classic phishing technique.$$),
  ('que-es-el-phishing-guia-completa', 4,
   $$What does "smishing" mean?$$,
   $${"A": "Phishing through email", "B": "Phishing through SMS messages", "C": "Phishing through voice calls", "D": "Phishing through social networks"}$$,
   $$Smishing combines SMS + Phishing. It is especially dangerous because people trust text messages more.$$),
  ('que-es-el-phishing-guia-completa', 5,
   $$Which are warning signs of a phishing website? (Select all that apply)$$,
   $${"A": "URL with spelling tricks", "B": "Valid SSL certificate", "C": "Forms that ask for passwords by email", "D": "Design identical to the original site", "E": "Urgency to act immediately"}$$,
   $$Phishing sites use typosquatting, request credentials inappropriately and create artificial urgency. A valid SSL certificate no longer guarantees legitimacy.$$),

  -- Ingeniería Social Avanzada
  ('ingenieria-social-arte-del-engano', 1,
   $$Which psychological principle do attackers exploit when they say "You only have 10 minutes to claim your prize"?$$,
   $${"A": "Authority", "B": "Social proof", "C": "Scarcity and urgency", "D": "Reciprocity"}$$,
   $$Artificial scarcity and urgency stop the victim from thinking critically and push them to act immediately.$$),
  ('ingenieria-social-arte-del-engano', 2,
   $$What is "pretexting" in social engineering?$$,
   $${"A": "Sending emails with fake text", "B": "Creating a fictitious identity or story to manipulate the victim", "C": "Posting false information on social networks", "D": "Intercepting text communications"}$$,
   $$Pretexting means building a believable scenario (a pretext) to gain the victim's trust and obtain information.$$),
  ('ingenieria-social-arte-del-engano', 3,
   $$In social engineering, "baiting" refers to:$$,
   $${"A": "Using physical or digital bait such as infected USB drives to compromise systems", "B": "Sending malicious text messages", "C": "Spoofing the incoming caller number", "D": "Cloning banking websites"}$$,
   $$Baiting exploits human curiosity, for example by leaving infected USB drives in strategic places hoping someone plugs them in.$$),
  ('ingenieria-social-arte-del-engano', 4,
   $$What is the best defense against a vishing (voice phishing) attack?$$,
   $${"A": "Give only the last 4 digits of your card", "B": "Ask for the agent's name and continue the call", "C": "Hang up and call the organization's official number directly", "D": "Check that the call comes from a local number"}$$,
   $$Always ending the call and contacting the organization through verified official channels is the most effective defense.$$),
  ('ingenieria-social-arte-del-engano', 5,
   $$An email that seems to come from your boss asks you to make an urgent transfer without following the usual process. Which psychological principle is mainly being exploited?$$,
   $${"A": "Reciprocity", "B": "Social proof", "C": "Authority", "D": "Scarcity"}$$,
   $$The attacker poses as an authority figure so the victim obeys without verifying.$$),

  -- Quiz OWASP Top 10
  ('owasp-top-10-amenazas-web-phishing', 1,
   $$Which OWASP Top 10:2021 category directly describes the main goal of phishing?$$,
   $${"A": "A01 - Broken Access Control", "B": "A03 - Injection", "C": "A07 - Identification and Authentication Failures", "D": "A10 - Server-Side Request Forgery"}$$,
   $$OWASP A07 covers identity authentication failures and theft. Phishing seeks exactly that: stealing authentication credentials (username and password) to impersonate the victim.$$),
  ('owasp-top-10-amenazas-web-phishing', 2,
   $$What is a "homograph attack" in the context of phishing?$$,
   $${"A": "An attack that uses images to hide malicious text", "B": "Using visually identical Unicode characters to create fake domains", "C": "An attack that exactly copies the visual design of a legitimate site", "D": "Mass-sending the same phishing email to multiple victims"}$$,
   $$Homograph attacks use characters from other alphabets (such as Cyrillic) that look identical to Latin letters. For example: "pаypal.com" where the "а" is Cyrillic (U+0430), not Latin. The domain is different but looks identical.$$),
  ('owasp-top-10-amenazas-web-phishing', 3,
   $$Which of the following URLs is most likely to be a phishing attack? (OWASP A03)$$,
   $${"A": "https://paypal.com/es/signin", "B": "https://secure-paypal.com/login", "C": "https://www.paypal.com/signin?returnUrl=/home", "D": "https://developer.paypal.com/docs/api/"}$$,
   $$"secure-paypal.com" is a different domain from "paypal.com". The "secure-" prefix is a common trick: the real domain is "secure-paypal.com", not "paypal.com". The other three URLs legitimately belong to "paypal.com".$$),
  ('owasp-top-10-amenazas-web-phishing', 4,
   $$Which OWASP category protects against attacks where the server is used to scan internal networks?$$,
   $${"A": "A01 - Broken Access Control", "B": "A05 - Security Misconfiguration", "C": "A09 - Security Logging Failures", "D": "A10 - Server-Side Request Forgery (SSRF)"}$$,
   $$OWASP A10 covers SSRF: attacks where the attacker tricks the server into making HTTP requests to internal resources (192.168.x.x, 169.254.169.254 for cloud metadata, etc.). PHISHWARE blocks this vector before querying external APIs.$$),
  ('owasp-top-10-amenazas-web-phishing', 5,
   $$According to NIST SP 800-63B, which password recommendation is INCORRECT?$$,
   $${"A": "Passwords must be at least 8 characters long", "B": "Passwords should be checked against blocklists of compromised passwords", "C": "Passwords must mandatorily expire every 90 days", "D": "All ASCII and Unicode characters should be allowed"}$$,
   $$NIST SP 800-63B specifically recommends NOT forcing periodic password expiration. That requirement leads users to choose predictable passwords (Password1, Password2...). NIST only recommends changing a password when there is evidence of compromise.$$),

  -- Quiz NIST CSF
  ('nist-cybersecurity-framework-defensa-phishing', 1,
   $$You received a suspicious email and pasted the URL into PHISHWARE before clicking. Which NIST CSF function does this action belong to?$$,
   $${"A": "IDENTIFY (ID) — Identify assets and risks", "B": "PROTECT (PR) — Implement safeguards", "C": "DETECT (DE) — Detect security events", "D": "RESPOND (RS) — Respond to incidents"}$$,
   $$Actively checking a URL before clicking is a DETECTION (DE) action: you are using a tool to detect whether the URL is a risk before exposing yourself. PROTECT would be having automatic filters; IDENTIFY would be mapping the assets at risk.$$),
  ('nist-cybersecurity-framework-defensa-phishing', 2,
   $$After a phishing incident, your company decides to make MFA mandatory for all employees. Which NIST CSF function does this decision belong to?$$,
   $${"A": "IDENTIFY — Catalog the organization's assets", "B": "PROTECT — Implement safeguards for critical services", "C": "DETECT — Continuously monitor security events", "D": "RECOVER — Implement post-incident improvements"}$$,
   $$Implementing MFA is a PROTECTION safeguard (PR.AC-7: Users, devices, and other assets are authenticated). PROTECT includes all preventive controls: access control, training, data and infrastructure protection.$$),
  ('nist-cybersecurity-framework-defensa-phishing', 3,
   $$What is the correct order of the 5 functions of the NIST Cybersecurity Framework?$$,
   $${"A": "Protect → Identify → Detect → Respond → Recover", "B": "Identify → Protect → Detect → Respond → Recover", "C": "Detect → Identify → Protect → Respond → Recover", "D": "Identify → Detect → Protect → Respond → Recover"}$$,
   $$The logical order of the NIST CSF is: first IDENTIFY (know what to protect), then PROTECT (implement controls), DETECT (monitor threats), RESPOND (act on incidents) and RECOVER (return to normal and improve). Together they form a continuous cycle.$$),
  ('nist-cybersecurity-framework-defensa-phishing', 4,
   $$After an employee fell for a phishing attack, the company runs a training session on that type of attack. Which NIST CSF function mainly applies?$$,
   $${"A": "DETECT (DE) — Detecting future threats", "B": "RESPOND (RS) — Responding to the current incident", "C": "RECOVER (RC) — Improving capabilities after the incident", "D": "IDENTIFY (ID) — Identifying new risks"}$$,
   $$Post-incident training belongs to RECOVER, specifically RC.IM-1 (Recovery plans incorporate lessons learned) and RC.IM-2 (Recovery strategies are updated). The organization learns from the incident and improves for the future.$$),
  ('nist-cybersecurity-framework-defensa-phishing', 5,
   $$According to NIST SP 800-30, how is risk calculated in a formal assessment?$$,
   $${"A": "Risk = Threat + Vulnerability", "B": "Risk = Likelihood × Impact", "C": "Risk = Asset × Threat × Vulnerability", "D": "Risk = Impact ÷ Controls"}$$,
   $$NIST SP 800-30 defines Risk = f(Likelihood, Impact). Likelihood covers the chance that a threat exploits a vulnerability; impact measures the resulting harm. PHISHWARE combines its sources (Safe Browsing, VirusTotal, heuristics, content and domain age) following this idea.$$),

  -- Smishing
  ('smishing-phishing-por-sms', 1,
   $$Why do attackers use shortened URLs (like bit.ly) in smishing messages?$$,
   $${"A": "Because they load faster on phones", "B": "Because they hide the link's real destination", "C": "Because carriers require them", "D": "Because SMS does not support long URLs"}$$,
   $$A shortener hides the real domain, so the victim cannot check where the link leads.$$),
  ('smishing-phishing-por-sms', 2,
   $$You receive: "Your package could not be delivered. Reschedule here: [link]", but you were not expecting any package. What do you do?$$,
   $${"A": "Open the link just to see what it is", "B": "Reply STOP so they stop texting me", "C": "Ignore the link and check the carrier's official website or app", "D": "Forward it to my contacts to warn them"}$$,
   $$Never click links in unsolicited SMS. Always verify through an official channel that you open yourself.$$),
  ('smishing-phishing-por-sms', 3,
   $$Which are signs of smishing? (Select all that apply)$$,
   $${"A": "Sender with an unknown or strange number", "B": "Shortened URL you cannot verify", "C": "Message from a saved contact saying they arrive in 5 minutes", "D": "Exaggerated urgency to act", "E": "Grammar mistakes"}$$,
   $$Strange numbers, shortened links, urgency and grammar mistakes are the classic signs of smishing.$$),
  ('smishing-phishing-por-sms', 4,
   $$True or false: SMS is safer than email because it has better spam filters.$$,
   $${"A": "True", "B": "False"}$$,
   $$It is the other way around: SMS has fewer filters than email and a 98% open rate, which is why it is so effective for attackers.$$),
  ('smishing-phishing-por-sms', 5,
   $$You get an SMS saying your card was blocked. What is the safest way to verify it?$$,
   $${"A": "Call the number shown in the SMS", "B": "Open the bank's official app or call the number on the back of the card", "C": "Open the link only if I am on my home Wi-Fi", "D": "Reply to the SMS with the last 4 digits of the card"}$$,
   $$Always use channels you control: the official app or the number printed on your card, never the details in the message.$$),

  -- Vishing
  ('vishing-phishing-por-voz', 1,
   $$What is Caller ID spoofing?$$,
   $${"A": "Recording a call without permission", "B": "Faking the caller's number so the call looks legitimate", "C": "Blocking calls from unknown numbers", "D": "Forwarding calls to voicemail"}$$,
   $$With spoofing, the attacker makes your bank's or an official agency's number appear on your screen.$$),
  ('vishing-phishing-por-voz', 2,
   $$Someone claiming to be from your bank asks for the code you just received by SMS to verify your identity. What do you do?$$,
   $${"A": "Give it because the call seems official", "B": "Give only half of the code", "C": "Hang up: verification codes are never shared with anyone", "D": "Ask them to email it to me first"}$$,
   $$No bank will ask for a 2FA code over the phone. Whoever asks for it is trying to get into your account.$$),
  ('vishing-phishing-por-voz', 3,
   $$Which are vishing warning signs? (Select all that apply)$$,
   $${"A": "Unsolicited tech support call", "B": "They ask for remote access to your device", "C": "They ask for payment with gift cards or cryptocurrency", "D": "You called the official number shown on the bank's website", "E": "Threats of immediate consequences"}$$,
   $$If you started the call to the official number, it is not a warning sign; all the others are.$$),
  ('vishing-phishing-por-voz', 4,
   $$True or false: if the caller ID shows the bank's official number, the call is legitimate.$$,
   $${"A": "True", "B": "False"}$$,
   $$False. The number can be spoofed. Hang up and call the official number yourself.$$),
  ('vishing-phishing-por-voz', 5,
   $$A supposed Microsoft technician calls saying your computer has a virus and asks you to install a remote access program. What do you do?$$,
   $${"A": "Install it if the program is well known", "B": "Ask for their employee number and continue", "C": "Hang up: Microsoft does not make unsolicited support calls", "D": "Give them access for just 5 minutes"}$$,
   $$Tech companies do not call you unless you asked. Granting remote access hands full control of your computer to the attacker.$$),

  -- 10 Buenas Prácticas
  ('10-buenas-practicas-ciberseguridad', 1,
   $$According to the module, what is the recommended minimum password length?$$,
   $${"A": "6 characters", "B": "8 characters", "C": "12 characters", "D": "4 characters"}$$,
   $$The module recommends at least 12 characters combining letters, numbers and symbols.$$),
  ('10-buenas-practicas-ciberseguridad', 2,
   $$Why is it worth turning on two-factor authentication (2FA)?$$,
   $${"A": "Because it makes the password shorter", "B": "Because even if your password is stolen, they cannot access your account", "C": "Because it removes the need to update the system", "D": "Because it blocks all spam emails"}$$,
   $$The second factor blocks access even if the attacker knows your password.$$),
  ('10-buenas-practicas-ciberseguridad', 3,
   $$Which of these are recommended best practices? (Select all that apply)$$,
   $${"A": "Use a different password for each service", "B": "Use a VPN on public Wi-Fi networks", "C": "Postpone updates until something breaks", "D": "Make regular backups", "E": "Post your date of birth on your public profile"}$$,
   $$Unique passwords, a VPN on public networks and backups are key practices. Postponing updates and exposing personal data increase the risk.$$),
  ('10-buenas-practicas-ciberseguridad', 4,
   $$Which practice takes the most power away from a ransomware attack?$$,
   $${"A": "Using a VPN", "B": "Having backups of your data", "C": "Changing your wallpaper", "D": "Using a different browser"}$$,
   $$If you have backups, you can recover your data without paying the ransom.$$),
  ('10-buenas-practicas-ciberseguridad', 5,
   $$True or false: attackers can use public information from your social media (OSINT) to prepare targeted attacks.$$,
   $${"A": "True", "B": "False"}$$,
   $$True. That is why it is wise to limit the personal information you post.$$),

  -- OWASP A03
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', 1,
   $$In the URL https://secure-login.paypal.com.attacker.xyz/verify, what is the real domain?$$,
   $${"A": "paypal.com", "B": "secure-login.paypal.com", "C": "attacker.xyz", "D": "verify"}$$,
   $$The domain is read from right to left before the first slash: the real domain is attacker.xyz.$$),
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', 2,
   $$An email contains the link <a href='https://malicious-site.xyz'>https://official-bank.com</a>. If you click it, where does it take you?$$,
   $${"A": "To official-bank.com", "B": "To malicious-site.xyz", "C": "It depends on the browser", "D": "Nowhere, the link is broken"}$$,
   $$This is HTML injection: the visible text says one thing, but the real destination is in the href attribute.$$),
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', 3,
   $$Why is a URL like https://www.google.com/url?q=https://phishing-site.xyz dangerous?$$,
   $${"A": "Because google.com is an unsafe site", "B": "Because it uses a redirect parameter to take you to another site even though it starts with google.com", "C": "Because it does not use HTTPS", "D": "It is not dangerous, Google verified it"}$$,
   $$It is an open redirect: the victim sees a trusted domain and ends up on the attacker's site.$$),
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', 4,
   $$Which rules help identify an injected URL? (Select all that apply)$$,
   $${"A": "Read the domain from right to left before the first /", "B": "Check every character of the domain", "C": "Trust it if the URL uses HTTPS", "D": "Hover over the link without clicking", "E": "Trust it if the domain contains the brand name"}$$,
   $$HTTPS only means encryption, not legitimacy, and anyone can register a domain containing a brand name.$$),
  ('owasp-a03-inyeccion-phishing-manipulacion-urls', 5,
   $$True or false: pаypal.com written with a Cyrillic 'а' is the same domain as paypal.com.$$,
   $${"A": "True", "B": "False"}$$,
   $$False. It is a homograph attack: the letter looks the same but is a different Unicode character, and therefore a different domain.$$),

  -- OWASP A07
  ('owasp-a07-fallos-autenticacion-objetivo-phishing', 1,
   $$According to the Verizon DBIR 2023 report cited in the module, what percentage of data breaches involve stolen credentials?$$,
   $${"A": "12%", "B": "35%", "C": "83%", "D": "99%"}$$,
   $$83% of breaches involve stolen credentials, and phishing is the main way to steal them.$$),
  ('owasp-a07-fallos-autenticacion-objetivo-phishing', 2,
   $$Why does a password manager help protect you from phishing?$$,
   $${"A": "Because it changes your passwords every day", "B": "Because it does not autofill your credentials on a fake domain", "C": "Because it blocks all suspicious emails", "D": "Because it hides your IP address"}$$,
   $$The manager ties each password to an exact domain; on a cloned site it will not offer it, and that is a warning sign.$$),
  ('owasp-a07-fallos-autenticacion-objetivo-phishing', 3,
   $$Why are tools like Evilginx especially dangerous?$$,
   $${"A": "Because they only work on old computers", "B": "Because they act as a transparent proxy and can capture even MFA tokens", "C": "Because they wipe the victim's hard drive", "D": "Because they send mass spam"}$$,
   $$By sitting between the victim and the real site, they capture credentials and session tokens in real time.$$),
  ('owasp-a07-fallos-autenticacion-objetivo-phishing', 4,
   $$Which are NIST SP 800-63B recommendations according to the module? (Select all that apply)$$,
   $${"A": "Check passwords against blocklists", "B": "Do not expire passwords periodically, only when compromise is detected", "C": "Force a password change every 30 days", "D": "Favor length over complexity", "E": "Limit passwords to a maximum of 12 characters"}$$,
   $$NIST discourages periodic expiration and short limits; it prioritizes length and blocklists.$$),
  ('owasp-a07-fallos-autenticacion-objetivo-phishing', 5,
   $$What is the correct order of the credential theft phases described in the module?$$,
   $${"A": "Distribution → Cloning → Reconnaissance → Capture", "B": "Reconnaissance → Cloning → Distribution → Capture", "C": "Cloning → Reconnaissance → Capture → Distribution", "D": "Capture → Distribution → Cloning → Reconnaissance"}$$,
   $$First the target is studied, then the page is cloned, the link is distributed and finally the credentials are captured.$$),

  -- NIST SP 800-30
  ('nist-sp-800-30-evaluacion-riesgos-phishing', 1,
   $$What is the first step of a risk assessment according to NIST SP 800-30?$$,
   $${"A": "Communicate the results", "B": "Prepare for the assessment", "C": "Maintain the assessment", "D": "Conduct the assessment"}$$,
   $$The steps are: Prepare, Conduct, Communicate and Maintain.$$),
  ('nist-sp-800-30-evaluacion-riesgos-phishing', 2,
   $$According to the module's tables, a C-suite executive without MFA has an attack likelihood that is:$$,
   $${"A": "Low", "B": "Moderate", "C": "High", "D": "Very high"}$$,
   $$Executives are whaling targets, and without MFA an attack is almost certain: very high likelihood.$$),
  ('nist-sp-800-30-evaluacion-riesgos-phishing', 3,
   $$In the module's risk matrix, what is the result of HIGH likelihood with HIGH impact?$$,
   $${"A": "Green (low risk)", "B": "Yellow (moderate risk)", "C": "Orange (considerable risk)", "D": "Red (high risk)"}$$,
   $$High likelihood × high impact falls in the red zone of the matrix.$$),
  ('nist-sp-800-30-evaluacion-riesgos-phishing', 4,
   $$Which of these are VULNERABILITIES (not threats) in the context of phishing? (Select all that apply)$$,
   $${"A": "No MFA", "B": "Weak passwords", "C": "Spear phishing", "D": "Not knowing the warning signs", "E": "Smishing"}$$,
   $$Spear phishing and smishing are threats; vulnerabilities are the weaknesses those threats exploit.$$),
  ('nist-sp-800-30-evaluacion-riesgos-phishing', 5,
   $$What is residual risk?$$,
   $${"A": "The risk before applying any control", "B": "The risk that remains after applying the controls", "C": "The risk of a system that was already attacked", "D": "The risk assumed by the internet provider"}$$,
   $$No control eliminates risk completely; what remains after applying them is the residual risk.$$),

  -- Ciberhigiene OWASP y NIST
  ('buenas-practicas-owasp-nist-ciberhigiene', 1,
   $$What order of preference for MFA methods does the module recommend?$$,
   $${"A": "SMS > TOTP apps > Physical keys", "B": "Physical keys (FIDO2) > TOTP apps > SMS", "C": "TOTP apps > SMS > Physical keys", "D": "All methods are equally secure"}$$,
   $$FIDO2/WebAuthn keys are phishing-resistant; SMS is the weakest method.$$),
  ('buenas-practicas-owasp-nist-ciberhigiene', 2,
   $$What does the 3-2-1 backup rule mean?$$,
   $${"A": "3 passwords, 2 emails, 1 phone", "B": "3 copies, on 2 different media, 1 offsite or in the cloud", "C": "Back up 3 times a day, 2 days a week, 1 month a year", "D": "3 disks, 2 computers, 1 server"}$$,
   $$Three copies, two different media and one offsite protect your data even against ransomware.$$),
  ('buenas-practicas-owasp-nist-ciberhigiene', 3,
   $$According to the module's golden rule, what should you do when a message makes you feel urgency?$$,
   $${"A": "Act immediately so you don't lose access", "B": "Wait 5 minutes before acting", "C": "Forward it to your contacts", "D": "Reply asking for more time"}$$,
   $$Urgency is the attacker's tool. A real problem does not disappear if you wait a few minutes.$$),
  ('buenas-practicas-owasp-nist-ciberhigiene', 4,
   $$Which are good practices for permissions and account separation? (Select all that apply)$$,
   $${"A": "Revoke OAuth access for apps you no longer use", "B": "Use email aliases to sign up on third-party sites", "C": "Use the same email for work and personal life", "D": "Apply the principle of least privilege", "E": "Give apps every permission so they work better"}$$,
   $$Separating accounts and limiting permissions reduces the damage if an account or app is compromised.$$),
  ('buenas-practicas-owasp-nist-ciberhigiene', 5,
   $$If you are in Colombia, where can you report a cybersecurity incident according to the module?$$,
   $${"A": "incidencias@incibe-cert.es", "B": "incidentes@colcert.gov.co", "C": "cgsi@sspc.gob.mx", "D": "soporte@google.com"}$$,
   $$ColCERT is Colombia's incident response team. INCIBE is Spain's and CGSI is Mexico's.$$)

) AS v(slug, idx, q, o, e)
WHERE qq.quiz_id = z.id AND z.content_id = c.id AND c.slug = v.slug AND qq.order_index = v.idx;
