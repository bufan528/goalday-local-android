package com.bf410.goaldaylocal.ui.book

internal fun removeExactItem(items: List<String>, item: String): List<String> =
    items.filterNot { it == item }

internal fun renameExactItemDistinct(items: List<String>, oldItem: String, newItem: String): List<String> =
    items.map { item -> if (item == oldItem) newItem else item }.distinct()
