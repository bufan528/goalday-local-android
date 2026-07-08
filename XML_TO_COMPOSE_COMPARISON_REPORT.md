# XML布局与Compose实现对比报告

## 概述

本报告对比了逆向工程的Android XML布局文件与当前Jetpack Compose实现的差异，重点关注尺寸、颜色、间距、字体大小等视觉属性。

**单位转换说明：**
- 1pt ≈ 1.333dp (Android标准转换)
- 1dip = 1dp
- 1sp = 1dp (用于字体大小)

---

## 1. 日程页面 (Schedule)

### 1.1 fragment_schedule_inbook.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 左上角日期容器 marginTop | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 左上角日期容器 marginStart | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 左上角日期文字1 textSize | 11.0sp | 11sp | - | ⚠️ 未实现 |
| 左上角日期文字2 textSize | 9.0sp | 9sp | - | ⚠️ 未实现 |
| 右上角容器 marginTop | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 右上角容器 marginEnd | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 右上角文字1 textSize | 12.0sp | 12sp | - | ⚠️ 未实现 |
| 右上角文字2 textSize | 10.0sp | 10sp | - | ⚠️ 未实现 |
| 中间分割线宽度 | 24.5pt | 32.7dp | - | ⚠️ 未实现 |
| RecyclerView marginBottom | 30.0dip | 30dp | 30.dp | ✅ 匹配 |

**颜色值：**
- textColor="@2131099742" - 需要解析资源ID

### 1.2 item_schedule_item_in_book.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 日期列宽度 | 24.5dip | 24.5dp | 24.5.dp | ✅ 匹配 |
| 日期列高度 | 37.5dip | 37.5dp | - | ⚠️ 未使用 |
| 日期文字1 textSize | 9.0dip | 9sp | 9.sp | ✅ 匹配 |
| 日期文字1 marginBottom | 2.0dip | 2dp | 2.dp | ✅ 匹配 |
| 日期文字1 textFontWeight | 400 | Normal | Normal | ✅ 匹配 |
| 日期文字2 textSize | 9.0dip | 9sp | 9.sp | ✅ 匹配 |
| 日期文字3 textSize | 6.0dip | 6sp | 6.sp | ✅ 匹配 |
| 日期文字3 marginTop | 2.0dip | 2dp | 2.dp | ✅ 匹配 |
| 目标列 paddingVertical | 3.5pt | 4.7dp | 3.5.dp | ❌ 不匹配 |
| 目标行高度 | 12.0pt | 16dp | 16.dp | ✅ 匹配 |
| 勾选框宽度 | 9.0dip | 9dp | 9.dp | ✅ 匹配 |
| 勾选框高度 | 9.0dip | 9dp | 9.dp | ✅ 匹配 |
| 勾选框 marginTop | 2.0dip | 2dp | 2.dp | ✅ 匹配 |
| 勾选框 marginStart | 2.0dip | 2dp | 2.dp | ✅ 匹配 |

**关键差异：**
- **paddingVertical**: XML为3.5pt(≈4.7dp)，Compose使用3.5dp，差异约1.2dp

### 1.3 fragment_monthly_schedule.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| RecyclerView宽度 | 0.0dip | match_parent | - | ⚠️ 未实现整月视图 |
| 分割线宽度 | 1.0dip | 1dp | - | ⚠️ 未实现 |
| 分割线颜色 | #e0e0e0 | - | - | ⚠️ 未实现 |
| 右侧目标容器宽度 | 177.0pt | 236dp | - | ⚠️ 未实现 |
| 右侧目标容器背景 | @2131099729 | - | - | ⚠️ 未实现 |
| 标题容器 marginTop | 5.0dip | 5dp | - | ⚠️ 未实现 |
| 标题容器 marginStart/End | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 标题容器 paddingHorizontal | 8.0dip | 8dp | - | ⚠️ 未实现 |
| 标题容器 paddingVertical | 3.0dip | 3dp | - | ⚠️ 未实现 |
| 圆点宽度 | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 圆点高度 | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 标题文字 textSize | 20.0dip | 20sp | - | ⚠️ 未实现 |
| 标题文字 textFontWeight | 600 | SemiBold | - | ⚠️ 未实现 |
| 目标列表背景 | @2131100256 | - | - | ⚠️ 未实现 |
| 添加按钮宽度 | 43.0dip | 43dp | - | ⚠️ 未实现 |
| 添加按钮高度 | 43.0dip | 43dp | - | ⚠️ 未实现 |
| 添加按钮 margin | 33.0dip | 33dp | - | ⚠️ 未实现 |

---

## 2. 日记页面 (Diary)

### 2.1 fragment_diary_inbook.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 左上角日期容器 marginTop | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 左上角日期容器 marginStart | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 左上角日期文字1 textSize | 11.0sp | 11sp | - | ⚠️ 未实现 |
| 左上角日期文字2 textSize | 9.0sp | 9sp | - | ⚠️ 未实现 |
| 右上角容器 marginTop | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 右上角容器 marginEnd | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 右上角文字1 textSize | 12.0sp | 12sp | - | ⚠️ 未实现 |
| 右上角文字2 textSize | 10.0sp | 10sp | - | ⚠️ 未实现 |
| 日期标签容器高度 | 24.0dip | 24dp | 24.dp | ✅ 匹配 |
| 日期标签容器 paddingStart | 7.5pt | 10dp | 10.dp | ✅ 匹配 |
| 日期标签容器 paddingEnd | 7.5pt | 10dp | 10.dp | ✅ 匹配 |
| 日期文字 textSize | 12.0dip | 12sp | 12.sp | ✅ 匹配 |
| 日期文字 textColor | @2131099692 | - | adaptiveInkPrimary | ⚠️ 需验证 |
| 日期文字 textFontWeight | 500 | Medium | Medium | ✅ 匹配 |
| RecyclerView marginTop | 5.0dip | 5dp | 5.dp | ✅ 匹配 |
| RecyclerView marginBottom | 30.0dip | 30dp | 30.dp | ✅ 匹配 |
| RecyclerView marginStart | 7.5pt | 10dp | 10.dp | ✅ 匹配 |
| RecyclerView marginEnd | 7.5pt | 10dp | 10.dp | ✅ 匹配 |
| 底部图片栏高度 | 23.0pt | 30.7dp | 30.7.dp | ✅ 匹配 |
| 底部图片栏背景 | @2131100579 | - | adaptiveSurface | ⚠️ 需验证 |
| 图片选择按钮宽度 | 23.0pt | 30.7dp | 30.7.dp | ✅ 匹配 |
| 图片选择按钮高度 | 23.0pt | 30.7dp | 30.7.dp | ✅ 匹配 |
| 图片选择按钮 marginStart | 3.75pt | 5dp | 5.dp | ✅ 匹配 |
| 图片图标宽度 | 12.5pt | 16.7dp | 16.7.dp | ✅ 匹配 |
| 图片图标高度 | 12.5pt | 16.7dp | 16.7.dp | ✅ 匹配 |
| 图片图标 marginStart | 3.75pt | 5dp | - | ❌ 不匹配 |

**关键差异：**
- **图片图标marginStart**: XML为3.75pt(≈5dp)，Compose未设置

### 2.2 item_diary_text.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 文字 textSize | 16.0dip | 16sp | 16.sp | ✅ 匹配 |
| 文字 textColor | #ff2c2c2c | #2C2C2C | adaptiveInkPrimary | ⚠️ 需验证 |
| 文字 lineSpacingExtra | 2.0dip | 2dp | - | ❌ 不匹配 |
| 文字 ellipsize | 3 (end) | - | - | ⚠️ 未实现 |

**关键差异：**
- **lineSpacingExtra**: XML为2dp，Compose使用lineHeight=20.sp(隐含行距)

### 2.3 item_diary_target_in_book.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器背景 | @2131230866 | - | DiaryTargetBackground | ⚠️ 需验证 |
| 容器 paddingBottom | 4.5pt | 6dp | 6.dp | ✅ 匹配 |
| 容器 marginBottom | 5.0dip | 5dp | - | ❌ 不匹配 |
| 标题文字 textSize | 9.0dip | 9sp | 9.sp | ✅ 匹配 |
| 标题文字 textColor | #503311 | #503311 | Color(0xFF503311) | ✅ 匹配 |
| 标题文字 marginTop | 4.5pt | 6dp | 6.dp | ✅ 匹配 |
| 标题文字 marginBottom | 1.5pt | 2dp | 2.dp | ✅ 匹配 |
| 标题文字 marginStart | 8.0pt | 10.7dp | 10.7.dp | ✅ 匹配 |
| 标题文字 textFontWeight | 600 | SemiBold | SemiBold | ✅ 匹配 |
| 时间文字 textColor | #cbcbcb | - | - | ⚠️ 未实现 |

**关键差异：**
- **marginBottom**: XML为5dp，Compose未设置

### 2.4 item_diary_topic_target_inbook.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器背景 | @2131230867 | - | adaptiveInkSecondary | ⚠️ 需验证 |
| 容器 marginBottom | 5.0dip | 5dp | - | ❌ 不匹配 |
| 容器 paddingStart | 5.0pt | 6.7dp | 6.7.dp | ✅ 匹配 |
| 容器 paddingEnd | 5.0pt | 6.7dp | 6.7.dp | ✅ 匹配 |
| 标题文字 textSize | 8.0dip | 8sp | 8.sp | ✅ 匹配 |
| 标题文字 textStyle | 0x00000001 (bold) | Bold | SemiBold | ⚠️ 近似 |
| 标题文字 textColor | #ffffffff | White | adaptivePaper | ⚠️ 需验证 |
| 标题文字 marginTop | 5.0pt | 6.7dp | 6.7.dp | ✅ 匹配 |
| 标题文字 marginBottom | 3.5pt | 4.7dp | 4.7.dp | ✅ 匹配 |
| 标题文字 textFontWeight | 600 | SemiBold | SemiBold | ✅ 匹配 |
| 副标题文字 textSize | 9.0dip | 9sp | 9.sp | ✅ 匹配 |
| 副标题文字 textColor | #9cffffff | 61% White | adaptivePaper.copy(alpha=0.61f) | ✅ 匹配 |
| 副标题文字 marginBottom | 3.5pt | 4.7dp | 4.7.dp | ✅ 匹配 |
| 副标题文字 textFontWeight | 600 | SemiBold | SemiBold | ✅ 匹配 |
| 副标题文字 ellipsize | 3 (end) | - | - | ⚠️ 未实现 |
| 副标题文字 singleLine | true | maxLines=1 | maxLines=1 | ✅ 匹配 |

**关键差异：**
- **marginBottom**: XML为5dp，Compose未设置

### 2.5 item_diary_target_child_inbook.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器高度 | 12.0dip | 12dp | 12.dp | ✅ 匹配 |
| 容器 paddingStart | 5.0pt | 6.7dp | 6.7.dp | ✅ 匹配 |
| 圆点背景 | @2131230897 | - | DiaryTargetChildDot | ⚠️ 需验证 |
| 圆点宽度 | 2.5pt | 3.3dp | 3.3.dp | ✅ 匹配 |
| 圆点高度 | 2.5pt | 3.3dp | 3.3.dp | ✅ 匹配 |
| 圆点 marginStart | 4.0dip | 4dp | - | ❌ 不匹配 |
| 圆点 marginEnd | 4.0dip | 4dp | - | ❌ 不匹配 |

**关键差异：**
- **圆点marginStart/End**: XML为4dp，Compose未设置（文字padding start=4dp可能已包含）

---

## 3. 目标页面 (Target)

### 3.1 item_target_detail.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 滑动布局 dragEdge | right | - | - | ⚠️ 未实现 |
| 滑动布局 mode | same_level | - | - | ⚠️ 未实现 |
| 删除按钮背景 | #ed8888 | - | - | ⚠️ 未实现 |
| 删除按钮宽度 | 50.0pt | 66.7dp | - | ⚠️ 未实现 |
| 勾选框容器 paddingTop | 21.0dip | 21dp | - | ⚠️ 未实现 |
| 勾选框容器 paddingBottom | 20.0dip | 20dp | - | ⚠️ 未实现 |
| 勾选框容器 paddingStart | 27.0dip | 27dp | - | ⚠️ 未实现 |
| 勾选框容器 paddingEnd | 27.0dip | 27dp | - | ⚠️ 未实现 |
| 勾选框背景 | @2131230962 | - | - | ⚠️ 未实现 |
| 勾选框图标 | @2131230964 | - | - | ⚠️ 未实现 |
| 日期文字 textSize | 20.0dip | 20sp | - | ⚠️ 未实现 |
| 日期文字 marginStart | 56.0dip | 56dp | - | ⚠️ 未实现 |
| 内容输入框 paddingBottom | 12.0dip | 12dp | - | ⚠️ 未实现 |
| 内容输入框 marginStart | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 内容输入框 marginEnd | 27.0dip | 27dp | - | ⚠️ 未实现 |
| 完成标签 textColor | @2131100579 | - | - | ⚠️ 未实现 |
| 完成标签背景 | @2131230894 | - | - | ⚠️ 未实现 |
| 完成标签 marginBottom | 13.0dip | 13dp | - | ⚠️ 未实现 |
| 完成标签 paddingStart | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 完成标签 paddingEnd | 10.0dip | 10dp | - | ⚠️ 未实现 |
| 底部分割线背景 | @2131230898 | - | - | ⚠️ 未实现 |
| 底部分割线高度 | 4.0dip | 4dp | - | ⚠️ 未实现 |
| 底部分割线 translationY | 3.0dip | 3dp | - | ⚠️ 未实现 |

**关键差异：**
- 整个滑动删除交互未实现
- 底部操作栏未实现

### 3.2 activity_target_detail.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器背景 | @2131099729 | - | adaptivePaperGradient | ⚠️ 需验证 |
| 容器 fitsSystemWindows | true | - | - | ⚠️ 未实现 |
| 底部操作栏背景 | @2131100579 | - | adaptivePaper | ⚠️ 需验证 |
| 底部操作栏高度 | 46.0dip | 46dp | 46.dp | ✅ 匹配 |
| 日期标签 textSize | 20.0dip | 20sp | 20.sp | ✅ 匹配 |
| 日期标签 textColor | @2131099692 | - | adaptiveInkPrimary | ⚠️ 需验证 |
| 日期标签背景 | @2131230894 | - | adaptiveSurface | ⚠️ 需验证 |
| 日期标签 paddingStart | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 日期标签 paddingEnd | 10.0dip | 10dp | 10.dp | ✅ 匹配 |
| 日期标签 backgroundTint | #cff6f6f6 | - | - | ⚠️ 未实现 |
| 分隔线背景 | #d3cdc6 | - | adaptiveDivider | ⚠️ 需验证 |
| 分隔线宽度 | 1.0dip | 1dp | 1.dp | ✅ 匹配 |
| 分隔线高度 | 22.0dip | 22dp | 22.dp | ✅ 匹配 |
| 完成图标 padding | 13.0dip | 13dp | 13.dp | ✅ 匹配 |
| 置顶图标 padding | 13.0dip | 13dp | 13.dp | ✅ 匹配 |
| 删除图标 padding | 13.0dip | 13dp | 13.dp | ✅ 匹配 |
| 删除图标 marginEnd | 13.0dip | 13dp | - | ❌ 不匹配 |

**关键差异：**
- **删除图标marginEnd**: XML为13dp，Compose未设置

### 3.3 item_target_add.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器 paddingTop | 7.0dip | 7dp | - | ⚠️ 未实现 |
| 容器 paddingBottom | 7.0dip | 7dp | - | ⚠️ 未实现 |
| 圆点背景 | @2131230897 | - | - | ⚠️ 未实现 |
| 圆点宽度 | 5.0dip | 5dp | - | ⚠️ 未实现 |
| 圆点高度 | 5.0dip | 5dp | - | ⚠️ 未实现 |
| 圆点 marginStart | 17.0dip | 17dp | - | ⚠️ 未实现 |
| 输入框 textSize | 20.0dip | 20sp | - | ⚠️ 未实现 |
| 输入框 textColor | @2131099692 | - | - | ⚠️ 未实现 |
| 输入框 marginStart | 6.0dip | 6dp | - | ⚠️ 未实现 |
| 输入框 ellipsize | 3 (end) | - | - | ⚠️ 未实现 |
| 输入框 maxLines | 1 | - | - | ⚠️ 未实现 |

**关键差异：**
- 整个添加行布局未实现

### 3.4 item_schedule_target.xml

| XML属性 | XML值 | 转换为dp | Compose值 | 匹配状态 |
|---------|-------|---------|-----------|---------|
| 容器 paddingTop | 7.0dip | 7dp | - | ⚠️ 未实现 |
| 容器 paddingBottom | 7.0dip | 7dp | - | ⚠️ 未实现 |
| 圆点背景 | @2131230897 | - | - | ⚠️ 未实现 |
| 圆点宽度 | 5.0dip | 5dp | - | ⚠️ 未实现 |
| 圆点高度 | 5.0dip | 5dp | - | ⚠️ 未实现 |
| 圆点 marginStart | 17.0dip | 17dp | - | ⚠️ 未实现 |
| 输入框 textSize | 20.0dip | 20sp | - | ⚠️ 未实现 |
| 输入框 textColor | @2131099692 | - | - | ⚠️ 未实现 |
| 输入框 marginStart | 6.0dip | 6dp | - | ⚠️ 未实现 |
| 输入框 ellipsize | 3 (end) | - | - | ⚠️ 未实现 |
| 输入框 maxLines | 1 | - | - | ⚠️ 未实现 |

**关键差异：**
- 整个日程目标布局未实现

---

## 4. 颜色值对比

### 4.1 硬编码颜色

| XML颜色值 | 用途 | Compose对应 | 匹配状态 |
|-----------|------|-------------|---------|
| #ff2c2c2c | 日记文字颜色 | adaptiveInkPrimary | ⚠️ 需验证 |
| #503311 | 日记目标标题 | Color(0xFF503311) | ✅ 匹配 |
| #cbcbcb | 日记时间文字 | - | ⚠️ 未实现 |
| #ffffffff | 专题目标标题 | adaptivePaper | ⚠️ 需验证 |
| #9cffffff | 专题目标副标题 | adaptivePaper.copy(alpha=0.61f) | ✅ 匹配 |
| #ed8888 | 目标删除按钮背景 | - | ⚠️ 未实现 |
| #e0e0e0 | 月视图分割线 | - | ⚠️ 未实现 |
| #d3cdc6 | 目标底部分隔线 | adaptiveDivider | ⚠️ 需验证 |
| #9e9e9e | 目标添加文字 | - | ⚠️ 未实现 |

### 4.2 资源引用颜色

以下颜色引用了资源ID，需要进一步解析：
- @2131099742 - 日程日期文字颜色
- @2131099692 - 主要文字颜色
- @2131100579 - 底部栏背景
- @2131230866 - 日记目标背景
- @2131230867 - 专题目标背景
- @2131230897 - 圆点背景
- @2131099729 - 页面背景
- @2131230894 - 日期标签背景
- @2131230898 - 底部分割线背景

---

## 5. 缺失功能总结

### 5.1 日程页面缺失功能

1. **整月视图** (fragment_monthly_schedule.xml)
   - 完整的月历网格布局
   - 左侧日程列表 + 右侧目标容器
   - 分割线和添加按钮

2. **日期标记容器**
   - 左上角双行日期显示（11sp + 9sp）
   - 右上角双行日期显示（12sp + 10sp）

3. **中间分割线**
   - 24.5pt宽度的垂直分割线

### 5.2 日记页面缺失功能

1. **日期标记容器**
   - 同日程页面的左上角和右上角日期显示

2. **时间文字显示**
   - item_diary_target_in_book.xml中的TimeTextView (#cbcbcb)

3. **文字省略**
   - ellipsize="end" 未实现

4. **行距**
   - lineSpacingExtra="2dp" 未精确实现

### 5.3 目标页面缺失功能

1. **滑动删除交互** (item_target_detail.xml)
   - SwipeRevealLayout 滑动显示删除按钮
   - 删除按钮背景 #ed8888
   - 勾选框容器和图标

2. **底部操作栏完整实现**
   - 日期标签点击交互
   - 背景色调 #cff6f6f6

3. **目标添加行** (item_target_add.xml)
   - 圆点 + 输入框布局
   - 20sp大字输入框

4. **日程目标行** (item_schedule_target.xml)
   - 类似目标添加行的布局

---

## 6. 尺寸不匹配汇总

### 6.1 需要修复的尺寸差异

| 文件 | 属性 | XML值 | 当前Compose值 | 差异 |
|------|------|-------|---------------|------|
| item_schedule_item_in_book.xml | paddingVertical | 3.5pt(4.7dp) | 3.5dp | +1.2dp |
| fragment_diary_inbook.xml | 图片图标marginStart | 3.75pt(5dp) | 未设置 | -5dp |
| item_diary_text.xml | lineSpacingExtra | 2.0dp | 未设置 | -2dp |
| item_diary_target_in_book.xml | marginBottom | 5.0dp | 未设置 | -5dp |
| item_diary_topic_target_inbook.xml | marginBottom | 5.0dp | 未设置 | -5dp |
| item_diary_target_child_inbook.xml | 圆点marginStart | 4.0dp | 未设置 | -4dp |
| item_diary_target_child_inbook.xml | 圆点marginEnd | 4.0dp | 未设置 | -4dp |
| activity_target_detail.xml | 删除图标marginEnd | 13.0dp | 未设置 | -13dp |

### 6.2 字体样式差异

| 文件 | 属性 | XML值 | 当前Compose值 | 差异 |
|------|------|-------|---------------|------|
| item_diary_topic_target_inbook.xml | textStyle | bold | SemiBold | 近似 |

---

## 7. 优先级修复建议

### 7.1 高优先级（视觉影响大）

1. **日记页面底部图片图标marginStart**
   - 修复：添加 `.padding(start = 5.dp)` 到图片图标

2. **日记目标块和专题目标块的marginBottom**
   - 修复：添加 `.padding(bottom = 5.dp)` 到容器

3. **目标页面删除图标marginEnd**
   - 修复：添加 `.padding(end = 13.dp)` 到删除图标

4. **日程目标列paddingVertical**
   - 修复：将 `3.5.dp` 改为 `4.7.dp`

### 7.2 中优先级（功能缺失）

1. **实现整月视图布局**
   - 参考 fragment_monthly_schedule.xml
   - 需要完整的月历网格和右侧目标容器

2. **实现日期标记容器**
   - 左上角和右上角的双行日期显示

3. **实现日记文字行距**
   - 添加 `lineHeight = (16 + 2).sp` 或使用 `lineSpacing`

### 7.3 低优先级（细节优化）

1. **实现滑动删除交互**
   - 需要第三方库或自定义手势处理

2. **解析资源引用颜色**
   - 需要反编译APK获取实际颜色值

3. **实现文字省略**
   - 添加 `overflow = TextOverflow.Ellipsis`

---

## 8. 验证清单

### 8.1 需要视觉验证的项目

- [ ] 日程页面日期列宽度和目标槽高度
- [ ] 日记页面底部图片栏高度和图标大小
- [ ] 日记目标块的背景颜色和圆角
- [ ] 专题目标块的深色背景和文字颜色
- [ ] 目标页面底部操作栏高度和图标间距
- [ ] 所有文字大小和字重是否正确
- [ ] 所有间距和边距是否符合预期

### 8.2 需要功能验证的项目

- [ ] 日程页面的目标勾选和删除线效果
- [ ] 日记页面的编辑和保存功能
- [ ] 目标页面的勾选状态持久化
- [ ] 目标页面的添加和删除功能
- [ ] 整月视图的日期选择和导航

---

## 9. 结论

当前Compose实现已经覆盖了大部分核心布局和尺寸，但仍存在以下主要差异：

1. **尺寸精度**：8处尺寸不匹配，主要是pt到dp的转换和margin遗漏
2. **功能缺失**：整月视图、滑动删除、日期标记容器等重要功能未实现
3. **颜色验证**：多个资源引用颜色需要解析验证
4. **细节优化**：行距、省略、字重等细节需要调整

建议按照优先级逐步修复这些差异，优先处理视觉影响大的尺寸问题，然后补充缺失功能，最后优化细节。
