package com.example.timetrace.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.di.WidgetRepoEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlanceScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetRepoEntryPoint::class.java
        )
        val repository = hiltEntryPoint.repository()
        // Fetch all schedules and filter completed ones in the UI layer
        val schedules = repository.getAllSchedules().first()

        provideContent {
            WidgetContent(schedules = schedules)
        }
    }

    @Composable
    private fun WidgetContent(schedules: List<Schedule>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFF5F5F5)))
        ) {
            // Title Bar
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("时迹", style = TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(SimpleDateFormat("M月d日", Locale.getDefault()).format(Date()))
            }

            if (schedules.none { !it.isCompleted }) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("今日暂无安排")
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(schedules.filter { !it.isCompleted }) { schedule ->
                        ScheduleWidgetItem(schedule = schedule)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleWidgetItem(schedule: Schedule) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color.White))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .height(24.dp) // Set a fixed height for the color bar
                .background(getPriorityColor(schedule.priority))
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = schedule.title,
            style = TextStyle(
                textDecoration = if (schedule.isCompleted) androidx.glance.text.TextDecoration.LineThrough else androidx.glance.text.TextDecoration.None,
                color = if (schedule.isCompleted) ColorProvider(Color.Gray) else ColorProvider(Color.Black)
            )
        )
    }
}

@Composable
private fun getPriorityColor(priority: Int): ColorProvider {
    return when (priority) {
        1 -> ColorProvider(Color.Green.copy(alpha = 0.6f))
        2 -> ColorProvider(Color.Yellow.copy(alpha = 0.6f))
        3 -> ColorProvider(Color.Red.copy(alpha = 0.6f))
        else -> ColorProvider(Color.Gray.copy(alpha = 0.4f))
    }
}
