package com.bf410.goaldaylocal.ui.book

data class HandbookScheduleSlot(
    val index: Int,
    val text: String,
    val completed: Boolean = false,
)

data class HandbookScheduleDay(
    val dayText: String,
    val weekText: String,
    val leftColumn: List<HandbookScheduleSlot>,
    val rightColumn: List<HandbookScheduleSlot>,
) {
    val slots: List<HandbookScheduleSlot> = leftColumn + rightColumn
}

fun buildHandbookScheduleDay(
    dayText: String,
    weekText: String,
    items: List<String>,
    completedItems: Set<String> = emptySet(),
): HandbookScheduleDay {
    val slots = (0 until 6).map { index ->
        val text = items.getOrNull(index).orEmpty()
        HandbookScheduleSlot(
            index = index,
            text = text,
            completed = text.isNotBlank() && text in completedItems,
        )
    }
    return HandbookScheduleDay(
        dayText = dayText,
        weekText = weekText,
        leftColumn = slots.take(3),
        rightColumn = slots.drop(3),
    )
}
