# Rich Diary Editor Hardening Pass - 2026-06-22

Scope: continue project-wide inspection by tightening the local rich diary editor path.

Changes:
- Restricted WebView navigation to the internal editor asset files and the editor callback scheme.
- Disabled file-page access to other file URLs and universal URLs while keeping the bundled asset editor functional.
- Disabled content access, JavaScript window opening, multiple windows, and mixed content.
- Expanded rich HTML sanitization to remove `data:` links/sources in addition to active script/event content.
- Added unit tests for active-content removal, basic formatting preservation, and stored-length limits.

Verification target:
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`
