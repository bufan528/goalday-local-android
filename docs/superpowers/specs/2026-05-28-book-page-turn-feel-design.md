# Goalday Local Book Page-Turn Feel Refinement

## Context

The current `goalday-local` book page-turn redesign already replaced the old inline drag behavior with a dedicated page-turn component and pure state model. The app now supports:

- drag-to-turn state resolution
- success vs snap-back release handling
- previous/next page layering
- dedicated page-turn rendering

That work improved structure and baseline behavior, but the interaction still falls short of the intended reference feel. The current page turn is functionally correct but still too uniform in drag response, release behavior, and edge animation.

## Goal

Improve page-turn feel so the interaction reads more like a physical page:

- boundary drag becomes progressively heavier instead of using a single resistance ratio
- release behavior distinguishes between slow half-turns, weak drags, and fast intentional flicks
- snap-back feels pulled back instead of simply resetting
- non-drag edge taps animate as short assisted turns rather than instant state jumps
- page-back, shadow, and destination reveal react more clearly to progress

## Out of Scope

- changing data models outside page-turn interaction state
- diary editor behavior changes
- calendar work
- backup/restore
- full curl physics simulation
- cross-book transitions

## Product Behavior

### Drag Response

- Early drag should move relatively freely.
- As the page approaches the end of available range, movement should feel heavier.
- Boundary drag on unavailable pages should acknowledge input but clamp harder as drag increases.

### Release Resolution

- A long slow drag beyond the distance threshold should complete.
- A short, weak drag should snap back.
- A fast flick in the active direction should complete even if distance is shorter.
- Opposing velocity at release should bias toward snap-back.

### Assisted Edge Turn

- Tapping the left or right page edge should not jump immediately to the next state.
- Instead, it should start from a small committed progress and animate into a completed turn.

### Visual Cues

- Page-back tint should vary more with progress.
- Shadow width and opacity should increase more clearly near mid/late turn.
- Destination page opacity should start lower and grow more noticeably with reveal progress.
- Active page translation should be direction-aware and slightly stronger late in the turn.

## Technical Design

### State Model Changes

Extend `PageTurnState.kt` with:

- a more expressive drag resistance calculation
- explicit helper for assisted turn start progress
- release decision that considers opposing vs supporting velocity
- differentiated animation presets for completion and snap-back

Suggested helpers:

- `applyTurnResistance(rawProgress: Float, canTurn: Boolean): Float`
- `initialEdgeTapProgress(): Float`
- `PageTurnAnimationSpec` or equivalent duration/easing constants

### Component Changes

Update `BookPageTurner.kt` to:

- use the refined resistance helper instead of fixed boundary scaling
- use different settle animation timings for completion vs snap-back
- apply a seeded progress for edge-tap turns before settling
- strengthen destination page fade, page-back tone, and fold-shadow response

### Data Flow

No persistent data flow changes are required:

- `BookViewModel` remains unchanged
- selected page still updates only after a successful completion animation

## Testing Strategy

### Unit Tests

Add tests for:

- stronger clamping at unavailable boundaries
- completion on supporting fling velocity
- snap-back on opposing release velocity
- stable assisted edge-turn start progress

### Build Verification

- run focused unit tests for `PageTurnStateTest`
- run `assembleDebug`
- copy the latest APK to `D:\Downloads`

## Acceptance Criteria

This refinement slice is complete when:

- drag resistance is perceptibly stronger near boundaries
- short flicks can complete turns intentionally
- weak drags still snap back
- edge taps animate into turns instead of jumping instantly
- shadow, page-back, and reveal cues are visibly stronger than the previous build
- the app still builds and produces an installable APK
