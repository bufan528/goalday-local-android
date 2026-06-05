package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Test

class InspirationTemplatesTest {
    @Test
    fun target_asset_candidates_accept_local_and_reference_apk_names() {
        val candidates = targetAssetCandidates("assets/topictarget/life_target.txt")

        assertEquals(listOf("topictarget/life_target.txt", "topictarget/life.txt"), candidates)
    }

    @Test
    fun target_asset_candidates_accept_reference_apk_name_first() {
        val candidates = targetAssetCandidates("assets/topictarget/topicweek.txt")

        assertEquals(listOf("topictarget/topicweek.txt", "topictarget/topicweek_target.txt"), candidates)
    }

    @Test
    fun fallback_topic_catalog_summary_matches_local_template_count() {
        val summary = fallbackTopicCatalogSummary("assets/topic_center_config.json")

        assertEquals(0, summary.rootCount)
        assertEquals(InspirationTemplates.all.size, summary.topicCount)
        assertEquals("assets/cover", summary.coverRoot)
        assertEquals("assets/topictarget", summary.targetRoot)
        assertEquals("本地专题", summary.label)
    }
}
