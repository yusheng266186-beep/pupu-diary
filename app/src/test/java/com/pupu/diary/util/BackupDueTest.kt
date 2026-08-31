package com.pupu.diary.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** v4.0.0 回归测试：自动备份到期判断（修复 lastAutoBackup 永远解析失败的问题） */
class BackupDueTest {

    @Test
    fun emptyLastIsDue() {
        assertTrue(Backup.isBackupDue("", LocalDate.of(2026, 8, 31)))
    }

    @Test
    fun basicIsoFormatParses() {
        // 当前写入格式 yyyyMMdd
        assertFalse(Backup.isBackupDue("20260830", LocalDate.of(2026, 8, 31)))
        assertTrue(Backup.isBackupDue("20260824", LocalDate.of(2026, 8, 31)))
    }

    @Test
    fun isoFormatAlsoParses() {
        // 兼容 ISO 写入值
        assertFalse(Backup.isBackupDue("2026-08-30", LocalDate.of(2026, 8, 31)))
        assertTrue(Backup.isBackupDue("2026-08-10", LocalDate.of(2026, 8, 31)))
    }

    @Test
    fun garbageIsDue() {
        assertTrue(Backup.isBackupDue("not-a-date", LocalDate.of(2026, 8, 31)))
    }
}
