package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Test

class HudFormatterTest {
    @Test
    fun speedIsRoundedToSingleDecimal() {
        assertEquals("19.9", HudFormatter.speed(19.94f))
        assertEquals("20.0", HudFormatter.speed(19.96f))
    }

    @Test
    fun speedUnitIsKmPerHour() {
        assertEquals("KM/H", HudFormatter.unit())
    }
}
