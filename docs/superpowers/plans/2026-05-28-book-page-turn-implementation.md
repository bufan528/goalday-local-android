# Book Page-Turn Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the book detail page-turn interaction so it behaves like a real page turn instead of a horizontal slide while preserving existing local page content behavior.

**Architecture:** Extract page-turn gesture resolution into focused state/model code, then move the rendering and interaction into a dedicated `BookPageTurner` composable. Keep `BookViewModel` as the source of truth for selected page state and commit persistent page changes only after turn completion.

**Tech Stack:** Kotlin, Jetpack Compose, Android ViewModel, JUnit4, Gradle

---

## File Structure

- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
  - Pure state/model code for direction, drag state, boundary resistance, and release resolution.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
  - Dedicated composable for layered page-turn rendering and gesture handling.
- Create: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`
  - Unit tests for page-turn threshold and boundary behavior.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`
  - Remove inline page-turn mechanics and delegate to `BookPageTurner`.
- Modify: `app/build.gradle.kts`
  - Add local unit-test dependencies if absent.
- Create: `docs/superpowers/plans/2026-05-28-book-page-turn-implementation.md`
  - This execution plan.

### Task 1: Add Test Support and Failing State Tests

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Add JUnit dependencies for local unit tests**

```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")
}
```

- [ ] **Step 2: Write failing tests for release resolution and boundary resistance**

```kotlin
package com.bf410.goaldaylocal.ui.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTurnStateTest {
    @Test
    fun resolves_next_turn_when_progress_crosses_threshold() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.42f,
            velocity = -120f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.CompleteNext, result)
    }

    @Test
    fun resolves_previous_turn_when_velocity_is_fast_enough() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.PREVIOUS,
            progress = 0.12f,
            velocity = 2100f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.CompletePrevious, result)
    }

    @Test
    fun snaps_back_when_thresholds_are_not_met() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.18f,
            velocity = -200f,
            hasPreviousPage = true,
            hasNextPage = true,
        )

        assertEquals(TurnReleaseResult.SnapBack, result)
    }

    @Test
    fun blocks_next_turn_when_next_page_does_not_exist() {
        val result = resolvePageTurnRelease(
            direction = TurnDirection.NEXT,
            progress = 0.85f,
            velocity = -3000f,
            hasPreviousPage = true,
            hasNextPage = false,
        )

        assertEquals(TurnReleaseResult.SnapBack, result)
    }

    @Test
    fun applies_resistance_when_dragging_past_book_boundary() {
        val resisted = applyBoundaryResistance(rawProgress = 0.4f, canTurn = false)

        assertTrue(resisted in 0f..0.2f)
    }
}
```

- [ ] **Step 3: Run the tests to verify they fail**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: FAIL because `TurnDirection`, `TurnReleaseResult`, `resolvePageTurnRelease`, and `applyBoundaryResistance` do not exist yet.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt
git commit -m "test: add page turn state coverage"
```

### Task 2: Implement Page-Turn State Model

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
- Test: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Implement the minimal pure model to satisfy the tests**

```kotlin
package com.bf410.goaldaylocal.ui.book

import kotlin.math.abs

enum class TurnDirection {
    NEXT,
    PREVIOUS,
}

enum class TurnReleaseResult {
    CompleteNext,
    CompletePrevious,
    SnapBack,
}

private const val TURN_DISTANCE_THRESHOLD = 0.32f
private const val TURN_FLING_THRESHOLD = 1600f
private const val BOUNDARY_RESISTANCE_FACTOR = 0.28f

fun resolvePageTurnRelease(
    direction: TurnDirection,
    progress: Float,
    velocity: Float,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean,
): TurnReleaseResult {
    val canTurn = when (direction) {
        TurnDirection.NEXT -> hasNextPage
        TurnDirection.PREVIOUS -> hasPreviousPage
    }
    if (!canTurn) return TurnReleaseResult.SnapBack

    val progressPasses = progress >= TURN_DISTANCE_THRESHOLD
    val velocityPasses = when (direction) {
        TurnDirection.NEXT -> velocity <= -TURN_FLING_THRESHOLD
        TurnDirection.PREVIOUS -> velocity >= TURN_FLING_THRESHOLD
    }

    return when {
        progressPasses || velocityPasses -> {
            when (direction) {
                TurnDirection.NEXT -> TurnReleaseResult.CompleteNext
                TurnDirection.PREVIOUS -> TurnReleaseResult.CompletePrevious
            }
        }
        else -> TurnReleaseResult.SnapBack
    }
}

fun applyBoundaryResistance(rawProgress: Float, canTurn: Boolean): Float {
    if (canTurn) return rawProgress.coerceIn(0f, 1f)
    return (abs(rawProgress) * BOUNDARY_RESISTANCE_FACTOR).coerceIn(0f, 0.18f)
}
```

- [ ] **Step 2: Run the tests to verify they pass**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt
git commit -m "feat: add page turn state model"
```

### Task 3: Extract a Dedicated Page-Turner Composable

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`

- [ ] **Step 1: Move page-surface responsibilities into `BookPageTurner.kt`**

Code to move and reshape:

```kotlin
@Composable
fun BookPageTurner(
    bookId: String,
    bookTitle: String,
    subtitle: String,
    page: BookPage,
    previousPage: BookPage?,
    nextPage: BookPage?,
    pageIndex: Int,
    pageCount: Int,
    tint: Color,
    isSaved: Boolean,
    diaryDraft: String,
    customPageItems: List<String>,
    onToggleSaved: () -> Unit,
    isChecked: (String, String) -> Boolean,
    onToggleChecked: (String, String) -> Unit,
    onDiaryChange: (String) -> Unit,
    onAddCustomItem: (String) -> Unit,
    onRemoveCustomItem: (String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
)
```

- [ ] **Step 2: Replace raw `dragRatio` state with internal gesture progress and release resolution**

Behavior to implement:

```kotlin
- track direction separately from progress
- derive normalized progress from drag distance and container width
- apply `applyBoundaryResistance` when the target page does not exist
- call `resolvePageTurnRelease` on drag end
- animate to full turn or snap-back
- invoke `onFlipNext` / `onFlipPrevious` only after successful completion animation
```

- [ ] **Step 3: Preserve layered rendering**

Required layers:

```kotlin
- destination page layer
- stable book/spine layer
- active turning page layer
- directional edge shadow
- distinct front/back page treatments
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt
git commit -m "feat: extract dedicated book page turner"
```

### Task 4: Refactor Book Screen Wiring

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`

- [ ] **Step 1: Remove top-level `dragRatio` state from `BookHomeScreen`**

```kotlin
- delete remembered `dragRatio`
- delete `LaunchedEffect` used only to reset drag ratio
- stop threading drag callbacks through `BookDetailView`
```

- [ ] **Step 2: Replace `BookPageSurface(...)` call with `BookPageTurner(...)`**

```kotlin
BookPageTurner(
    bookId = book.id,
    bookTitle = book.title,
    subtitle = book.subtitle,
    page = currentPage,
    previousPage = previousPage,
    nextPage = nextPage,
    pageIndex = uiState.selectedPageIndex,
    pageCount = book.pages.size,
    tint = book.color,
    isSaved = book.id in uiState.savedBookIds,
    diaryDraft = uiState.diaryDraft,
    customPageItems = uiState.customPageItems,
    onToggleSaved = viewModel::toggleSavedCurrentBook,
    isChecked = { pageTitle, item -> viewModel.isChecked(pageTitle, item) },
    onToggleChecked = { pageTitle, item -> viewModel.toggleChecked(pageTitle, item) },
    onDiaryChange = viewModel::updateDiaryDraft,
    onAddCustomItem = viewModel::addCustomPageItem,
    onRemoveCustomItem = viewModel::removeCustomPageItem,
    onAddToSchedule = viewModel::addItemToSchedule,
    onFlipNext = { if (uiState.selectedPageIndex < book.pages.lastIndex) viewModel.setPage(uiState.selectedPageIndex + 1) },
    onFlipPrevious = { if (uiState.selectedPageIndex > 0) viewModel.setPage(uiState.selectedPageIndex - 1) },
)
```

- [ ] **Step 3: Run unit tests again**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt
git commit -m "refactor: wire book screen through page turner"
```

### Task 5: Build and Deliver the APK

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Deliver: `D:\Downloads\goalday-local-android-debug.apk`

- [ ] **Step 1: Build the debug APK**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Copy the latest APK to the delivery location**

Run: `Copy-Item -LiteralPath 'C:\Users\bf410\goalday-local\app\build\outputs\apk\debug\app-debug.apk' -Destination 'D:\Downloads\goalday-local-android-debug.apk' -Force`
Expected: `D:\Downloads\goalday-local-android-debug.apk` exists with a fresh timestamp.

- [ ] **Step 3: Push all commits**

```bash
git push
```
