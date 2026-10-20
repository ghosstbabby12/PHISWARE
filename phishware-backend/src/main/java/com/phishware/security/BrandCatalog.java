package com.phishware.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Catálogo de marcas frecuentemente suplantadas y sus dominios oficiales.
 * NIST ID.RA-2: inteligencia de amenazas — lo usan tanto la heurística de URL
 * (typosquatting) como el análisis de contenido (suplantación visual).
 */
public final class BrandCatalog {

    public record Brand(String name, List<String> officialDomains) {}

    public static final List<Brand> BRANDS = List.of(
        // Globales
        new Brand("paypal",       List.of("paypal.com", "paypal.me")),
        new Brand("amazon",       List.of("amazon.com", "amazon.es", "amazon.com.mx", "amazon.com.br", "amazonaws.com")),
        new Brand("google",       List.of("google.com", "google.com.co", "gmail.com", "youtube.com", "googleapis.com", "gstatic.com")),
        new Brand("facebook",     List.of("facebook.com", "fb.com", "meta.com", "fbcdn.net")),
        new Brand("instagram",    List.of("instagram.com", "cdninstagram.com")),
        new Brand("whatsapp",     List.of("whatsapp.com", "whatsapp.net", "wa.me")),
        new Brand("microsoft",    List.of("microsoft.com", "live.com", "outlook.com", "office.com", "microsoftonline.com", "office365.com")),
        new Brand("outlook",      List.of("outlook.com", "live.com", "microsoft.com", "office.com")),
        new Brand("office365",    List.of("office.com", "office365.com", "microsoft.com", "microsoftonline.com")),
        new Brand("apple",        List.of("apple.com", "icloud.com")),
        new Brand("icloud",       List.of("icloud.com", "apple.com")),
        new Brand("netflix",      List.of("netflix.com", "nflxext.com")),
        new Brand("twitter",      List.of("twitter.com", "x.com", "twimg.com")),
        new Brand("linkedin",     List.of("linkedin.com", "licdn.com")),
        new Brand("dropbox",      List.of("dropbox.com")),
        new Brand("binance",      List.of("binance.com")),
        new Brand("coinbase",     List.of("coinbase.com")),
        new Brand("metamask",     List.of("metamask.io")),
        new Brand("dhl",          List.of("dhl.com", "dhl.de")),
        new Brand("fedex",        List.of("fedex.com")),
        new Brand("bankofamerica",List.of("bankofamerica.com")),
        new Brand("wellsfargo",   List.of("wellsfargo.com")),
        // Latinoamérica
        new Brand("bancolombia",  List.of("bancolombia.com", "grupobancolombia.com", "bancolombia.com.co")),
        new Brand("davivienda",   List.of("davivienda.com")),
        new Brand("daviplata",    List.of("daviplata.com", "davivienda.com")),
        new Brand("nequi",        List.of("nequi.com.co", "nequi.co")),
        new Brand("bbva",         List.of("bbva.com", "bbva.com.co", "bbva.mx", "bbva.es")),
        new Brand("santander",    List.of("santander.com", "santander.com.mx", "santander.com.br", "bancosantander.es")),
        new Brand("mercadopago",  List.of("mercadopago.com", "mercadopago.com.co", "mercadopago.com.mx", "mercadopago.com.ar")),
        new Brand("mercadolibre", List.of("mercadolibre.com", "mercadolibre.com.co", "mercadolibre.com.mx", "mercadolibre.com.ar")),
        new Brand("dian",         List.of("dian.gov.co")),
        new Brand("servientrega", List.of("servientrega.com"))
    );

    private static final Map<String, Pattern> MENTION_PATTERNS = BRANDS.stream()
        .collect(Collectors.toMap(Brand::name, b -> Pattern.compile("\\b" + b.name() + "\\b")));

    private BrandCatalog() {}

    /** true si el host pertenece a alguno de los dominios oficiales de la marca. */
    public static boolean isOfficialFor(Brand brand, String host) {
        return brand.officialDomains().stream().anyMatch(d -> DomainUtils.isSameOrSubdomain(host, d));
    }

    /** true si el host pertenece a cualquier marca del catálogo. */
    public static boolean isAnyOfficialDomain(String host) {
        return BRANDS.stream().anyMatch(b -> isOfficialFor(b, host));
    }

    /** Busca una marca cuyo nombre aparezca como palabra completa en el texto. */
    public static Optional<Brand> findMentionedBrand(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String t = text.toLowerCase();
        return BRANDS.stream()
            .filter(b -> MENTION_PATTERNS.get(b.name()).matcher(t).find())
            .findFirst();
    }
}
