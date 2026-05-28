# Goalday Local Book Page-Turn Visual Physics Refinement

## Context

The current page-turn implementation already supports:

- dedicated page-turn rendering
- drag and velocity release handling
- stronger boundary resistance
- assisted edge-tap turn animation
- stronger destination reveal and fold shadow than the first redesign pass

The remaining gap is that the turn still reads mostly as a rotating sheet. It does not yet communicate enough page thickness, fold crease, spine compression, or late-turn curl emphasis.

## Goal

Improve the visual physics of the page-turn so it feels more like paper curling around a spine:

- page back gains a crease-like gradient and more obvious sheet identity
- the book spine gains a visible compression/shadow response during turns
- page thickness is implied at the turning edge
- late-turn perspective becomes more nonlinear so the page appears to curl harder near completion

## Out of Scope

- new data/state models unrelated to page turning
- editor interactions
- new app modules
- full physical simulation with meshes or shaders

## Product Behavior

### Page Back

- The back side of the turning page should feel like the reverse side of a sheet, not just a tinted panel.
- A soft fold band should appear near the turning edge and intensify as progress increases.

### Spine Compression

- The central spine should look slightly darker and tighter while a page is under tension.
- During active turning, the spine zone should read as a compressed hinge rather than a static divider.

### Thickness Cue

- Near the active turning edge, a narrow bright/dark band should suggest page thickness.
- The thickness cue should strengthen late in the turn and remain subtle early on.

### Nonlinear Curl Feel

- Early turn should stay readable and not over-distort.
- Mid-to-late turn should accelerate the visual curl more than the linear drag progress alone suggests.

## Technical Design

### Derived Progress

Add a derived visual progress inside `BookPageTurner.kt`:

- keep the existing interaction progress for state resolution
- derive a second nonlinear visual progress, for example squaring or easing the drag progress for late-stage emphasis

This avoids changing the release semantics while still making the page look more curled.

### Visual Layers

Refine existing rendering with:

- page-back crease gradient
- edge-thickness highlight/shadow strip
- dynamic spine compression overlay
- stronger late-turn transform values based on visual progress

### Testing Strategy

No snapshot/UI test infrastructure exists yet, so verification for this slice remains:

- focused unit tests for any new pure helper added to `PageTurnState.kt`
- successful debug build
- installable APK copied to `D:\Downloads`

## Acceptance Criteria

This slice is complete when:

- page-back visually reads more like paper
- the spine shows turn-dependent compression/shadow
- turning edge suggests page thickness
- late-turn curl is visually stronger than the previous build
- unit tests still pass
- debug APK builds and is copied to `D:\Downloads`
