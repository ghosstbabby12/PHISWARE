package com.phishware.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private final String googleSafeBrowsingBaseUrl;
    private final String virusTotalBaseUrl;
    private final String virusTotalApiKey;
    private final String rdapBaseUrl;
    private final String hibpBaseUrl;

    public WebClientConfig(
            @Value("${app.google-safe-browsing.base-url}") String googleSafeBrowsingBaseUrl,
            @Value("${app.virus-total.base-url}") String virusTotalBaseUrl,
            @Value("${app.virus-total.api-key}") String virusTotalApiKey,
            @Value("${app.rdap.base-url:https://rdap.org}") String rdapBaseUrl,
            @Value("${app.hibp.base-url:https://api.haveibeenpwned.com}") String hibpBaseUrl) {
        this.googleSafeBrowsingBaseUrl = googleSafeBrowsingBaseUrl;
        this.virusTotalBaseUrl         = virusTotalBaseUrl;
        this.virusTotalApiKey          = virusTotalApiKey;
        this.rdapBaseUrl               = rdapBaseUrl;
        this.hibpBaseUrl               = hibpBaseUrl;
    }

    @NonNull
    private HttpClient buildHttpClient() {
        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(15))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS)));
    }

    @Bean("googleSafeBrowsingClient")
    public WebClient googleSafeBrowsingClient() {
        return WebClient.builder()
            .baseUrl(googleSafeBrowsingBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
            .build();
    }

    @Bean("virusTotalClient")
    public WebClient virusTotalClient() {
        return WebClient.builder()
            .baseUrl(virusTotalBaseUrl)
            .defaultHeader("x-apikey", virusTotalApiKey)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
            .build();
    }

    // RDAP: rdap.org redirige a la base de datos del registro correspondiente a cada TLD
    @Bean("rdapClient")
    public WebClient rdapClient() {
        return WebClient.builder()
            .baseUrl(rdapBaseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, "application/rdap+json, application/json")
            .clientConnector(new ReactorClientHttpConnector(buildHttpClient().followRedirect(true)))
            .build();
    }

    // HIBP k-anonymity: solo recibe prefijos SHA-1, nunca contraseñas completas
    @Bean("hibpClient")
    public WebClient hibpClient() {
        return WebClient.builder()
            .baseUrl(hibpBaseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, "PhishWare-PasswordCheck")
            .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
            .build();
    }
}
