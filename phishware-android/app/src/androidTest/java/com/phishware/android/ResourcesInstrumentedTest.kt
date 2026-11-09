package com.phishware.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that user-visible strings resolve on device (i18n smoke test).
 * Run with: ./gradlew connectedPlayDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ResourcesInstrumentedTest {

    private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun coreStringsAreNotEmpty() {
        listOf(
            R.string.nav_dashboard,
            R.string.guard_title,
            R.string.guard_theme_title,
            R.string.notif_threat_danger
        ).forEach { id ->
            assertFalse("empty string id=$id", ctx.getString(id).isBlank())
        }
    }

    @Test
    fun themeLabelsResolve() {
        assertTrue(ctx.getString(R.string.guard_theme_dark).isNotBlank())
        assertTrue(ctx.getString(R.string.guard_theme_system).isNotBlank())
    }
}
