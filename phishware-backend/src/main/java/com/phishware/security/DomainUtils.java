package com.phishware.security;

import java.util.Locale;
import java.util.Set;

/**
 * Utilidades para trabajar con nombres de dominio.
 *
 * El "dominio registrable" es la parte que una persona compra (ej. paypal.com,
 * bancolombia.com.co). Es lo que importa para saber si dos URLs pertenecen al mismo dueño.
 * Se usa una lista reducida de sufijos públicos de segundo nivel; para producción
 * se podría reemplazar por la Public Suffix List completa.
 */
public final class DomainUtils {

    private static final Set<String> SECOND_LEVEL_SUFFIXES = Set.of(
        "com.co", "net.co", "org.co", "gov.co", "edu.co", "nom.co",
        "com.mx", "org.mx", "gob.mx", "edu.mx",
        "com.ar", "gob.ar", "com.br", "gov.br", "com.pe", "gob.pe",
        "com.ec", "gob.ec", "com.ve", "cl.cl", "com.uy", "com.bo", "com.py",
        "co.uk", "org.uk", "gov.uk", "ac.uk", "com.au", "co.jp", "co.in", "com.es"
    );

    private DomainUtils() {}

    /** Devuelve el dominio registrable (ej. "login.secure.paypal.com" → "paypal.com"). */
    public static String registrableDomain(String host) {
        if (host == null || host.isBlank()) return "";
        String h = host.toLowerCase(Locale.ROOT).replaceAll("\\.$", "");
        if (isIpAddress(h)) return h;

        String[] labels = h.split("\\.");
        if (labels.length <= 2) return h;

        String lastTwo = labels[labels.length - 2] + "." + labels[labels.length - 1];
        if (SECOND_LEVEL_SUFFIXES.contains(lastTwo) && labels.length >= 3) {
            return labels[labels.length - 3] + "." + lastTwo;
        }
        return lastTwo;
    }

    /** Nombre principal del dominio sin TLD (ej. "paypal-secure.com" → "paypal-secure"). */
    public static String secondLevelLabel(String host) {
        String reg = registrableDomain(host);
        int dot = reg.indexOf('.');
        return dot > 0 ? reg.substring(0, dot) : reg;
    }

    /** true si host es exactamente el dominio o un subdominio de él (no un simple endsWith). */
    public static boolean isSameOrSubdomain(String host, String domain) {
        if (host == null || domain == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        String d = domain.toLowerCase(Locale.ROOT);
        return h.equals(d) || h.endsWith("." + d);
    }

    public static boolean isIpAddress(String host) {
        if (host == null) return false;
        return host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$") || host.contains(":");
    }

    /** Entropía de Shannon: valores altos indican nombres aleatorios (generados por malware/DGA). */
    public static double shannonEntropy(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        int[] freq = new int[256];
        for (char c : s.toCharArray()) freq[c & 0xFF]++;
        double entropy = 0.0;
        for (int f : freq) {
            if (f == 0) continue;
            double p = (double) f / s.length();
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    /** Distancia de Levenshtein: número de ediciones para convertir a en b. */
    public static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[b.length()];
    }

    /** Normaliza caracteres que se usan para imitar letras (0→o, 1→l, rn→m, vv→w...). */
    public static String normalizeHomoglyphs(String s) {
        return s.toLowerCase(Locale.ROOT)
            .replace("rn", "m")
            .replace("vv", "w")
            .replace('0', 'o')
            .replace('1', 'l')
            .replace('3', 'e')
            .replace('4', 'a')
            .replace('5', 's')
            .replace('7', 't')
            .replace('@', 'a')
            .replace('$', 's');
    }
}
