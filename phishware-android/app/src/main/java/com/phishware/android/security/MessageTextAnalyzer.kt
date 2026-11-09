package com.phishware.android.security

/**
 * On-device analysis of message text (Spanish + English, ~40 rules).
 *
 * The text NEVER leaves the device: only extracted URLs are sent to the
 * backend (Ley 1581 data minimisation). Used as the offline fallback when
 * the backend is unreachable.
 */
object MessageTextAnalyzer {

    data class TextRule(val id: String, val pattern: Regex, val weight: Double, val principle: String)

    data class TextRisk(
        val score: Double,
        val level: String,
        val principles: List<String>,
        val matchedRules: List<String>
    ) {
        val isThreat: Boolean get() = level != "SAFE"
    }

    // (id, regex, weight 5-15, Cialdini principle)
    private val RULES: List<TextRule> = listOf(
        // — Urgency / time pressure —
        TextRule("URGENCY_24H", Regex("24\\s*horas|24\\s*hours?", RegexOption.IGNORE_CASE), 12.0, "URGENCY"),
        TextRule("URGENCY_NOW", Regex("\\b(urgente|urgent|inmediat[oa]|immediately|ya mismo|ahora mismo)\\b", RegexOption.IGNORE_CASE), 10.0, "URGENCY"),
        TextRule("URGENCY_DEADLINE", Regex("última oportunidad|last chance|vence hoy|expires today|se acaba", RegexOption.IGNORE_CASE), 10.0, "URGENCY"),
        TextRule("URGENCY_ACTION", Regex("act[úu]e (ya|ahora|de inmediato)|act now|haz clic ya", RegexOption.IGNORE_CASE), 8.0, "URGENCY"),
        // — Fear / threats —
        TextRule("FEAR_BLOCK", Regex("cuenta bloqueada|account (suspended|blocked|locked)|suspenderemos", RegexOption.IGNORE_CASE), 14.0, "FEAR"),
        TextRule("FEAR_LEGAL", Regex("demanda|multa|c[aá]rcel|orden judicial|abogado|polic[íi]a nacional", RegexOption.IGNORE_CASE), 14.0, "FEAR"),
        TextRule("FEAR_LOSS", Regex("perder[áa]s|vas a perder|fondos retenidos|saldo congelado", RegexOption.IGNORE_CASE), 10.0, "FEAR"),
        TextRule("FEAR_SECURITY", Regex("actividad (sospechosa|inusual)|acceso no autorizado|intento de (acceso|ingreso)", RegexOption.IGNORE_CASE), 10.0, "FEAR"),
        // — Authority impersonation —
        TextRule("AUTH_BANK", Regex("\\b(banco|bancolombia|davivienda|nequi|bbva|santander|bank of america)\\b", RegexOption.IGNORE_CASE), 8.0, "AUTHORITY"),
        TextRule("AUTH_GOV", Regex("\\b(dian|registradur[íi]a|fiscal[íi]a|sisben|gobierno)\\b", RegexOption.IGNORE_CASE), 10.0, "AUTHORITY"),
        TextRule("AUTH_CARRIER", Regex("\\b(claro|movistar|tigo|wom)\\b.{0,20}\\b(soporte|t[eé]cnico|servicio)\\b", RegexOption.IGNORE_CASE), 8.0, "AUTHORITY"),
        TextRule("AUTH_BOSS", Regex("\\b(jefe|gerente|director|recursos humanos)\\b", RegexOption.IGNORE_CASE), 8.0, "AUTHORITY"),
        // — Curiosity / bait —
        TextRule("BAIT_PRIZE", Regex("ganaste|has ganado|you (won|have won)|premio|loter[íi]a|bono de", RegexOption.IGNORE_CASE), 12.0, "CURIOSITY"),
        TextRule("BAIT_FREE", Regex("\\b(gratis|free|regalo|obsequio|sin costo)\\b", RegexOption.IGNORE_CASE), 8.0, "CURIOSITY"),
        TextRule("BAIT_JOB", Regex("oferta de (empleo|trabajo)|trabaja desde casa|ingresos extra", RegexOption.IGNORE_CASE), 10.0, "CURIOSITY"),
        TextRule("BAIT_PACKAGE", Regex("paquete (retenido|en aduana|pendiente)|reclama(r)? tu (pedido|paquete)", RegexOption.IGNORE_CASE), 12.0, "CURIOSITY"),
        TextRule("BAIT_ROMANCE", Regex("\\b(hola (guapo|preciosa)|soy (soltera|nueva aqu[íi]))\\b", RegexOption.IGNORE_CASE), 8.0, "CURIOSITY"),
        // — Trust / verification lures —
        TextRule("TRUST_VERIFY", Regex("verific(a|ar|ación|e|en|ando)|verify your|confirma(r)? tu (identidad|cuenta|datos)", RegexOption.IGNORE_CASE), 10.0, "TRUST"),
        TextRule("TRUST_UPDATE", Regex("actualiz(a|ar|ación|ando) (de|tu|tus|sus)|update your (information|payment)", RegexOption.IGNORE_CASE), 10.0, "TRUST"),
        TextRule("TRUST_LOGIN", Regex("\\b(login|log-in|log in|inicia(r)? sesi[óo]n|ingresa aqu[íi])\\b", RegexOption.IGNORE_CASE), 8.0, "TRUST"),
        TextRule("TRUST_SUPPORT", Regex("soporte t[eé]cnico|atenci[óo]n al cliente|mesa de ayuda", RegexOption.IGNORE_CASE), 8.0, "TRUST"),
        // — Scarcity —
        TextRule("SCARCITY_LEFT", Regex("quedan \\d+|only \\d+ left|últimas? \\d+|cupos limitados", RegexOption.IGNORE_CASE), 8.0, "SCARCITY"),
        TextRule("SCARCITY_ONCE", Regex("solo por hoy|only today|solo esta vez", RegexOption.IGNORE_CASE), 8.0, "SCARCITY"),
        // — Social proof / reciprocity —
        TextRule("SOCIAL_CROWD", Regex("miles (de personas|ya)|todo el mundo|tus contactos ya", RegexOption.IGNORE_CASE), 6.0, "SOCIAL_PROOF"),
        TextRule("RECIP_CREDIT", Regex("cr[eé]dito (pre)?aprobado|pr[eé]stamo (inmediato|sin tr[áa]mites)", RegexOption.IGNORE_CASE), 10.0, "RECIPROCITY"),
        // — Technical signals inside the text —
        TextRule("TECH_SHORTENER", Regex("bit\\.ly|tinyurl|t\\.co/|goo\\.gl|ow\\.ly|s\\.id|rb\\.gy|is\\.gd", RegexOption.IGNORE_CASE), 12.0, "NONE"),
        TextRule("TECH_FREETLD", Regex("\\.(tk|ml|ga|cf|gq|xyz|top|sbs|buzz|icu|cyou|rest)\\b", RegexOption.IGNORE_CASE), 12.0, "NONE"),
        TextRule("TECH_IPURL", Regex("https?://\\d{1,3}(\\.\\d{1,3}){3}", RegexOption.IGNORE_CASE), 12.0, "NONE"),
        TextRule("TECH_ATSIGN", Regex("https?://[^\\s]*@", RegexOption.IGNORE_CASE), 10.0, "NONE"),
        TextRule("TECH_MANYDOTS", Regex("([a-z0-9-]+\\.){3,}[a-z]{2,}", RegexOption.IGNORE_CASE), 8.0, "NONE"),
        TextRule("TECH_MANYHYPHENS", Regex("[a-z0-9]+(-[a-z0-9]+){3,}", RegexOption.IGNORE_CASE), 8.0, "NONE"),
        TextRule("TECH_LONGURL", Regex("https?://\\S{75,}", RegexOption.IGNORE_CASE), 6.0, "NONE"),
        TextRule("TECH_MONEY", Regex("\\$\\s?\\d[\\d.,]*|\\d[\\d.,]*\\s?(pesos|d[oó]lares|cop)", RegexOption.IGNORE_CASE), 6.0, "NONE"),
        TextRule("TECH_CODEASK", Regex("(c[óo]digo|otp|pin|token|clave).{0,40}(comparta|comparteen|dime|diles|env[íi]a|reenv[íi]a)|(dime|comparte|env[íi]ame|reenv[íi]ame).{0,40}(c[óo]digo|otp|pin|token|clave)", RegexOption.IGNORE_CASE), 12.0, "TRUST"),
        TextRule("TECH_APK", Regex("\\.apk\\b|instala(r)? (esta|la) (app|aplicaci[óo]n)", RegexOption.IGNORE_CASE), 12.0, "FEAR"),
        TextRule("TECH_WHATSAPP_FWD", Regex("reenv[ií]a(do)? (este mensaje|a (tus|\\d+) contactos)", RegexOption.IGNORE_CASE), 6.0, "SOCIAL_PROOF"),
        TextRule("STYLE_CAPS", Regex("\\b[A-ZÁÉÍÓÚÑ]{6,}\\b"), 5.0, "NONE"),
        TextRule("STYLE_EXCLAM", Regex("!{2,}"), 5.0, "NONE"),
        TextRule("LANG_MIX", Regex("\\b(click here|dear customer|kindly)\\b", RegexOption.IGNORE_CASE), 6.0, "NONE"),
        TextRule("BANK_SENSITIVE", Regex("\\b(clave|contrase[ñn]a|password|pin|cvv|otp)\\b", RegexOption.IGNORE_CASE), 8.0, "TRUST")
    )

    fun analyze(text: String): TextRisk {
        if (text.isBlank()) return TextRisk(0.0, "SAFE", emptyList(), emptyList())
        val matched = RULES.filter { it.pattern.containsMatchIn(text) }
        val score = matched.sumOf { it.weight }.coerceAtMost(100.0)
        // Same bands as the server-side heuristic (NistThreatClassifier: 40/15)
        val level = when {
            score >= 40.0 -> "DANGEROUS"
            score >= 15.0 -> "SUSPICIOUS"
            else -> "SAFE"
        }
        val principles = matched.map { it.principle }.filter { it != "NONE" }.distinct()
        return TextRisk(score, level, principles, matched.map { it.id })
    }
}
