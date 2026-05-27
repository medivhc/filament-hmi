package com.example.filamenthmi

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HudOverlayInstrumentationTest {
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun launchAndKeepStableForScreenshotCapture() {
        // Placeholder E2E smoke test. Screenshot is captured by external script after launch.
        Thread.sleep(1_500)
    }
}
