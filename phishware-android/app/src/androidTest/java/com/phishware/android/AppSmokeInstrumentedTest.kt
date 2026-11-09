package com.phishware.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests that run on a device or emulator:
 * `./gradlew connectedPlayDebugAndroidTest` (or fullDebug).
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeInstrumentedTest {

    @Test
    fun appContext_hasExpectedPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.phishware.android", appContext.packageName)
    }

    @Test
    fun appNameStringResolves() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("PHISHWARE", appContext.getString(R.string.app_name))
    }
}
