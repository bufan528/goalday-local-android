# Goalday Local

本地离线的目标管理与手账应用：周计划、日记、清单、年历手账本，所有数据只保存在本机，无账号、无云同步。

## 功能

- **周计划**：7 天纵览，行内直接新增/改名，拖拽排期，重复事项（每天/每周/每月/每年）自动展开
- **月视图**：整月日程总览，清单侧栏快速排入日期
- **记录**：每日一问 + 自由书写，支持插入图片，完成事项自动生成卡片
- **清单**：多清单管理，勾选/置顶/改期，详情页支持显示选项与页内排序
- **手账本**：年历书翻页阅读（封面开场 + 3D 翻页），书内可直接写日记、改计划，按年切换
- **导出**：日程/日记导出长图、PDF 打印，支持保存与分享
- **小组件**：桌面今日组件， glance 今日安排
- **外观**：浅色/深色/跟随系统，多种字号
- **备份**：本地备份快照，支持恢复与删除

## 技术栈

- Kotlin + Jetpack Compose（BOM 2024.10.01）
- 本地存储：MMKV（偏好与清单状态）+ JSON（日程）+ 私有目录（日记图片）
- 最低 Android 8.0（API 26），目标 Android 15（API 35）

## 构建

```powershell
cd goalday-local
.\gradlew.bat assembleDebug --console=plain --offline
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

包名 `com.bf410.goaldaylocal`，当前版本 0.1.0（versionCode 1）。

## 项目结构

```
app/src/main/java/com/bf410/goaldaylocal/
├── data/        本地数据层（状态存储、日程仓库、示例种子、备份）
├── ui/main/     主界面（周/月/记录/清单）
├── ui/book/     手账本（翻页、书页预览、导出）
├── ui/calendar/ 日历（月历、重复展开、日程编辑）
├── ui/home/     首页
├── ui/settings/ 设置
├── ui/widget/   桌面小组件
└── MainActivity.kt  单 Activity 入口，全屏沉浸
```
