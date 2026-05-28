# Book Page-Turn Feel Refinement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the page-turn interaction feel with stronger resistance, better release resolution, assisted edge-turn animation, and clearer visual response.

**Architecture:** Keep the existing page-turn component split, refine pure interaction helpers in `PageTurnState.kt`, then wire those stronger semantics into `BookPageTurner.kt`. Preserve the current screen/view-model boundary.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit4, Gradle

---

## File Structure

- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
  - Add refined resistance, velocity-sensitive release logic, and edge-tap start progress.
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`
  - Add failing tests for the refined page-turn feel rules.
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`
  - Consume the refined helpers and strengthen animation/visual response.

### Task 1: Add Failing Tests for Refined Turn Feel

**Files:**
- Modify: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Add failing tests for resistance, opposing velocity, and edge-tap progress**

```kotlin
@Test
fun boundary_resistance_clamps_large_unavailable_drag_more_aggressively() {
    val resisted = applyBoundaryResistance(rawProgress = 0.9f, canTurn = false)
    assertTrue(resisted < 0.18f)
}

@Test
fun opposing_velocity_snaps_back_even_when_progress_is_near_threshold() {
    val result = resolvePageTurnRelease(
        direction = TurnDirection.NEXT,
        progress = 0.30f,
        velocity = 900f,
        hasPreviousPage = true,
        hasNextPage = true,
    )
    assertEquals(TurnReleaseResult.SnapBack, result)
}

@Test
fun edge_tap_progress_starts_in_turning_range() {
    val progress = initialEdgeTapProgress()
    assertTrue(progress in 0.18f..0.35f)
}
```

- [ ] **Step 2: Run the focused test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: FAIL because the current implementation does not expose `initialEdgeTapProgress` and does not satisfy the tighter resistance/release expectations.

### Task 2: Refine the Page-Turn State Model

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`
- Test: `app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt`

- [ ] **Step 1: Implement stronger resistance and velocity-sensitive release**

Implementation direction:

```kotlin
- lower available turn clamping remains 0f..1f
- unavailable boundary drag uses a curved response rather than one constant factor
- supporting fling still completes
- opposing velocity near threshold biases snap-back
- add `initialEdgeTapProgress()`
```

- [ ] **Step 2: Run the focused test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
Expected: PASS

### Task 3: Apply Feel Refinements in the Composable

**Files:**
- Modify: `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt`

- [ ] **Step 1: Use refined helpers for drag resistance and edge tap**

Implementation direction:

```kotlin
- use refined resistance during drag
- seed progress from `initialEdgeTapProgress()` for edge taps
- use different animation durations for completion vs snap-back
```

- [ ] **Step 2: Strengthen visual response**

Implementation direction:

```kotlin
- destination page reveal begins dimmer and ramps up harder
- page-back tone changes more with progress
- shadow width/opacity increase more near late turn
- active page translation increases slightly late in the gesture
```

- [ ] **Step 3: Run verification**

Run:
- `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`
- `./gradlew.bat assembleDebug`

Expected:
- focused unit test PASS
- debug build succeeds

### Task 4: Deliver APK and Push

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Deliver: `D:\Downloads\goalday-local-android-debug.apk`

- [ ] **Step 1: Copy the fresh APK**

Run: `Copy-Item -LiteralPath 'C:\Users\bf410\goalday-local\app\build\outputs\apk\debug\app-debug.apk' -Destination 'D:\Downloads\goalday-local-android-debug.apk' -Force`

- [ ] **Step 2: Commit and push**

```bash
git add app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt \
        app/src/main/java/com/bf410/goaldaylocal/ui/book/BookPageTurner.kt \
        app/src/test/java/com/bf410/goaldaylocal/ui/book/PageTurnStateTest.kt \
        docs/superpowers/specs/2026-05-28-book-page-turn-feel-design.md \
        docs/superpowers/plans/2026-05-28-book-page-turn-feel-implementation.md
git commit -m "Refine book page turn feel"
git push
```
