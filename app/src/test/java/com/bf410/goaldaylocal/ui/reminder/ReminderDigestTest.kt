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
    fun evening_copy_keeps_counts_with_evening_kind() {
        val digest = buildReminderDigest(listOf(entry("今天A", 26)), today).eveningCopy()

        assertEquals(ReminderKind.EVENING, digest.kind)
        assertTrue(digest.notificationTitle().contains("还剩1件"))
    }
}
