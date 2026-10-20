package com.phishware.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Descarga páginas web de forma controlada para el análisis de contenido.
 *
 * OWASP A10 (SSRF): como el servidor visita URLs enviadas por usuarios, cada salto
 * de redirección se valida resolviendo el DNS y rechazando IPs privadas, loopback,
 * link-local, CGNAT y metadatos de nube. Además se limitan puertos, tamaño y tiempo.
 */
@Component
@Slf4j
public class SafePageFetcher {

    // User-Agent de navegador real: muchos kits de phishing muestran una página inocente a los bots
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        + "(KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";

    private static final Set<Integer> ALLOWED_PORTS = Set.of(-1, 80, 443, 8080, 8443);

    private final HttpClient client;
    private final Duration timeout;
    private final int maxBytes;
    private final int maxRedirects;

    public SafePageFetcher(
            @Value("${app.content-analysis.timeout-ms:8000}") long timeoutMs,
            @Value("${app.content-analysis.max-bytes:1500000}") int maxBytes,
            @Value("${app.content-analysis.max-redirects:5}") int maxRedirects) {
        this.timeout      = Duration.ofMillis(timeoutMs);
        this.maxBytes     = maxBytes;
        this.maxRedirects = maxRedirects;
        this.client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)   // las redirecciones se siguen a mano para validarlas
            .connectTimeout(this.timeout)
            .build();
    }

    public FetchResult fetch(String url) {
        List<String> chain = new ArrayList<>();
        URI current;
        try {
            current = URI.create(url);
        } catch (IllegalArgumentException e) {
            return FetchResult.failed(url, chain, FetchError.INVALID_URL, e.getMessage());
        }

        for (int hop = 0; hop <= maxRedirects; hop++) {
            chain.add(current.toString());
            try {
                assertFetchable(current);

                HttpRequest request = HttpRequest.newBuilder(current)
                    .timeout(timeout)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .GET()
                    .build();

                HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                int status = response.statusCode();

                if (status >= 300 && status < 400) {
                    String location = response.headers().firstValue("Location").orElse(null);
                    response.body().close();
                    if (location == null) {
                        return FetchResult.failed(url, chain, FetchError.UNREACHABLE, "Redirección sin destino");
                    }
                    current = current.resolve(location.trim());
                    continue;
                }

                String contentType = response.headers().firstValue("Content-Type").orElse("");
                String disposition = response.headers().firstValue("Content-Disposition").orElse("");
                byte[] body;
                boolean truncated;
                try (InputStream in = response.body()) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int total = 0, n;
                    while ((n = in.read(buf)) != -1 && total < maxBytes) {
                        int take = Math.min(n, maxBytes - total);
                        out.write(buf, 0, take);
                        total += take;
                    }
                    truncated = total >= maxBytes;
                    body = out.toByteArray();
                }

                return new FetchResult(url, current.toString(), List.copyOf(chain), status,
                    contentType, disposition, body, truncated, null, null);

            } catch (SecurityException e) {
                log.warn("[OWASP A10] Fetch bloqueado para {}: {}", current, e.getMessage());
                return FetchResult.failed(url, chain, FetchError.BLOCKED, e.getMessage());
            } catch (SSLException e) {
                return FetchResult.failed(url, chain, FetchError.TLS_ERROR, e.getMessage());
            } catch (UnknownHostException e) {
                return FetchResult.failed(url, chain, FetchError.DNS_ERROR, "El dominio no existe o no resuelve");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return FetchResult.failed(url, chain, FetchError.UNREACHABLE, "Interrumpido");
            } catch (IOException | IllegalArgumentException e) {
                return FetchResult.failed(url, chain, FetchError.UNREACHABLE, e.getMessage());
            }
        }
        return FetchResult.failed(url, chain, FetchError.TOO_MANY_REDIRECTS,
            "Más de " + maxRedirects + " redirecciones");
    }

    /** Rechaza esquemas, puertos y destinos de red no públicos. */
    private void assertFetchable(URI uri) throws UnknownHostException {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new SecurityException("Esquema no permitido: " + scheme);
        }
        if (uri.getHost() == null) {
            throw new SecurityException("URL sin host");
        }
        if (!ALLOWED_PORTS.contains(uri.getPort())) {
            throw new SecurityException("Puerto no permitido: " + uri.getPort());
        }
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            if (isNonPublic(address)) {
                throw new SecurityException("El destino resuelve a una IP interna: " + address.getHostAddress());
            }
        }
    }

    static boolean isNonPublic(InetAddress a) {
        if (a.isLoopbackAddress() || a.isAnyLocalAddress() || a.isLinkLocalAddress()
                || a.isSiteLocalAddress() || a.isMulticastAddress()) {
            return true;
        }
        byte[] b = a.getAddress();
        if (a instanceof Inet4Address) {
            int b0 = b[0] & 0xFF, b1 = b[1] & 0xFF;
            return b0 == 0                                  // 0.0.0.0/8
                || (b0 == 100 && (b1 & 0xC0) == 64)         // 100.64.0.0/10 CGNAT
                || (b0 == 192 && b1 == 0 && (b[2] & 0xFF) == 0) // 192.0.0.0/24
                || b0 >= 240;                               // reservado
        }
        if (a instanceof Inet6Address) {
            return (b[0] & 0xFE) == 0xFC;                   // fc00::/7 ULA
        }
        return false;
    }

    // ── Tipos de resultado ────────────────────────────────────────────────────

    public enum FetchError { INVALID_URL, BLOCKED, DNS_ERROR, TLS_ERROR, UNREACHABLE, TOO_MANY_REDIRECTS }

    public record FetchResult(
        String originalUrl,
        String finalUrl,
        List<String> redirectChain,
        int statusCode,
        String contentType,
        String contentDisposition,
        byte[] body,
        boolean truncated,
        FetchError error,
        String errorMessage
    ) {
        static FetchResult failed(String url, List<String> chain, FetchError error, String message) {
            String last = chain.isEmpty() ? url : chain.get(chain.size() - 1);
            return new FetchResult(url, last, List.copyOf(chain), 0, "", "", new byte[0], false, error, message);
        }

        public boolean isSuccess() {
            return error == null;
        }

        public boolean isHtml() {
            String ct = contentType == null ? "" : contentType.toLowerCase();
            return ct.isEmpty() || ct.contains("text/html") || ct.contains("application/xhtml");
        }
    }
}
