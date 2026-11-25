package com.phishware.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Idioma del contenido educativo según la cabecera Accept-Language de la petición actual.
 *
 * Se lee la cabecera directamente (y no LocaleContextHolder) a propósito: sin cabecera,
 * Spring usaría el locale del servidor, que en el contenedor suele ser inglés. Aquí el
 * valor por defecto es SIEMPRE español, así que clientes que no envían la cabecera
 * (app Android, scripts) siguen recibiendo el contenido original.
 */
public final class ContentLanguage {

    private ContentLanguage() {}

    /** true solo si el idioma preferido de la petición es inglés. */
    public static boolean isEnglish() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) return false;
        HttpServletRequest request = servletAttrs.getRequest();
        String header = request.getHeader("Accept-Language");
        return header != null && header.trim().toLowerCase().startsWith("en");
    }

    /** Devuelve la traducción si se pidió inglés y existe; si no, el original en español. */
    public static String pick(String es, String en) {
        return isEnglish() && en != null && !en.isBlank() ? en : es;
    }

    /** Variante genérica (listas, JSON...). */
    public static <T> T pick(T es, T en) {
        return isEnglish() && en != null ? en : es;
    }
}
