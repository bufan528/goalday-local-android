package com.bf410.goaldaylocal.data

import com.bf410.goaldaylocal.GoaldayApplication
import com.bf410.goaldaylocal.ui.widget.WidgetRefresh
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleRepository private constructor(
    private val store: LocalStateStore,
) {
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision

    /**
     * 排期读写的唯一闸门。
     *
     * 之前 [entries]/[saveEntries] 各自独立，"读全表→改→写全表"在两次调用之间可被别的写入穿插
     * （通知广播的顺延、组件刷新、UI 勾选都会写），后写者拿旧快照覆盖整表 = 丢条目。
     * 现在：单条 [entries]/[saveEntries] 各自加锁（防撕裂读），跨读写的组合一律走 [updateEntries]。
     */
    private val lock = Any()

    /** 组件刷新去抖：一次连续改期/导入只刷一轮，不再每次写入都做 4 次 Binder IPC + 4 次全表解析 */
    private val widgetRefresh = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
    )

    // 单个常驻收集器做尾沿去抖：连续写入合并成最后一次刷新
    private val refreshWorker = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default,
    ).launch {
        widgetRefresh.collect {
            delay(300)
            GoaldayApplication.appContext?.let { ctx ->
                withContext(Dispatchers.IO) { WidgetRefresh.refreshScheduleWidgets(ctx) }
            }
        }
    }

    fun entries(): List<ScheduleEntry> = synchronized(lock) { store.scheduleEntries() }

    fun saveEntries(entries: List<ScheduleEntry>) {
        if (applyIfChanged { entries }) {
            notifyChanged()
            scheduleWidgetRefresh()
        }
    }

    /**
     * 原子读-改-写：transform 在锁内拿到最新全表，返回值整表落盘。
     * 所有"删/改/移动/完成"的读改写都必须走这里（直接 entries()+saveEntries() 组合仍有竞态窗口）。
     *
     * 返回 false 表示内容没变：不写盘、不发通知。这一条是必须的——
     * 写者若无条件 bump revision，而订阅方（如 BookViewModel 的 revision.collect）
     * 回写数据，就会变成"写入→通知→回写→通知"的自激循环，直接 ANR。
     */
    fun updateEntries(transform: (List<ScheduleEntry>) -> List<ScheduleEntry>): Boolean {
        if (applyIfChanged(transform)) {
            notifyChanged()
            scheduleWidgetRefresh()
            return true
        }
        return false
    }

    /** 锁内比较：只有真的变了才落盘。相等判定用内容而非引用。 */
    private fun applyIfChanged(transform: (List<ScheduleEntry>) -> List<ScheduleEntry>): Boolean =
        synchronized(lock) {
            val current = store.scheduleEntries()
            val next = transform(current)
            if (next == current) return@synchronized false
            store.saveScheduleEntries(next)
            true
        }

    /** transform 只需要读、不改（查重/统计）时走这里，至少保证读到的是一致快照 */
    fun <T> withEntries(read: (List<ScheduleEntry>) -> T): T = synchronized(lock) { read(store.scheduleEntries()) }

    fun addEntry(
        title: String,
        year: Int,
        month: Int,
        day: Int,
        note: String = "",
        timeText: String = "",
        repeatRule: String = "",
        repeatInterval: Int = 1,
        repeatEndDate: String = "",
        repeatGroupId: String = "",
        status: ScheduleStatus = ScheduleStatus.PLANNED,
        colorArgb: Int? = null,
    ): ScheduleEntry {
        val safeDate = safeScheduleDate(year, month, day)
        val entry = ScheduleEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            year = safeDate.year,
            month = safeDate.month,
            day = safeDate.day,
            note = note,
            timeText = timeText,
            repeatRule = repeatRule,
            repeatInterval = repeatInterval.coerceAtLeast(1),
            repeatEndDate = repeatEndDate,
            repeatGroupId = repeatGroupId,
            completed = status == ScheduleStatus.DONE,
            colorArgb = colorArgb,
        )
        updateEntries { all -> all + entry }
        return entry
    }

    /** 组件刷新排到后台并去抖 300ms：主线程不再为每次写入付 4 次 Binder IPC 的钱 */
    private fun scheduleWidgetRefresh() {
        widgetRefresh.tryEmit(Unit)
    }

    private fun notifyChanged() {
        // 不能用 _revision.value += 1：StateFlow 会把相同值当没变（去重），非原子自增在并发下
        // 还可能算出同一个值，导致这次写入对所有订阅方完全不可见。update{} 保证单调递增且不丢事件。
        _revision.update { it + 1 }
    }

    companion object {
        @Volatile
        private var instance: ScheduleRepository? = null

        fun getInstance(store: LocalStateStore): ScheduleRepository =
            instance ?: synchronized(this) {
                instance ?: ScheduleRepository(store).also { instance = it }
            }
    }
}
