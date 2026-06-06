# Visual polish feedback pass - 2026-06-06

Scope: clean-room local UI polish based on the current app behavior and readable APK surface cues. This pass does not decompile protected code, bypass packing, add server dependencies, or restore VIP/network-only features.

## Changes

- Bottom navigation now reads more like a persistent app dock: elevated top-rounded container, softer selected item background, clearer selected icon pill, and quieter inactive states.
- Handbook page management actions are grouped into a visible management panel with page position context. Normal actions and destructive actions are separated into two rows.
- Home feedback messages now use a full-width rounded hint pill instead of loose inline text.
- Calendar feedback messages now use the same hint pill treatment for consistent status visibility.

## Follow-up checks

- Continue comparing high-traffic screens against the reference APK screenshots/resources that are readable without code-level reverse engineering.
- Next likely visual targets: handbook page editor chrome, onboarding step hierarchy, empty states, and long-image export preview polish.
