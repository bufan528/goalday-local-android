package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HandbookLabelsTest {
    @Test
    fun `handbook labels are readable Chinese`() {
        val labels = listOf(
            HandbookLabels.plan,
            HandbookLabels.schedule,
            HandbookLabels.diary,
            HandbookLabels.topics,
            HandbookLabels.settings,
            HandbookLabels.monthPicker,
            HandbookLabels.bookOverview,
            HandbookLabels.bookShelf,
            HandbookLabels.export,
        )

        assertEquals(listOf("计划", "日程", "日记", "主题", "设置", "月份", "总览", "书架", "导出"), labels)
        labels.forEach { label ->
            assertFalse("label must not contain mojibake: $label", label.contains("�"))
            assertFalse("label must not contain mojibake: $label", label.contains("鏃"))
            assertFalse("label must not contain mojibake: $label", label.contains("璁"))
        }
    }

    @Test
    fun `handbook segment labels use the readable contract`() {
        assertEquals(HandbookLabels.plan, LocalHandbookSegment.PLAN.label)
        assertEquals(HandbookLabels.schedule, LocalHandbookSegment.SCHEDULE.label)
        assertEquals(HandbookLabels.diary, LocalHandbookSegment.DIARY.label)
        assertEquals(HandbookLabels.topics, LocalHandbookSegment.TOPICS.label)
    }
}
