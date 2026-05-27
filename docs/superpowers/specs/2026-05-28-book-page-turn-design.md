# Goalday Local Book Page-Turn Redesign

## Context

This spec covers the next implementation slice for `goalday-local`: redesigning the book page-turn interaction so it feels closer to the reference APK. The priority is interaction feel over visual fidelity. Visual detail is in scope only when it reinforces page-turn behavior.

The current implementation already has:

- local-only book data and page selection
- a library view and book detail view
- basic horizontal drag behavior
- page content types for target, plan, schedule, and diary

The current implementation does not yet achieve the intended book-like interaction. It still behaves closer to a horizontal content switch than a convincing page turn.

## Goal

Rebuild the page-turn interaction for the book detail screen so that:

- page dragging is continuous and hand-following
- release behavior resolves cleanly into either page-turn completion or snap-back
- adjacent pages are revealed during the gesture
- edge pages use resistance instead of fake turns
- the structure remains compatible with the existing page content system

This spec applies only to the non-editing viewing state of book pages.

## Out of Scope

The following are explicitly out of scope for this implementation slice:

- rich-text diary editing interaction changes
- full curl physics simulation
- full visual restyling of the whole app
- backup/restore changes
- calendar redesign
- topic-center parity work

## Product Behavior

### Primary Interaction Rules

- Horizontal drag is the only gesture that can trigger a page turn.
- Dragging left on the current page attempts to turn to the next page.
- Dragging right on the current page attempts to turn to the previous page.
- During drag, the page must update continuously with no discrete jump.
- On release, the system must combine distance and velocity to decide whether to complete the turn or snap back.
- A successful turn must reveal the destination page before release.
- A failed turn must animate back as a page being pulled back into place, not as a simple sliding card.

### Boundary Rules

- On the first page, dragging right must not enter a real previous-page turn.
- On the last page, dragging left must not enter a real next-page turn.
- Boundary drag can still provide a small resisted movement to acknowledge touch input.
- Boundary drag must never expose a non-existent page.

### Compatibility Rules

- Existing book/page selection state must remain authoritative after the redesign.
- Existing page content rendering must continue to work with target, plan, schedule, and diary pages.
- Editing behavior is deferred; this slice targets stable viewing behavior first.

## Technical Design

### Component Structure

`BookHomeScreen.kt` remains the screen-level composition entry point, but the page-turn behavior should move out of the current inline drag implementation into a dedicated component.

Proposed structure:

- `BookHomeScreen.kt`
  - owns book-level layout, toolbar actions, tabs, and current page selection
  - passes current, previous, and next page data into a dedicated page-turn renderer
- `BookPageTurner.kt`
  - owns gesture handling, transition progress, edge resistance, release resolution, and animation driving
  - renders the layered page-turn presentation
- `PageTurnState.kt`
  - defines page-turn direction/state types and transition metadata
  - keeps the interaction model explicit and testable

### Rendering Model

The book detail page should render in three semantic layers:

1. Destination page layer
2. Book base/current stack layer
3. Active turning page layer

#### Destination Page Layer

This is the page being progressively revealed during the drag:

- next page when dragging toward the left
- previous page when dragging toward the right

This layer remains visually stable while the active page turns away from it.

#### Book Base Layer

This layer provides the stable book surface and visual continuity underneath the turning page:

- book background
- page frame
- any persistent book styling that should not animate as the active sheet turns

#### Active Turning Page Layer

This layer represents the visible page sheet currently being manipulated by the user:

- moves continuously with drag progress
- rotates or folds around the spine-side anchor
- exposes back-face styling when appropriate
- carries directional shadowing along the fold edge

### Interaction State Machine

The implementation should use an explicit state machine with the following states:

- `Idle`
- `DraggingToNext`
- `DraggingToPrevious`
- `SettlingToNext`
- `SettlingToPrevious`
- `SnappingBack`

#### State Responsibilities

`Idle`

- no active gesture or settlement animation
- current selected page is fully visible

`DraggingToNext` / `DraggingToPrevious`

- active gesture in progress
- stores direction, normalized drag progress, and instantaneous or recent velocity sample

`SettlingToNext` / `SettlingToPrevious`

- release has resolved to a completed page turn
- animation continues from current drag progress to fully turned state
- on completion, the selected page index updates exactly once and state returns to `Idle`

`SnappingBack`

- release did not meet completion conditions
- animation returns from current drag progress to rest state
- page index does not change

### Gesture Resolution Rules

Page turn completion should be based on a combination of:

- normalized drag distance
- release velocity

Recommended rule set:

- complete the page turn if progress exceeds a configured distance threshold
- or complete if release velocity exceeds a configured fling threshold in the active direction
- otherwise snap back

Distance should remain the primary signal. Velocity should be a secondary override for intentional quick turns.

### Progress Model

The interaction should compute a normalized progress value in the range `0f..1f`:

- `0f` means resting page
- `1f` means fully completed turn

The visual transformation, revealed page width, page back-face visibility, and shadow intensity should all derive from this single progress model so the motion stays coherent.

### Edge Resistance

At book boundaries, the interaction should not enter a real drag-to-page-turn state. Instead:

- apply a damped progress response to the gesture
- provide visible but limited movement
- return to rest on release

This keeps the interface responsive without implying an unavailable page.

### Animation Design

This slice does not require a full physically accurate page curl. It does require the following motion cues:

- continuous drag-following movement
- release completion animation toward the destination page
- release snap-back animation toward the origin page
- directional shadow that changes with progress
- distinct page front/back treatment

The first version should favor a reliable hand-feel over ambitious visual simulation.

## Data Flow

- `BookViewModel` remains the source of truth for selected book and selected page index.
- `BookHomeScreen` derives `previousPage`, `currentPage`, and `nextPage` from current selection.
- `BookPageTurner` receives those values plus callbacks:
  - `onFlipNext`
  - `onFlipPrevious`
  - any non-page-turn page actions already needed by content rendering
- `BookPageTurner` owns only ephemeral interaction state.
- Persistent page selection updates happen only after a successful settle animation completes.

This split prevents visual interaction state from corrupting persistent book state.

## Error Handling and Risk Controls

### Risks

- page content and selected index can become unsynchronized if selection updates too early
- gesture code can conflict later with editable page content
- directional transforms can look wrong if the same math is reused for both directions without mirroring rules
- repeated fast turns can leave stale interaction state if animations are not serialized correctly

### Controls

- update selected page only at successful animation completion
- keep transient interaction progress inside the page-turn component
- explicitly model direction instead of inferring it from raw offset everywhere
- block new turn settlement from starting until the current one resolves
- keep editing interactions out of scope for this slice

## Testing Strategy

### Manual Verification

Manual checks must cover:

- middle-page drag left to next page
- middle-page drag right to previous page
- release below threshold snaps back
- release above threshold completes
- release with fast fling completes in the intended direction
- first-page right drag stays resisted and does not turn
- last-page left drag stays resisted and does not turn
- repeated consecutive page turns do not desynchronize content and selected index
- all current page types still render correctly inside the new layered renderer

### Build Verification

- produce a working debug APK after the redesign
- copy the latest installable APK to `D:\Downloads`

## Acceptance Criteria

The implementation is complete for this slice when all of the following are true:

- page turning follows the finger continuously
- successful turns and failed turns resolve consistently
- destination pages are visible during active turn progress
- snap-back reads as a pulled-back page rather than a card slide
- boundary pages resist but never fake-turn
- existing page content still renders correctly
- the app builds successfully and produces an installable APK

## Implementation Order

1. Extract page-turn state types and completion rules into focused code.
2. Introduce a dedicated page-turn component with explicit previous/current/next layering.
3. Replace the old inline drag behavior with the new gesture and settlement model.
4. Add key turn cues: page back-face, directional shadow, revealed destination page, edge resistance.
5. Verify behavior manually across page directions and boundaries.
6. Build a debug APK and place the latest installable output in `D:\Downloads`.
