package com.example.timetrace.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

suspend fun GlanceAppWidget.updateAll(context: Context) {
    val componentName = ComponentName(context, this::class.java)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val glanceAppWidgetManager = GlanceAppWidgetManager(context)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
    appWidgetIds.forEach { glanceId ->
        glanceAppWidgetManager.getGlanceIdBy(glanceId)?.let { 
            update(context, it)
        }
    }
}

fun updateAllWidgets(context: Context, scope: CoroutineScope) {
    scope.launch {
        GlanceScheduleWidget().updateAll(context)
    }
}
