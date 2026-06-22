package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RichDiaryEditorTest {
    @Test
    fun sanitizeRichHtmlRemovesActiveContent() {
        val raw = """
            <p onclick="steal()">今天完成了阅读</p>
            <script>alert('x')</script>
            <iframe src="https://example.com"></iframe>
            <a href="javascript:alert(1)">bad</a>
            <img src="data:text/html;base64,PHNjcmlwdD4=">
        """.trimIndent()

        val sanitized = sanitizeRichHtml(raw)

        assertFalse(sanitized.contains("<script", ignoreCase = true))
        assertFalse(sanitized.contains("<iframe", ignoreCase = true))
        assertFalse(sanitized.contains("onclick", ignoreCase = true))
        assertFalse(sanitized.contains("javascript:", ignoreCase = true))
        assertFalse(sanitized.contains("data:text/html", ignoreCase = true))
        assertTrue(sanitized.contains("今天完成了阅读"))
    }

    @Test
    fun sanitizeRichHtmlKeepsBasicFormatting() {
        val raw = "<h1>标题</h1><blockquote>引用</blockquote><ul><li>任务</li></ul>"

        assertEquals(raw, sanitizeRichHtml(raw))
    }

    @Test
    fun sanitizeRichHtmlCapsStoredLength() {
        val sanitized = sanitizeRichHtml("x".repeat(25_000))

        assertEquals(20_000, sanitized.length)
    }
}
