package com.bf410.goaldaylocal.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReverseTopicConfigParserTest {
    @Test
    fun parses_reversed_array_shape() {
        val topics = ReverseTopicConfigParser.parse(
            """
            {
              "cn": [
                {
                  "id": 23,
                  "name": "2026愿望清单",
                  "color": "F2C0A5",
                  "cover": "2026",
                  "target": "2026",
                  "linkToSchedule": true,
                  "targets": ["写一封感谢信", "整理房间"]
                }
              ],
              "en": []
            }
            """.trimIndent(),
        )

        assertEquals(1, topics.size)
        assertEquals("23", topics.first().id)
        assertEquals("2026愿望清单", topics.first().title)
        assertEquals("2026", topics.first().coverKey)
        assertEquals("2026", topics.first().targetKey)
        assertEquals("F2C0A5", topics.first().colorHex)
        assertTrue(topics.first().linkToSchedule)
        assertEquals(listOf("写一封感谢信", "整理房间"), topics.first().targets)
    }

    @Test
    fun broken_json_bom_and_array_top_level_return_empty() {
        assertTrue(ReverseTopicConfigParser.parse("{坏掉").isEmpty())
        assertTrue(ReverseTopicConfigParser.parse("\uFEFF").isEmpty())
        assertTrue(ReverseTopicConfigParser.parse("[1,2]").isEmpty())
        assertTrue(ReverseTopicConfigParser.parse("").isEmpty())
    }

    @Test
    fun bad_element_skipped_duplicate_id_suffixed_color_expanded() {
        val topics = ReverseTopicConfigParser.parse(
            """
            {
              "cn": [
                {"id": "dup", "name": "A", "color": "#f60", "targets": ["ok", 123, ""]},
                "坏元素",
                {"id": "dup", "name": "B", "color": "#8033AABB"},
                {"name": "", "color": "zzz"}
              ]
            }
            """.trimIndent(),
        )

        assertEquals(2, topics.size)
        assertEquals("dup", topics[0].id)
        assertEquals("FF6600", topics[0].colorHex)
        assertEquals(listOf("ok"), topics[0].targets)
        assertEquals("dup_2", topics[1].id)
        assertEquals("33AABB", topics[1].colorHex)
    }

    @Test
    fun parses_existing_roots_shape() {
        val topics = ReverseTopicConfigParser.parse(
            """
            {
              "roots": {
                "cn": {
                  "topics": [
                    {
                      "id": "wish_2026",
                      "title": "2026愿望清单",
                      "category": "年度",
                      "cover": "2026",
                      "target": "2026_target",
                      "schedule": false
                    }
                  ]
                }
              }
            }
            """.trimIndent(),
        )

        assertEquals("wish_2026", topics.first().id)
        assertEquals("2026愿望清单", topics.first().title)
        assertEquals("2026_target", topics.first().targetKey)
        assertFalse(topics.first().linkToSchedule)
    }
}
