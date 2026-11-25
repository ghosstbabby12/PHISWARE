package com.phishware.i18n;

import com.phishware.entity.EducationalContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContentLanguageTest {

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static void request(String acceptLanguage) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (acceptLanguage != null) req.addHeader("Accept-Language", acceptLanguage);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    @Test
    void sinCabeceraSiempreEspanol() {
        request(null);
        assertThat(ContentLanguage.pick("hola", "hello")).isEqualTo("hola");
    }

    @Test
    void ingesSoloSiEsElIdiomaPreferido() {
        request("en-US,en;q=0.9");
        assertThat(ContentLanguage.pick("hola", "hello")).isEqualTo("hello");
        request("es-ES,es;q=0.9,en;q=0.8");
        assertThat(ContentLanguage.pick("hola", "hello")).isEqualTo("hola");
    }

    @Test
    void sinTraduccionCaeAlEspanol() {
        request("en");
        assertThat(ContentLanguage.pick("hola", null)).isEqualTo("hola");
        assertThat(ContentLanguage.pick("hola", "  ")).isEqualTo("hola");
    }

    @Test
    void fueraDeUnaPeticionEsEspanol() {
        assertThat(ContentLanguage.isEnglish()).isFalse();
    }

    @Test
    void localizedDevuelveCopiaSinTocarLaEntidad() {
        EducationalContent original = EducationalContent.builder()
            .id(1L).slug("s").category("SMISHING").difficulty("BEGINNER")
            .title("Título").content("Contenido").tags(List.of("es"))
            .titleEn("Title").contentEn("Content").tagsEn(List.of("en"))
            .build();

        request("en");
        EducationalContent en = original.localized();
        assertThat(en).isNotSameAs(original);
        assertThat(en.getTitle()).isEqualTo("Title");
        assertThat(en.getContent()).isEqualTo("Content");
        assertThat(en.getTags()).containsExactly("en");
        assertThat(original.getTitle()).isEqualTo("Título");

        request("es");
        assertThat(original.localized()).isSameAs(original);
    }
}
