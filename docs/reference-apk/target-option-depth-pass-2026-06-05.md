# Target option depth pass - 2026-06-05

Scope: clean-room parity pass for target detail option behavior.

## Reference Signals

- `TargetDetailActivity`
- `target_detail_options`
- `bg_pop_target_detail_option`
- target detail date/option resources

## Problems Addressed

- The local target detail route exposed a `保存为我的目标` action, but it only wrote a note.
- Target options were shown as a simple horizontal pill row instead of the more explicit option-row model implied by the APK resources.

## Changes

- Wired `保存为我的目标` to the real local target store through `addCustomPageItem`.
- Rebuilt the target option panel as action rows with code badges, title, subtitle, and action affordance.
- Kept local-only behavior: save-as-own, review scheduling, and execution sentence generation all persist locally without account/VIP/server flows.

## Verification

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug`
