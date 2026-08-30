package com.pupu.diary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BristolTest {

    @Test
    fun labelReturnsAllSevenTypeNames() {
        assertEquals("硬块状", Bristol.label(1))
        assertEquals("泥块状", Bristol.label(4))
        assertEquals("水样状", Bristol.label(7))
        assertEquals(7, Bristol.labels.size)
    }

    @Test
    fun hintGivesAdvicePerType() {
        assertTrue(Bristol.hint(1).contains("多喝水"))
        assertTrue(Bristol.hint(3).contains("继续保持"))
        assertTrue(Bristol.hint(5).contains("膳食纤维"))
        assertTrue(Bristol.hint(7).contains("肚子"))
    }

    @Test
    fun groupOfMapsDistributionGroups() {
        val expected = mapOf(1 to 0, 2 to 0, 3 to 1, 4 to 1, 5 to 2, 6 to 2, 7 to 3)
        expected.forEach { (type, group) -> assertEquals(group, Bristol.groupOf(type)) }
        assertEquals(4, Bristol.groupNames.size)
    }
}
