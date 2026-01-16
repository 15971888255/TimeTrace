package com.example.timetrace.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.timetrace.R
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.di.WidgetRepoEntryPoint
import com.example.timetrace.ui.theme.DarkColorScheme
import com.example.timetrace.ui.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WidgetColorScheme {
    val colors = ColorProviders(LightColorScheme, DarkColorScheme)
}

object SelectedScheduleStateDefinition : GlanceStateDefinition<Preferences> {
    override suspend fun getDataStore(context: Context, fileKey: String) =
        PreferencesGlanceStateDefinition.getDataStore(context, fileKey)

    override fun getLocation(context: Context, fileKey: String) =
        PreferencesGlanceStateDefinition.getLocation(context, fileKey)
}

class ToggleScheduleDetailsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val scheduleId = parameters[scheduleIdKey] ?: return

        updateAppWidgetState(context, glanceId) { prefs ->
            val currentSelectedId = prefs[SELECTED_SCHEDULE_KEY]
            if (currentSelectedId == scheduleId) {
                prefs.remove(SELECTED_SCHEDULE_KEY)
            } else {
                prefs[SELECTED_SCHEDULE_KEY] = scheduleId
            }
        }
        GlanceScheduleWidget().update(context, glanceId)
    }
}

private val SELECTED_SCHEDULE_KEY = longPreferencesKey("selected_schedule_id")
private val scheduleIdKey = ActionParameters.Key<Long>("scheduleId")

class GlanceScheduleWidget : GlanceAppWidget() {

    override val stateDefinition = SelectedScheduleStateDefinition

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
            val prefs = currentState<Preferences>()
            val selectedId = prefs[SELECTED_SCHEDULE_KEY]

            GlanceTheme(colors = WidgetColorScheme.colors) {
                Scaffold(
                    backgroundColor = GlanceTheme.colors.surfaceVariant,
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.ic_launcher_foreground),
                            title = "日程",
                            actions = {
                                Image(
                                    provider = ImageProvider(android.R.drawable.stat_notify_sync),
                                    contentDescription = "刷新",
                                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshActionCallback>()),
                                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                                )
                            }
                        )
                    },
                    content = { WidgetContent(schedules = upcomingSchedules, selectedId = selectedId) }
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(schedules: List<Schedule>, selectedId: Long?) {
        if (schedules.isEmpty()) {
            EmptyScheduleContent()
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp)) {
                val groupedSchedules = groupSchedules(schedules)

                groupedSchedules.forEach { (header, scheduleList) ->
                    item { GroupHeader(header) }
                    items(scheduleList) { schedule -> ScheduleWidgetItem(schedule, selectedId == schedule.id) }
                }
            }
        }
    }
    
    @Composable
    private fun EmptyScheduleContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = "空日程",
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            Text("暂无未来日程", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 16.sp))
        }
    }

    private fun groupSchedules(schedules: List<Schedule>): Map<String, List<Schedule>> {
        val today = Calendar.getInstance()
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

        val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.getDefault())

        return schedules.groupBy { schedule ->
            val scheduleDate = Calendar.getInstance().apply { timeInMillis = schedule.timestamp }
            when {
                isSameDay(scheduleDate, today) -> "今天"
                isSameDay(scheduleDate, tomorrow) -> "明天"
                else -> dateFormat.format(scheduleDate.time)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    @Composable
    private fun GroupHeader(title: String) {
        Text(
            text = title,
            modifier = GlanceModifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurfaceVariant)
        )
    }

    @Composable
    private fun ScheduleWidgetItem(schedule: Schedule, isSelected: Boolean) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        Column(
            modifier = GlanceModifier.fillMaxWidth()
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<ToggleScheduleDetailsAction>(parameters = actionParametersOf(scheduleIdKey to schedule.id)))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (schedule.isFromRoutine) {
                    Image(
                        provider = ImageProvider(android.R.drawable.stat_notify_sync),
                        contentDescription = "周期性日程",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                        modifier = GlanceModifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = schedule.title,
                    style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = timeFormat.format(Date(schedule.timestamp)),
                    style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant)
                )
            }
            if (isSelected) {
                Column(modifier = GlanceModifier.background(GlanceTheme.colors.surfaceVariant).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp).fillMaxWidth()) {
                    Text(
                        text = schedule.notes ?: "没有备注",
                        style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
    }
}
class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        GlanceScheduleWidget().update(context, glanceId)
    }
}
