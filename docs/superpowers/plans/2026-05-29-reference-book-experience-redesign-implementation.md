# Reference Book Experience Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the book reading flow around a dedicated book shell, turn engine, and page surface so the local app feels materially closer to the reference APK while also removing core Chinese mojibake.

**Architecture:** Keep the existing `BookViewModel` and local persistence boundaries intact, but split the current monolithic `BookPageTurner.kt` responsibilities into focused UI units. First harden the pure page-turn state helpers with tests, then introduce a new shell/engine/surface composition, migrate the book detail screen onto it, and finally normalize corrupted Chinese copy in the root book flow.

**Tech Stack:** Kotlin, Jetpack Compose, Android View interop for the diary editor, JUnit4, Gradle

---

## File Structure

- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/GoaldayApp.kt`
  - Replace corrupted root tab labels with corrected Chinese copy.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`
  - Keep screen orchestration and dialogs, but route the reading state through the redesigned reader surface and normalize book-flow copy.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookViewModel.kt`
  - Repair Chinese defaults and keep existing reading/editing entry points stable.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
  - Expand the pure turn-state helpers to support the redesigned engine.
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`
  - Add failing tests for the new pure turn-state semantics.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookReader.kt`
  - New top-level reader composition that joins shell, turn engine, and page content.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookShell.kt`
  - Dedicated book chrome, spine, stack, background, and edge-hit-area rendering.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnEngine.kt`
  - The interaction/rendering coordinator for the active turn.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageSurface.kt`
  - Shared paper layout for page content plus the page-type-specific content branches.
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt`
  - Centralized corrected Chinese labels for the core book flow.
- Delete or shrink heavily: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
  - Remove the monolithic implementation after migration; keep only any still-useful helpers that are not better placed elsewhere.

### Task 1: Extend the Pure Turn-State Contract First

**Files:**
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`

- [ ] **Step 1: Write the failing tests for the new turn-state helpers**

```kotlin
@Test
fun drag_update_for_next_turn_accumulates_progress_from_left_swipe() {
    val next = updatedTurnProgress(
        currentProgress = 0.20f,
        dragAmountPx = -120f,
        pageWidthPx = 400f,
        canTurn = true,
    )

    assertEquals(0.50f, next, 0.0001f)
}

@Test
fun drag_update_for_blocked_turn_uses_boundary_resistance() {
    val next = updatedTurnProgress(
        currentProgress = 0.05f,
        dragAmountPx = -180f,
        pageWidthPx = 360f,
        canTurn = false,
    )

    assertTrue(next < 0.20f)
}

@Test
fun reveal_alpha_stays_low_early_and_rises_late() {
    val early = destinationRevealAlpha(0.10f)
    val late = destinationRevealAlpha(0.82f)

    assertTrue(early < 0.20f)
    assertTrue(late > 0.80f)
}
```

- [ ] **Step 2: Run the focused test suite and verify RED**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`

Expected: FAIL because `updatedTurnProgress` and `destinationRevealAlpha` do not exist yet.

- [ ] **Step 3: Add the minimal pure helpers in `PageTurnState.kt`**

```kotlin
fun updatedTurnProgress(
    currentProgress: Float,
    dragAmountPx: Float,
    pageWidthPx: Float,
    canTurn: Boolean,
): Float {
    val safeWidth = pageWidthPx.coerceAtLeast(1f)
    val delta = abs(dragAmountPx) / safeWidth
    val raw = currentProgress + delta
    return applyBoundaryResistance(raw, canTurn)
}

fun destinationRevealAlpha(progress: Float): Float {
    val emphasized = visualTurnProgress(progress).coerceIn(0f, 1f)
    return (0.05f + emphasized * 0.95f).coerceIn(0.05f, 1f)
}
```

- [ ] **Step 4: Run the focused tests and verify GREEN**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`

Expected: PASS.

- [ ] **Step 5: Commit the pure-state expansion**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt
git commit -m "Add helpers for redesigned page turn engine"
```

### Task 2: Introduce Centralized Book Copy and Correct the Root Labels

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/GoaldayApp.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookViewModel.kt`

- [ ] **Step 1: Create the centralized copy file**

```kotlin
package com.bf410.goaldaylocal.ui.book

internal object BookStrings {
    const val appTitle = "Goalday Local"
    const val tabBook = "书本"
    const val tabCalendar = "日历"
    const val tabSettings = "设置"
    const val librarySubtitle = "离线书库与手帐"
    const val createBook = "新建一本书"
    const val backToLibrary = "返回书库"
    const val editBook = "改书"
    const val addPage = "新增页"
    const val renamePage = "改页名"
    const val moveLeft = "左移"
    const val moveRight = "右移"
    const val deletePage = "删页"
    const val deleteBook = "删书"
    const val saveBook = "保存为我的书"
    const val savedBook = "已保存到我的书"
}
```

- [ ] **Step 2: Replace the corrupted root tab labels**

```kotlin
private enum class RootTab(val label: String) {
    BOOK(BookStrings.tabBook),
    CALENDAR(BookStrings.tabCalendar),
    SETTINGS(BookStrings.tabSettings),
}
```

- [ ] **Step 3: Repair the default diary copy in the view model**

```kotlin
else -> DiaryPage(trimmed, "写下这一页最重要的记录。")
```

- [ ] **Step 4: Run the existing unit tests to confirm no regressions**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest --tests com.bf410.goaldaylocal.ui.book.PageContentModeTest`

Expected: PASS.

- [ ] **Step 5: Commit the copy baseline**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt app/src/main/java/com/bf410/goaldaylocal/ui/GoaldayApp.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookViewModel.kt
git commit -m "Normalize core book flow copy"
```

### Task 3: Build the New Reader Shell and Paper Surface

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookShell.kt`
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageSurface.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/RichDiaryEditor.kt`

- [ ] **Step 1: Create the shell container**

```kotlin
@Composable
fun BookShell(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .shadow(34.dp, RoundedCornerShape(42.dp), clip = false)
            .clip(RoundedCornerShape(42.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFC79B75), Color(0xFFE7C7A5), Color(0xFFD2A784)),
                    start = Offset.Zero,
                    end = Offset(1300f, 900f),
                ),
            ),
        content = content,
    )
}
```

- [ ] **Step 2: Create the shared page surface**

```kotlin
@Composable
fun PageSurface(
    modifier: Modifier = Modifier,
    title: String,
    pageNumber: String,
    headerTitle: String,
    headerSubtitle: String,
    tint: Color,
    body: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp, 26.dp, 30.dp, 30.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFFEFB), Color(0xFFFBF4EB), Color(0xFFECD9C0))))
            .padding(horizontal = 28.dp, vertical = 26.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PageHeaderLine(
                bookTitle = headerTitle,
                subtitle = headerSubtitle,
                tint = tint,
                savedText = pageNumber,
            )
            Spacer(Modifier.height(14.dp))
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2D261F))
            Spacer(Modifier.height(16.dp))
            body()
        }
    }
}
```

- [ ] **Step 3: Keep the diary editor API stable while allowing the new surface to host it**

```kotlin
// No signature change required in RichDiaryEditor.
// Only allow layout-related adjustments that keep:
// fun RichDiaryEditor(
//     html: String,
//     placeholder: String,
//     modifier: Modifier = Modifier,
//     pendingCommand: RichEditorCommand? = null,
//     onHtmlChange: (String) -> Unit,
// )
```

- [ ] **Step 4: Run a debug build to validate the new files compile in isolation**

Run: `./gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit the shell and page surface**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookShell.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/PageSurface.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/RichDiaryEditor.kt
git commit -m "Add book shell and shared page surface"
```

### Task 4: Build the New Page Turn Engine

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnEngine.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`

- [ ] **Step 1: Create the engine state model**

```kotlin
internal sealed interface TurnPhase {
    data object Idle : TurnPhase
    data object DraggingNext : TurnPhase
    data object DraggingPrevious : TurnPhase
    data object SettlingForward : TurnPhase
    data object SettlingBack : TurnPhase
}
```

- [ ] **Step 2: Implement the engine composable around the pure helpers**

```kotlin
@Composable
fun PageTurnEngine(
    modifier: Modifier = Modifier,
    canTurnPrevious: Boolean,
    canTurnNext: Boolean,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
    destination: @Composable BoxScope.(Float, TurnDirection?) -> Unit,
    pageBack: @Composable BoxScope.(Float, TurnDirection?) -> Unit,
    activePage: @Composable BoxScope.(Float, TurnDirection?) -> Unit,
) {
    // Own Animatable progress, active direction, velocity tracking, and release resolution here.
    // Use updatedTurnProgress(), resolvePageTurnRelease(), initialEdgeTapProgress(), and visualTurnProgress().
}
```

- [ ] **Step 3: Ensure the engine uses one interaction path for edge taps and drags**

```kotlin
// Both drag release and edge tap should end up invoking the same settle(result) logic,
// with edge taps seeding progress via initialEdgeTapProgress().
```

- [ ] **Step 4: Run the pure tests and the debug build**

Run:
- `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
- `./gradlew.bat assembleDebug`

Expected:
- PASS
- BUILD SUCCESSFUL

- [ ] **Step 5: Commit the engine**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnEngine.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt
git commit -m "Add dedicated page turn engine"
```

### Task 5: Compose the Reader and Migrate the Book Detail Screen

**Files:**
- Create: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookReader.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`

- [ ] **Step 1: Create the top-level reader composition**

```kotlin
@Composable
fun BookReader(
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
    onRenameCustomItem: (String, String) -> Unit,
    onAddToSchedule: (String, Int) -> Unit,
    onFlipNext: () -> Unit,
    onFlipPrevious: () -> Unit,
) {
    BookShell {
        PageTurnEngine(
            canTurnPrevious = previousPage != null,
            canTurnNext = nextPage != null,
            onFlipNext = onFlipNext,
            onFlipPrevious = onFlipPrevious,
            destination = { progress, direction -> /* render reveal page */ },
            pageBack = { progress, direction -> /* render page back */ },
            activePage = { _, _ -> /* render active surface */ },
        )
    }
}
```

- [ ] **Step 2: Replace the old `BookPageTurner` call site in `BookHomeScreen.kt`**

```kotlin
BookReader(
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
    isChecked = viewModel::isChecked,
    onToggleChecked = viewModel::toggleChecked,
    onDiaryChange = viewModel::updateDiaryDraft,
    onAddCustomItem = viewModel::addCustomPageItem,
    onRemoveCustomItem = viewModel::removeCustomPageItem,
    onRenameCustomItem = viewModel::renameCustomPageItem,
    onAddToSchedule = viewModel::addItemToSchedule,
    onFlipNext = { viewModel.setPage((uiState.selectedPageIndex + 1).coerceAtMost(book.pages.lastIndex)) },
    onFlipPrevious = { viewModel.setPage((uiState.selectedPageIndex - 1).coerceAtLeast(0)) },
)
```

- [ ] **Step 3: Preserve the current dialogs and library mode behavior**

```kotlin
// Keep CreateBookDialog, CreatePageDialog, RenamePageDialog, and EditBookDialog unchanged
// unless the new reader requires only local signature updates.
```

- [ ] **Step 4: Run the debug build and smoke-test navigation**

Run: `./gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit the reader migration**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookReader.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt
git commit -m "Migrate book detail screen to redesigned reader"
```

### Task 6: Remove Monolithic Reader Logic and Finish the Core Copy Cleanup

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt`

- [ ] **Step 1: Replace remaining mojibake labels in the book flow**

```kotlin
Text(text = BookStrings.appTitle, ...)
Text(text = BookStrings.librarySubtitle, ...)
Text(text = BookStrings.createBook, ...)
Text(BookStrings.backToLibrary, ...)
Text(BookStrings.editBook, ...)
Text(BookStrings.addPage, ...)
Text(BookStrings.renamePage, ...)
Text(BookStrings.moveLeft, ...)
Text(BookStrings.moveRight, ...)
Text(BookStrings.deletePage, ...)
Text(BookStrings.deleteBook, ...)
```

- [ ] **Step 2: Delete or strip the old `BookPageTurner.kt` down to any still-needed local helpers**

```kotlin
// Preferred end state:
// - page-turn rendering lives in BookShell.kt, PageTurnEngine.kt, and PageSurface.kt
// - BookPageTurner.kt is removed
// If one or two reusable private helpers remain useful, move them into the new files instead.
```

- [ ] **Step 3: Run the targeted tests and full debug build**

Run:
- `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest --tests com.bf410.goaldaylocal.ui.book.PageContentModeTest`
- `./gradlew.bat assembleDebug`

Expected:
- PASS
- BUILD SUCCESSFUL

- [ ] **Step 4: Commit the cleanup**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt
git commit -m "Remove legacy page turner and finish book copy cleanup"
```

### Task 7: Final Verification and APK Output

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Deliver: `D:\Downloads\goalday-local-android-debug.apk`

- [ ] **Step 1: Run the final verification commands**

Run:
- `./gradlew.bat testDebugUnitTest`
- `./gradlew.bat assembleDebug`

Expected:
- all unit tests PASS
- BUILD SUCCESSFUL

- [ ] **Step 2: Copy the debug APK for local side-by-side comparison**

Run: `Copy-Item -LiteralPath 'C:\Users\bf410\goalday-local\app\build\outputs\apk\debug\app-debug.apk' -Destination 'D:\Downloads\goalday-local-android-debug.apk' -Force`

Expected: file copied successfully.

- [ ] **Step 3: Commit the finished redesign slice**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/GoaldayApp.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookViewModel.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookReader.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookShell.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnEngine.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/PageSurface.kt app/src/main/java/com/bf410/goaldaylocal/ui/book/BookStrings.kt docs/superpowers/specs/2026-05-29-reference-book-experience-redesign-design.md docs/superpowers/plans/2026-05-29-reference-book-experience-redesign-implementation.md
git commit -m "Redesign local book reading experience"
```
