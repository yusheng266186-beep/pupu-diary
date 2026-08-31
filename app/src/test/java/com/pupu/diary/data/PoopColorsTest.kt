package com.pupu.diary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PoopColorsTest {

    @Test
    fun labelsAndColorsPaired() {
        assertEquals(6, PoopColors.labels.size)
        assertEquals(PoopColors.labels.size, PoopColors.colors.size)
        assertTrue(PoopColors.colors.toSet().size == PoopColors.colors.size)
    }

    @Test
    fun labelInRange() {
        assertEquals("深棕", PoopColors.label(0))
        assertEquals("棕色", PoopColors.label(1))
        assertEquals("其他", PoopColors.label(5))
    }

    @Test
    fun labelOutOfRangeFallsBack() {
        assertEquals("未记录颜色", PoopColors.label(-1))
        assertEquals("未记录颜色", PoopColors.label(99))
        assertEquals(androidx.compose.ui.graphics.Color(0xFFBE8E60), PoopColors.color(-1))
    }
}
