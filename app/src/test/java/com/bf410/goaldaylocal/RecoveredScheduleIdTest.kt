package com.bf410.goaldaylocal

import com.bf410.goaldaylocal.data.recoveredScheduleId
import com.bf410.goaldaylocal.data.shouldQuarantine
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveredScheduleIdTest {
    @Test
    fun `missing id is stable across reads`() {
        val item = JSONObject()
            .put("title", "晨跑")
            .put("year", 2026)
            .put("month", 9)
            .put("day", 29)
        val first = recoveredScheduleId(item, 0, mutableSetOf())
        val second = recoveredScheduleId(item, 0, mutableSetOf())
        assertEquals("同一份脏数据必须派生同一个 id，否则编辑/删除永远命中不了", first, second)
        assertTrue(first.startsWith("recovered-"))
    }

    @Test
    fun `different content yields different ids`() {
        val a = JSONObject().put("title", "晨跑").put("year", 2026)
        val b = JSONObject().put("title", "夜跑").put("year", 2026)
        assertNotEquals(
            recoveredScheduleId(a, 0, mutableSetOf()),
            recoveredScheduleId(b, 0, mutableSetOf()),
        )
    }

    @Test
    fun `index keeps identical entries apart`() {
        val item = JSONObject().put("title", "晨跑").put("year", 2026)
        val first = recoveredScheduleId(item, 0, mutableSetOf())
        val used = mutableSetOf(first)
        val second = recoveredScheduleId(item, 1, used)
        assertNotEquals("同内容两条必须是不同 id", first, second)
    }

    @Test
    fun `declared id wins when not yet used`() {
        val item = JSONObject().put("id", "real-id").put("title", "晨跑")
        assertEquals("real-id", recoveredScheduleId(item, 0, mutableSetOf()))
    }

    @Test
    fun `duplicate declared id is replaced instead of reused`() {
        val used = mutableSetOf("dup")
        val item = JSONObject().put("id", "dup").put("title", "晨跑")
        val resolved = recoveredScheduleId(item, 1, used)
        assertNotEquals("重复声明的 id 不能原样放行，否则两行会共享一个 key", "dup", resolved)
        assertTrue(resolved.startsWith("recovered-"))
    }

    @Test
    fun `blank id is treated as missing`() {
        val item = JSONObject().put("id", "   ").put("title", "晨跑")
        assertTrue(recoveredScheduleId(item, 0, mutableSetOf()).startsWith("recovered-"))
    }
}

/**
 * 守住一次真实事故：留档守卫写成
 * `if (mmkv.decodeString(QUARANTINE, "") != null) return`
 * MMKV 带默认值时永远不返回 null，守卫恒成立 → 留档从不执行 →
 * 我们以为已经防住的"解析失败后原文被静默覆盖"照旧发生。
 */
class ScheduleQuarantineGuardTest {
    @Test
    fun `first sighting must be quarantined`() {
        assertTrue(
            "尚无留档（key 不存在 → null）时必须留档",
            shouldQuarantine(existing = null),
        )
    }

    @Test
    fun `already quarantined must not be overwritten`() {
        assertFalse(shouldQuarantine(existing = "[{\"title\":\"旧原文\"}]"))
    }

    @Test
    fun `blank existing value still counts as present`() {
        // decodeString(key, "") 返回 ""，不是 null：不能被当成"还没有"
        assertFalse(shouldQuarantine(existing = ""))
    }
}
