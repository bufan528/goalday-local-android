# Handbook Rebuild From Reverse Assets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the local Goalday handbook module around the reversed APK hand账 resources and operation model.

**Architecture:** Keep the existing Compose Android app and MMKV persistence. Add focused model/adapters for reversed topic assets and in-book schedule rows, then replace the current handbook shell with reference-shaped Plan, Schedule, Diary, and Topics surfaces.

**Tech Stack:** Kotlin, Jetpack Compose, Android Gradle Plugin, MMKV, JUnit 4.

---

## File Structure

- Modify `app/src/main/java/com/bf410/goaldaylocal/ui/book/BookHomeScreen.kt`: route handbook mode into the rebuilt shell.
- Modify `app/src/main/java/com/bf410/goaldaylocal/ui/book/LocalHandbookWorkspace.kt`: replace mixed workspace with reference-shaped tab shell and surfaces.
- Create `app/src/main/java/com/bf410/goaldaylocal/ui/book/HandbookReferenceModels.kt`: UI models for tabs, schedule slots, diary blocks, plan rows, and topic cards.
- Create `app/src/main/java/com/bf410/goaldaylocal/data/ReverseTopicConfigParser.kt`: parser for `assets/topic_center_config.json` variants.
- Modify `app/src/main/java/com/bf410/goaldaylocal/ui/book/PageTurnState.kt`: fix handbook edge-drag threshold behavior.
- Add tests in `app/src/test/java/com/bf410/goaldaylocal/ui/book/HandbookReferenceModelsTest.kt`.
- Add tests in `app/src/test/java/com/bf410/goaldaylocal/data/ReverseTopicConfigParserTest.kt`.

## Tasks

### Task 1: Fix Handbook Edge Turn Regression

- [ ] Write a failing/confirming test in `PageTurnStateTest` for right-edge left drag with handbook threshold ratios.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.PageTurnStateTest`.
- [ ] Adjust `resolveDragTurnDirection` so the threshold is usable for a 400 px page and an 80 px drag.
- [ ] Re-run the same test class.

### Task 2: Add Reference Schedule Slot Model

- [ ] Create `HandbookReferenceModelsTest` covering six slots split into two columns of three rows.
- [ ] Implement `HandbookReferenceModels.kt` with `HandbookScheduleDay`, `HandbookScheduleSlot`, and `buildHandbookScheduleDay`.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.ui.book.HandbookReferenceModelsTest`.

### Task 3: Parse Reversed Topic Config

- [ ] Create `ReverseTopicConfigParserTest` with samples for the reversed array shape and current local roots shape.
- [ ] Implement `ReverseTopicConfigParser.parse`.
- [ ] Make parser tolerate mojibake source text but prefer explicit `name`, `cover`, `target`, `color`, `linkToSchedule`, and inline `targets`.
- [ ] Run `./gradlew.bat testDebugUnitTest --tests com.bf410.goaldaylocal.data.ReverseTopicConfigParserTest`.

### Task 4: Rebuild Handbook Shell

- [ ] Replace `LocalHandbookWorkspace` content with a top reference tab bar and pager-like body.
- [ ] Implement Plan, Schedule, Diary, and Topics tabs as separate private composables in `LocalHandbookWorkspace.kt`.
- [ ] Keep existing callbacks to `BookViewModel` for persistence.
- [ ] Ensure no login, VIP, payment, ad, or network UI appears.
- [ ] Run focused unit tests and compile with `./gradlew.bat testDebugUnitTest`.

### Task 5: Connect Topics To Local Assets

- [ ] Load `topic_center_config.json` from assets through the new parser.
- [ ] Display topic cards with `assets/cover/<cover>.png` when available.
- [ ] Add save/import actions that call existing `BookViewModel` methods where possible.
- [ ] Add fallback text when asset parsing fails.
- [ ] Run topic parser tests and compile.

### Task 6: Build And Deliver

- [ ] Run `./gradlew.bat testDebugUnitTest`.
- [ ] Run `./gradlew.bat assembleDebug`.
- [ ] Copy latest debug APK to `D:\Downloads\goalday-local-debug.apk`.
- [ ] Inspect `git status`.
- [ ] Commit the implementation.
- [ ] Push to GitHub.
