package com.phishware.android

import com.phishware.android.security.UrlExtractor
import org.junit.Assert.*
import org.junit.Test

class UrlExtractorTest {

    @Test
    fun `extrae URL de SMS con urgencia`() {
        val urls = UrlExtractor.extractUrls("NEQUI: su cuenta sera bloqueada en 24h, verifique en nequi-verificacion.tk/login")
        assertEquals(1, urls.size)
        assertTrue(urls[0].startsWith("https://"))
    }

    @Test
    fun `ignora localhost y red local`() {
        assertTrue(UrlExtractor.extractUrls("mira http://localhost:8080/api y http://192.168.1.1/x").isEmpty())
    }

    @Test
    fun `detecta truco userinfo con arroba`() {
        val urls = UrlExtractor.extractUrls("ingrese a https://bancolombia.com@malicioso.ru/login")
        assertEquals(1, urls.size)
    }

    @Test
    fun `texto sin URL retorna vacio`() {
        assertTrue(UrlExtractor.extractUrls("Hola, te escribo por la reunion").isEmpty())
    }

    @Test
    fun `limita a 5 urls por mensaje`() {
        val text = (1..10).joinToString(" ") { "https://evil$it.xyz/login" }
        assertTrue(UrlExtractor.extractUrls(text).size <= 5)
    }
}
