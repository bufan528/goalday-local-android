# Goalday Handbook Visual Baseline - 2026-06-28

This pass starts moving Goalday toward a stable local handbook design system instead of one-off card styling.

## Direction

- The primary metaphor is a local paper handbook on a warm desk, not a generic dashboard.
- Handbook routes should read as real book sections: overview, schedule, diary, and target.
- New-user guidance should describe concrete local tasks and open the real target area.
- Advanced controls should remain available, but the first path should be readable as write, schedule, review, and export.

## Code Baseline

- `GoaldayDesign` now owns paper, desk, spine, board, shelf, and route colors.
- `GuideOverlay` now shows a task rail for each guide target.
- `BookShell` and the handbook stage now reuse the shared paper/book tokens.

## No-Regression Rules

- Do not replace the handbook stage with plain white cards.
- Do not scatter new near-duplicate paper or route colors across feature files.
- Do not make onboarding a detached tutorial slideshow; keep it tied to real routes and local actions.
- Do not add server-only assumptions to diary, schedule, book, export, or guide flows.
