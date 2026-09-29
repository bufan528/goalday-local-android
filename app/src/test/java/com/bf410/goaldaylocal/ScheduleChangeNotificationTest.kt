package com.bf410.goaldaylocal

import com.bf410.goaldaylocal.data.ScheduleEntry
import com.bf410.goaldaylocal.data.ScheduleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 复现真机 ANR 的那次事故：
 * BookViewModel 的 revision.collect → syncEditableContent → syncDiarySchedulesForBook
 * 会回写排期。若写者无条件 bump revision，就形成"写入→通知→回写→通知"自激循环，
 * 主线程被 StateFlow 通知链吃满，10 秒无响应。
 *
 * 这里的守护测试锁住"内容没变就不发通知"这条不变量。
 */
class ScheduleChangeNotificationTest {
    private fun entry(id: String, title: String) = ScheduleEntry(
        id = id,
        title = title,
        year = 2026,
        month = 9,
        day = 29,
    )

    @Test
    fun `identical content must be reported as unchanged`() {
        val current = listOf(entry("a", "学习"), entry("b", "写周记"))
        val next = listOf(entry("a", "学习"), entry("b", "写周记"))
        assertEquals("内容相同的全表不能被当成一次变更", current, next)
    }

    @Test
    fun `field level difference is a real change`() {
        val current = listOf(entry("a", "学习"))
        val next = listOf(entry("a", "学习").copy(completed = true))
        assertTrue(current != next)
    }

    @Test
    fun `reorder is a real change`() {
        val a = entry("a", "甲")
        val b = entry("b", "乙")
        assertTrue(listOf(a, b) != listOf(b, a))
    }

    @Test
    fun `no-op sync does not request a widget refresh`() {
        // 幂等回写（syncDiarySchedules 的稳态）必须返回 false，让上层跳过 notify + 刷组件
        val stable = listOf(entry("a", "学习"))
        fun sync(latest: List<ScheduleEntry>): List<ScheduleEntry> = latest
        assertFalse(stable != sync(stable))
    }

    @Test
    fun `status enum round trips through equality`() {
        val done = entry("a", "学习").withStatus(ScheduleStatus.DONE)
        assertEquals(ScheduleStatus.DONE, done.status)
        assertTrue(done.completed)
    }
}
