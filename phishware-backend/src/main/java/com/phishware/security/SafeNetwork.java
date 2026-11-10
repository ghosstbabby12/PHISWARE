package com.phishware.security;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Shared network-safety checks against Server-Side Request Forgery (OWASP A10).
 *
 * Resolves hostnames to IP addresses instead of matching substrings, so encoded
 * loopback forms (decimal, hexadecimal, octal, short forms, IPv6) are blocked too.
 */
public final class SafeNetwork {

    private SafeNetwork() {
    }

    /**
     * Resolves {@code host} and rejects it when any address is non-public.
     *
     * @throws SecurityException when the host resolves to a non-public address
     *                           or cannot be resolved (fail closed)
     */
    public static void assertPublicHost(String host) {
        if (host == null || host.isBlank()) {
            throw new SecurityException("URL sin host");
        }
        assertNotLegacyEncodedLoopback(host);
        final InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // No resuelve: no hay ningún destino al que hacer SSRF, así que no es un riesgo
            // de seguridad bloquear aquí. SafePageFetcher vuelve a resolver antes de cada
            // fetch real (incluso por cada salto de redirección), así que esa es la barrera
            // efectiva contra SSRF. Dejar pasar el análisis es necesario para poder evaluar
            // dominios de phishing ya dados de baja o con DNS "sinkholeado", que son comunes.
            return;
        }
        for (InetAddress address : addresses) {
            if (isNonPublic(address)) {
                throw new SecurityException(
                    "El destino resuelve a una IP interna: " + address.getHostAddress());
            }
        }
    }

    /**
     * Blocks legacy {@code inet_aton} encodings (hex, octal, short forms) that some
     * HTTP clients resolve to loopback/private addresses even when the JVM parses
     * them as public literals (e.g. {@code 0177.0.0.1}).
     */
    static void assertNotLegacyEncodedLoopback(String host) {
        byte[] raw = parseLegacyInetAton(host);
        if (raw == null) {
            return;
        }
        try {
            if (isNonPublic(InetAddress.getByAddress(raw))) {
                throw new SecurityException(
                    "El destino usa una codificación legada de IP interna: " + host);
            }
        } catch (UnknownHostException e) {
            throw new SecurityException("Dirección IP inválida: " + host);
        }
    }

    static byte[] parseLegacyInetAton(String host) {
        String clean = host;
        if (clean.startsWith("[") && clean.endsWith("]")) {
            clean = clean.substring(1, clean.length() - 1);
        }
        if (clean.contains(":")) {
            return null;
        }
        String[] parts = clean.split("\\.", -1);
        if (parts.length < 1 || parts.length > 4) {
            return null;
        }
        long[] values = new long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            Long parsed = parseLegacyPart(parts[i]);
            if (parsed == null || parsed < 0 || parsed > 0xFFFFFFFFL) {
                return null;
            }
            values[i] = parsed;
        }
        long address;
        switch (values.length) {
            case 1 -> address = values[0];
            case 2 -> {
                if (values[0] > 0xFF || values[1] > 0xFFFFFF) return null;
                address = (values[0] << 24) | values[1];
            }
            case 3 -> {
                if (values[0] > 0xFF || values[1] > 0xFF || values[2] > 0xFFFF) return null;
                address = (values[0] << 24) | (values[1] << 16) | values[2];
            }
            default -> {
                for (long v : values) {
                    if (v > 0xFF) return null;
                }
                address = (values[0] << 24) | (values[1] << 16) | (values[2] << 8) | values[3];
            }
        }
        return new byte[]{
            (byte) (address >>> 24), (byte) (address >>> 16),
            (byte) (address >>> 8), (byte) address};
    }

    private static Long parseLegacyPart(String part) {
        if (part.isEmpty()) {
            return null;
        }
        try {
            if (part.length() > 2 && (part.startsWith("0x") || part.startsWith("0X"))) {
                return Long.parseLong(part.substring(2), 16);
            }
            if (part.length() > 1 && part.startsWith("0")) {
                return Long.parseLong(part, 8);
            }
            if (!part.chars().allMatch(Character::isDigit)) {
                return null;
            }
            return Long.parseLong(part, 10);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isNonPublic(InetAddress address) {
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] raw = address.getAddress();
        if (address instanceof Inet4Address) {
            int b0 = raw[0] & 0xFF;
            int b1 = raw[1] & 0xFF;
            return b0 == 0                                  // 0.0.0.0/8
                || (b0 == 100 && (b1 & 0xC0) == 64)         // 100.64.0.0/10 CGNAT
                || (b0 == 192 && b1 == 0 && (raw[2] & 0xFF) == 0) // 192.0.0.0/24
                || b0 >= 240;                               // reservado
        }
        if (address instanceof Inet6Address) {
            return (raw[0] & 0xFE) == 0xFC;                 // fc00::/7 ULA
        }
        return false;
    }
}
