package com.bf410.goaldaylocal.ui.reminder

import com.bf410.goaldaylocal.data.ScheduleEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderDigestTest {
    private val today = LocalDate.of(2026, 9, 26)

    private fun entry(title: String, day: Int, completed: Boolean = false, timeText: String = "") =
        ScheduleEntry(
            id = title,
            title = title,
            year = 2026,
            month = 9,
            day = day,
            timeText = timeText,
            completed = completed,
        )

    @Test
    fun overdue_comes_before_today_and_completed_excluded() {
        val digest = buildReminderDigest(
            listOf(
                entry("今天B", 26),
                entry("逾期A", 24),
                entry("做完的不算", 26, completed = true),
                entry("未来不算", 27),
                entry("今天A", 26, timeText = "09:00"),
            ),
            today,
        )

        assertEquals(listOf("逾期A"), digest.overdueTitles)
        assertEquals(listOf("今天A", "今天B"), digest.todayTitles)
        assertEquals(3, digest.totalUndone)
    }

    @Test
    fun empty_and_blank_titles_yield_zero() {
        assertEquals(0, buildReminderDigest(emptyList(), today).totalUndone)
        assertEquals(0, buildReminderDigest(listOf(entry("  ", 26)), today).totalUndone)
    }

    @Test
    fun morning_title_mentions_overdue_count() {
        val digest = buildReminderDigest(listOf(entry("逾期A", 24), entry("今天A", 26)), today)

        assertTrue(digest.notificationTitle().contains("逾期1件"))
        assertTrue(digest.notificationTitle().contains("今日还有1件"))
    }

    @Test
    fun content_lists_up_to_four_then_counts_rest() {
        val entries = (1..6).map { entry("事项$it", 26) }
        val digest = buildReminderDigest(entries, today)

        val content = digest.notificationContent()
        assertTrue(content.contains("事项1"))
        assertTrue(content.contains("事项4"))
        assertTrue(content.contains("等2件"))
    }

    @Test
    fun rollover_moves_only_overdue_undone_without_duplicates() {
        val rolled = rolloverOverdue(
            listOf(
                entry("逾期A", 24),
                entry("今天已有", 26),
                entry("今天已有", 24),
                entry("做完的不动", 24, completed = true),
                entry("未来不动", 27),
            ),
            today,
        )
        val byTitle = rolled.associateBy({ it.title }, { Triple(it.day, it.month, it.completed) })

        // 逾期A搬到今天
        assertEquals(Triple(26, 9, false), byTitle["逾期A"])
        // 今天已有同名：留在原地，不搬
        assertTrue(rolled.any { it.title == "今天已有" && it.day == 24 })
        assertTrue(rolled.any { it.title == "今天已有" && it.day == 26 })
        // 完成态与未来不动
        assertEquals(Triple(24, 9, true), byTitle["做完的不动"])
        assertEquals(Triple(27, 9, false), byTitle["未来不动"])
    }

    @Test
    fun evening_copy_keeps_counts_with_evening_kind() {
        val digest = buildReminderDigest(listOf(entry("今天A", 26)), today).eveningCopy()

        assertEquals(ReminderKind.EVENING, digest.kind)
        assertTrue(digest.notificationTitle().contains("还剩1件"))
    }

    @Test
    fun format_minutes_pads_and_wraps_day() {
        assertEquals("8:00", ReminderScheduler.formatMinutes(480))
        assertEquals("21:05", ReminderScheduler.formatMinutes(1265))
        assertEquals("0:00", ReminderScheduler.formatMinutes(0))
        assertEquals("23:59", ReminderScheduler.formatMinutes(1439))
    }
}
