package com.bf410.goaldaylocal.ui.widget

import com.bf410.goaldaylocal.ui.widget.DiaryAddWidgetProvider.Companion.MAX_CONTENT_CHARS
import com.bf410.goaldaylocal.ui.widget.DiaryAddWidgetProvider.Companion.diaryWidgetContent
import com.bf410.goaldaylocal.ui.widget.DiaryAddWidgetProvider.Companion.diaryWidgetTitle
import com.bf410.goaldaylocal.ui.widget.DiaryAddWidgetProvider.Companion.diaryWidgetUserText
import com.bf410.goaldaylocal.ui.widget.DiaryAddWidgetProvider.Companion.isDiaryWidgetLocked
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 对照原版 `DiaryWidgetUpdateService` + `WidgetUtils.formatStr`：
 * 标题 `M月d日 EEE`，正文去 img 截 50 字，为空回落占位。
 */
class DiaryAddWidgetTest {

    @Test
    fun title_matches_original_date_pattern() {
        assertEquals("9月16日 周三", diaryWidgetTitle(LocalDate.of(2026, 9, 16)))
        assertEquals("9月20日 周日", diaryWidgetTitle(LocalDate.of(2026, 9, 20)))
        assertEquals("9月14日 周一", diaryWidgetTitle(LocalDate.of(2026, 9, 14)))
    }

    @Test
    fun user_text_mirrors_structured_diary_parsing() {
        assertEquals("", diaryWidgetUserText(""))
        assertEquals("", diaryWidgetUserText("   "))
        // 无结构即整段原文
        assertEquals("开心", diaryWidgetUserText("开心"))
        // 取富文本分区
        val raw = "# 日期\n2026-09-16\n# 富文本\n今天很开心\n# 图片\n"
        assertEquals("今天很开心", diaryWidgetUserText(raw))
        // 无富文本分区即空
        assertEquals("", diaryWidgetUserText("# 日期\n2026-09-16\n"))
    }

    @Test
    fun content_strips_img_and_truncates_to_50() {
        val placeholder = "今天想记些什么呢?"
        assertEquals(placeholder, diaryWidgetContent("", placeholder))
        assertEquals(placeholder, diaryWidgetContent("   ", placeholder))
        assertEquals("开心", diaryWidgetContent("开心", placeholder))
        // img 标签去除（对照 WidgetUtils.formatStr 的 <img> 正则）
        assertEquals("abc", diaryWidgetContent("a<img src=\"x\"/>bc", placeholder))
        // 50 字截断（对照 formatStr(content, 50)，无省略号）
        val long = "字".repeat(80)
        val out = diaryWidgetContent(long, placeholder)
        assertEquals(50, out.length)
        assertEquals("字".repeat(50), out)
        // 纯 img 内容视为空，回落占位
        assertEquals(placeholder, diaryWidgetContent("<img src=\"x\"/>", placeholder))
    }

    @Test
    fun local_build_is_never_locked() {
        // 本地版无 VIP 门槛（对照原版锁形遮罩分支，结构保留）
        assertFalse(isDiaryWidgetLocked())
        assertTrue(MAX_CONTENT_CHARS == 50)
    }
}
