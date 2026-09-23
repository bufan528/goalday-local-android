package com.bf410.goaldaylocal.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 勾选键迁移（hashCode → SHA-256）：旧键 "Aa"/"BB" 同为 2112 会串勾选态，
 * 新键必须区分；纯函数单测，不依赖 MMKV。
 */
class CheckKeyDigestTest {

    @Test
    fun classicCollisionPairGetsDistinctDigests() {
        // 前提：旧方案确实碰撞
        assertEquals("Aa".hashCode(), "BB".hashCode())
        assertNotEquals(sha256Hex("Aa"), sha256Hex("BB"))
    }

    @Test
    fun digestIsStable64HexLowercase() {
        val first = sha256Hex("完成或取消完成事件：点击事件前或键盘上方的")
        assertEquals(first, sha256Hex("完成或取消完成事件：点击事件前或键盘上方的"))
        assertEquals(64, first.length)
        assertTrue(first.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun emptyAndLongInputs() {
        assertEquals(64, sha256Hex("").length)
        assertNotEquals(sha256Hex("a".repeat(500)), sha256Hex("b".repeat(500)))
    }

    @Test
    fun matchesSha256StandardVector() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha256Hex("abc"),
        )
    }
}
