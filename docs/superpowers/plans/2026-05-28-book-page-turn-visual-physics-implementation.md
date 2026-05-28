# Book Page-Turn Visual Physics Refinement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the page turn look more like curling paper by adding crease gradients, spine compression, edge thickness cues, and stronger nonlinear late-turn perspective.

**Architecture:** Keep interaction logic stable, optionally add a small pure helper for derived visual progress, and concentrate most changes inside `BookPageTurner.kt`.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit4, Gradle

---

## File Structure

- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
  - Add a pure helper for nonlinear visual progress if needed.
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`
  - Add a small unit test for the nonlinear visual progress helper.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
  - Add page-back crease, spine compression, thickness cues, and stronger late-turn transforms.

### Task 1: Add Failing Test for Visual Progress Helper

**Files:**
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Add a failing test for nonlinear visual progress**

```kotlin
@Test
fun visual_progress_emphasizes_late_turn_more_than_linear_progress() {
    val visual = visualTurnProgress(0.7f)
    assertTrue(visual > 0.7f)
}
```

- [ ] **Step 2: Run the focused test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: FAIL because `visualTurnProgress` does not exist yet.

### Task 2: Implement Visual Progress Helper

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Add the helper**

Implementation direction:

```kotlin
fun visualTurnProgress(progress: Float): Float
```

Behavior:

- clamp into `0f..1f`
- return a value that stays close early and lifts higher late in the turn

- [ ] **Step 2: Run the focused test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: PASS

### Task 3: Apply Visual Physics in BookPageTurner

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`

- [ ] **Step 1: Use nonlinear visual progress for late-turn transforms**

- [ ] **Step 2: Add page-back crease gradient and thickness strip**

- [ ] **Step 3: Add dynamic spine compression overlay**

- [ ] **Step 4: Rebuild and verify**

Run:
- `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
- `./gradlew.bat assembleDebug`

Expected:
- focused unit test PASS
- debug build succeeds

### Task 4: Deliver and Push

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Deliver: `D:\Downloads\goalday-local-android-debug.apk`

- [ ] **Step 1: Copy the APK**

Run: `Copy-Item -LiteralPath 'C:\Users\bf410\goalday-local\app\build\outputs\apk\debug\app-debug.apk' -Destination 'D:\Downloads\goalday-local-android-debug.apk' -Force`

- [ ] **Step 2: Commit and push**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt \
        app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt \
        app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt \
        docs/superpowers/specs/2026-05-28-book-page-turn-visual-physics-design.md \
        docs/superpowers/plans/2026-05-28-book-page-turn-visual-physics-implementation.md
git commit -m "Refine book page turn visual physics"
git push
```
