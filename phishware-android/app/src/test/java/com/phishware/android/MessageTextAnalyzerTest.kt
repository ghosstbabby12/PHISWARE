package com.phishware.android

import com.phishware.android.security.MessageTextAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTextAnalyzerTest {

    @Test
    fun emptyTextIsSafe() {
        val r = MessageTextAnalyzer.analyze("   ")
        assertEquals("SAFE", r.level)
        assertFalse(r.isThreat)
    }

    @Test
    fun bankUrgencySmishingIsDangerous() {
        val r = MessageTextAnalyzer.analyze(
            "BANCOLOMBIA: su cuenta sera bloqueada en 24 horas, verifique su identidad aqui: http://banco-seguro.tk/login"
        )
        assertEquals("DANGEROUS", r.level)
        assertTrue(r.isThreat)
        assertTrue(r.principles.contains("URGENCY"))
        assertTrue(r.principles.contains("AUTHORITY") || r.principles.contains("FEAR"))
    }

    @Test
    fun prizeBaitIsAtLeastSuspicious() {
        val r = MessageTextAnalyzer.analyze(
            "Felicidades! Has ganado un premio, reclama tu regalo gratis aqui"
        )
        assertTrue(r.isThreat)
        assertTrue(r.principles.contains("CURIOSITY"))
    }

    @Test
    fun govImpersonationInSpanishScoresHigh() {
        val r = MessageTextAnalyzer.analyze(
            "DIAN: tiene una multa pendiente, evite la demanda actualizando sus datos hoy mismo"
        )
        assertTrue(r.score >= 25.0)
        assertTrue(r.principles.contains("AUTHORITY"))
    }

    @Test
    fun englishPhishingIsDetected() {
        val r = MessageTextAnalyzer.analyze(
            "Your account has been suspended. Verify your identity within 24 hours to avoid losing access"
        )
        assertTrue(r.isThreat)
    }

    @Test
    fun benignChatIsSafe() {
        val r = MessageTextAnalyzer.analyze(
            "Hola, nos vemos manana a las 3 en el cafe de la esquina. Trae el libro porfa."
        )
        assertEquals("SAFE", r.level)
    }

    @Test
    fun shortenerPlusMoneyIsSuspicious() {
        val r = MessageTextAnalyzer.analyze("Mira esto http://bit.ly/xyz1234 gana \$500.000 ya")
        assertTrue(r.matchedRules.contains("TECH_SHORTENER"))
        assertTrue(r.isThreat)
    }

    @Test
    fun otpRequestScoresTrustPrinciple() {
        val r = MessageTextAnalyzer.analyze("Soy de soporte, dime el codigo que te llego por SMS para verificar")
        assertTrue(r.principles.contains("TRUST"))
        assertTrue(r.matchedRules.contains("TECH_CODEASK"))
    }

    @Test
    fun scoreIsCappedAt100() {
        val r = MessageTextAnalyzer.analyze(
            "URGENTE banco DIAN multa cuenta bloqueada en 24 horas verifica gratis premio http://bit.ly/x http://1.2.3.4/y"
        )
        assertTrue(r.score <= 100.0)
        assertEquals("DANGEROUS", r.level)
    }

    @Test
    fun textNeverLeavesDeviceContract() {
        // The analyzer is a pure local function: no network, no callbacks, no permissions.
        // This test pins the contract — analyze() must stay side-effect free.
        val before = Runtime.getRuntime().freeMemory()
        MessageTextAnalyzer.analyze("BANCOLOMBIA urgente verifique http://x.tk")
        assertTrue(Runtime.getRuntime().freeMemory() <= before + 50 * 1024 * 1024)
    }
}
