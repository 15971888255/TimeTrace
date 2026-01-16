package com.example.timetrace.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.GlanceTheme
import com.example.timetrace.R
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.di.WidgetRepoEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        GlanceScheduleWidget().update(context, glanceId)
    }
}

class GlanceScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetRepoEntryPoint::class.java
        )
        val repository = hiltEntryPoint.repository()
        val allSchedules = repository.getAllSchedules().first()
        val upcomingSchedules = allSchedules.filter { !it.isCompleted && it.timestamp >= System.currentTimeMillis() }
            .sortedBy { it.timestamp }

        provideContent {
            GlanceTheme {
                Scaffold(
                    backgroundColor = GlanceTheme.colors.surfaceVariant,
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_launcher_foreground),
                            title = "未来日程",
                            actions = {
                                androidx.glance.Image(
                                    provider = ImageProvider(android.R.drawable.stat_notify_sync),
                                    contentDescription = "刷新",
                                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshActionCallback>()),
                                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                                )
                            }
                        )
                    },
                    content = { WidgetContent(schedules = upcomingSchedules) }
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(schedules: List<Schedule>) {
        if (schedules.isEmpty()) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无未来日程", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 16.sp))
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp)) {
                val (today, tomorrow, future) = groupSchedules(schedules)

                if (today.isNotEmpty()) {
                    item { GroupHeader("今天") }
                    items(today) { schedule -> ScheduleWidgetItem(schedule) }
                }

                if (tomorrow.isNotEmpty()) {
                    item { GroupHeader("明天") }
                    items(tomorrow) { schedule -> ScheduleWidgetItem(schedule) }
                }

                if (future.isNotEmpty()) {
                    item { GroupHeader("将来") }
                    items(future) { schedule -> ScheduleWidgetItem(schedule) }
                }
            }
        }
    }

    private fun groupSchedules(schedules: List<Schedule>): Triple<List<Schedule>, List<Schedule>, List<Schedule>> {
        val today = mutableListOf<Schedule>()
        val tomorrow = mutableListOf<Schedule>()
        val future = mutableListOf<Schedule>()

        val now = Calendar.getInstance()
        val todayEnd = (now.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }
        val tomorrowEnd = (todayEnd.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

        schedules.forEach {
            when {
                it.timestamp <= todayEnd.timeInMillis -> today.add(it)
                it.timestamp <= tomorrowEnd.timeInMillis -> tomorrow.add(it)
                else -> future.add(it)
            }
        }
        return Triple(today, tomorrow, future)
    }

    @Composable
    private fun GroupHeader(title: String) {
        Text(
            text = title,
            modifier = GlanceModifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        )
    }

    @Composable
    private fun ScheduleWidgetItem(schedule: Schedule) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (schedule.isFromRoutine) {
                androidx.glance.Image(
                    provider = ImageProvider(android.R.drawable.stat_notify_sync),
                    contentDescription = "周期性日程",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.padding(end = 8.dp)
                )
            }
            Text(
                text = schedule.title,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = timeFormat.format(Date(schedule.timestamp)),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp)
            )
        }
    }
}
