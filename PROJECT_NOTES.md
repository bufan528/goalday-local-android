# Project Notes

## Project

- Name: `goalday-local`
- Goal: reverse-engineer the reference APK and recreate an Android app that is as close as practical in local-only usage, interaction, and overall feel.

## User Requirements

- The app must be fully local-only.
- No server dependency is allowed.
- No login, VIP, payment, subscription, or ad flows are needed.
- The app is for personal local use, not commercial deployment.
- The recreated app should stay as close as possible to the reference APK in:
  - feature set
  - visual structure
  - interaction details
  - motion/effects
- The book metaphor is a hard requirement.
- The page-turn effect must feel like a real book, not just a basic horizontal swipe.
- UI can reference iOS polish, but should still follow the original APK's information architecture and core visual identity.
- Users must be able to create and edit their own content locally:
  - books
  - pages
  - schedules
  - diary content
  - checklist items
- When a task is marked completed, text must become gray and show a strikethrough. Tapping again must clear that state.
- Calendar/day interactions should support tap-to-add behavior similar to the original APK.

## Delivery Constraints

- Always place the latest installable APK in `D:\Downloads`.
- Prefer keeping heavy Android/Gradle caches and tooling on `D:` when possible because `C:` space is limited.

## Reference APK

- Original APK path:
  - `D:\电脑管家迁移文件\xwechat_files\wxid_dfb9b3ch4lju22_65fe\msg\file\2026-05\base.apk.1(1).1`
- Reverse-engineering notes already established:
  - package name is `com.first.goalday`
  - original app contains topic center, book module, diary module, calendar-related capability, backup, and more content-heavy assets
  - original diary uses local asset-based rich editor files
  - code appears protected/packed, so resource and asset analysis is more reliable than direct source recovery

## Current Product Direction

- Keep prioritizing parity for local core modules first:
  - book/library
  - page editing
  - diary editing
  - schedule/calendar
  - backup/restore
  - page-turn interaction
- Business/network features remain intentionally excluded unless the user later asks otherwise.

## Current Known Gaps

- Page turning is improved but still not at full reference parity.
- The app still needs more original-like visual detail and richer module completeness.
- Topic-center scale/content breadth from the original APK is not yet fully replicated.
- More reverse-engineered interaction details still need to be brought over.

## Working Rule

- Do not describe a feature as fully matched to the original APK unless it is actually verified to be close in behavior and feel.
- Prefer verified build evidence over assumptions.

## 对齐进度备忘（2026-09-06 深度对照轮）

### 已对齐（本轮验证）
- 周右栏=清单条目全集（原版语义），完成=橙勾框+灰字删除线
- 记录 Tab 今日完成橙红渐变卡片（含 @来源清单）
- 月 Tab 右侧「选择清单」侧栏（fragment_monthly_schedule 镜像结构）+ bg_arrow 收起钮
- 书右页日记顶部：页面日期完成条目橙卡片
- +FAB=新建清单弹窗（PlanAddBottomDialog：名称+6色选择+取消/完成）
- 清单侧栏尾部直接输入新增条目（addListPageItem，只入清单不排期）
- 交互反馈：原版点击音效(ps_click_music.wav)+50ms震动、条目/清单卡左滑操作层、
  勾选橙底白勾+完成日期戳、Tab方向滑动切换、双击返回退出 Toast、Tab拖拽排序持久化

### 深色模式核查（本轮通过）
周右栏/月侧栏/完成卡片/滑动操作层/编辑弹层/新建清单弹窗均正常。

### 待办差距（下轮）
1. 书页日期模型：原版实机书左页=昨天(5|周六)、右页=今天(6|周日)的天级模型
   （顶部月份按当天显示「9月」），重写版=左周日程+右周一日记（spreadMonday）。
   原版书页还配 rv_schedule GridView 行高≈282px(75dp)。改模型有回归风险，需整轮专门做。
2. 原版书主 ViewPager 证实为 androidx.viewpager.widget.ViewPager（viewPager+vp2 双层）。
3. 原版周右栏列表尾部有行内新增（已对齐）。
4. lottie goalday.json 仅用于 GuideActivity/CouponActivity，非完成庆祝——不接入。
5. 原版月 Tab 顶栏切换后偶现不刷新（原版自身行为，不跟）。
