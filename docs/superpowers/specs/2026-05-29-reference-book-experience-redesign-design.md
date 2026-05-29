# Goalday Local Reference Book Experience Redesign

## Context

The local-only Android project at `C:\Users\bf410\goalday-local` already has:

- a Compose-based library and reading flow
- a custom page-turn implementation in `BookPageTurner.kt`
- local persistence for custom books, saved books, diary text, and checklist state
- a rich diary editor restored in recent commits

The current implementation is functional, but it still falls short of the local reference APK in three visible ways:

- the page turn reads more like a rotating panel than a physical page
- the reading page layout and surrounding shell do not yet feel like a cohesive book object
- several Chinese strings are visibly mojibake and reduce fidelity immediately

The user confirmed the target for this redesign is the actual local reference APK behavior, with priority on matching visual feel, interaction feel, and overall reading flow even if the change requires a substantial restructuring.

## Goal

Redesign the book-reading experience so it tracks the local reference APK much more closely by:

- rebuilding the reading surface around a dedicated book shell and page-turn engine
- making page turning feel like a weighted physical sheet rather than a simple 3D card rotation
- unifying page presentation into a stronger paper/book visual language
- fixing the current corrupted Chinese copy in the core book experience

## Out of Scope

- major changes to persistence or storage architecture
- broad redesign of calendar or settings modules
- feature expansion unrelated to the core book-reading flow
- exact byte-for-byte or implementation-level cloning of the reference APK internals

## Product Behavior

### Overall Reading Experience

- Opening a book should feel like entering a dedicated book object, not just a generic app detail page.
- The shell, spine, page stack, page margins, and turn shadows should work together as one visual system.
- Edge taps and drag gestures should both use the same turn semantics so the book always responds consistently.

### Page-Turn Feel

- A turn should begin with resistance, build into a clearer reveal of the destination page, and then either settle forward or snap back with weight.
- The user should perceive separate visual roles for the current page, the page back, and the destination page.
- Late-turn motion should feel heavier and more committed than the current implementation.

### Page Content Presentation

- Reading pages should share a common paper treatment regardless of whether the content is diary, target, plan, or schedule.
- Editing controls should not dominate the reading state.
- Entering editing mode should be explicit enough to avoid gesture conflicts with turning.

### Core Chinese Copy

- Visible mojibake in the root tabs, library/book screens, and page-turn UI must be replaced with correct Chinese copy.
- Copy should be consistent across library, reading, editing, and action labels.

## Technical Design

### Architecture Split

Replace the current monolithic responsibilities in `BookPageTurner.kt` with three clearer layers:

1. `BookShell`
   - owns the outer book framing
   - renders cover thickness, spine, paper stack cues, ambient shading, and edge tap zones
   - does not know page-specific content semantics

2. `PageTurnEngine`
   - owns page-turn state, drag progress, release resolution, and page index handoff
   - renders the coordinated turn layers for current page, turning page back, and revealed destination page
   - provides a single interaction model for drag turns and edge-tap turns

3. `PageSurface`
   - owns actual page content composition
   - renders diary, target, plan, and schedule pages through a common paper layout vocabulary
   - keeps content editing concerns separate from turn physics

This split is intended to raise the ceiling on visual fidelity without forcing storage or domain-level rewrites.

### Page-Turn State Model

The redesigned turn engine should use explicit turn phases rather than a single visual progress value:

- `Idle`
- `DraggingNext`
- `DraggingPrevious`
- `SettlingForward`
- `SettlingBack`

The release decision should use both:

- drag distance
- release velocity

This keeps the gesture response close to the reference expectation and prevents edge taps from behaving like a different subsystem.

### Turn Rendering Model

The turn should be rendered as coordinated layers instead of a single rotating content panel:

- `StaticStackLayer`
- `RevealPageLayer`
- `TurningPageFrontLayer`
- `TurningPageBackLayer`

Key visual signals:

- stronger page-back identity
- dynamic fold/crease shading
- spine compression while a page is under tension
- edge thickness cues
- nonlinear late-stage curl emphasis

The implementation may still use Compose as the primary rendering technology, but the design does not constrain the solution to Compose-only techniques if another local, maintainable Android rendering path produces a closer result.

### Page Surface Redesign

`PageSurface` should establish one shared paper layout system:

- page header
- page title
- content area
- page footer / page number
- consistent inner margins and paper tint

Content-specific pages should adapt within that shell:

- `DiaryPage` keeps rich editing support, but defaults to a stronger reading presentation
- `TargetPage`, `PlanPage`, and `SchedulePage` keep their functional differences while sharing one visual structure

Editing states should temporarily suppress turn gestures when necessary to avoid accidental page turns.

### Text and Copy Repair

This redesign includes a targeted text cleanup in at least these files:

- `ui/GoaldayApp.kt`
- `ui/book/BookHomeScreen.kt`
- `ui/book/BookPageTurner.kt`
- `ui/book/BookViewModel.kt`

The goal is not only to repair encoding damage, but to normalize wording used by the reading flow so the experience feels intentional and finished.

## Implementation Boundaries

This redesign should preserve:

- the existing `BookViewModel` entry points where practical
- existing local persistence behavior
- current book/page domain models unless a narrow UI-facing adapter is clearly justified

This redesign may change:

- reading UI file structure
- page-turn rendering approach
- internal UI state organization
- how page content composables are grouped and named

## Testing Strategy

No mature screenshot regression setup exists in the project, so verification for this redesign should include:

- new focused automated tests for any extracted pure turn-state helpers
- successful project tests for touched areas
- successful debug build
- manual runtime verification of the reading flow against the reference APK

## Acceptance Criteria

This redesign slice is complete when:

- the reading experience is clearly structured around a book shell, page-turn engine, and page surface
- turn interactions feel materially closer to the reference APK than the current rotating-panel implementation
- reading pages share a stronger and more consistent paper/book visual language
- visible Chinese mojibake in the core book flow is removed
- local editing and persistence behavior still works in the redesigned reading flow
- tests added for new pure helpers pass
- the app builds successfully for manual comparison
