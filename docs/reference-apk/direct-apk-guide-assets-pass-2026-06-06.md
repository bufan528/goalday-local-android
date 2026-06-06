# Direct APK Guide Assets Pass - 2026-06-06

## Direct APK Evidence

Commands used:

- `aapt dump badging docs/reference-apk/goalday-reference-base.apk`
- `aapt dump xmltree docs/reference-apk/goalday-reference-base.apk AndroidManifest.xml`
- `unzip -l docs/reference-apk/goalday-reference-base.apk`

Visible evidence:

- Package: `com.first.goalday`, version `Version 2.5.7`, versionCode `56`.
- Launcher is `com.first.goalday.guidemodule.GuideActivity`.
- The APK has visible guide assets under `assets/lottie/`, including `goalday.json`, `coupon.json`, `book.png`, `card.png`, image plates, and star images.
- The APK is protected by visible `.jgapp` and `libjiagu*` assets, so this pass uses only readable assets and manifest/resource names.

## Local Changes

- The local guide overlay now parses `assets/lottie/goalday.json` and `assets/lottie/coupon.json`.
- It surfaces the animation name, frame count, frame rate, canvas size, and a live frame-progress pill in the guide illustration.
- This makes the guide screen visibly asset-driven like the reference `GuideActivity` instead of only checking whether the files exist.

## Boundary

No protected code was decompiled, unpacked, copied, or bypassed. This is a clean-room UI implementation based on readable APK assets.
