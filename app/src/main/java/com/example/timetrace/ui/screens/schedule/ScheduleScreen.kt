package com.example.timetrace.ui.screens.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(navController: NavController, viewModel: ScheduleViewModel = hiltViewModel()) {
    val schedulesByDate by viewModel.schedulesByDate.collectAsState()
    val completedSchedules by viewModel.completedSchedules.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的日程") })
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("添加日程") },
                        onClick = { 
                            navController.navigate("add_schedule_detail")
                            showMenu = false 
                        },
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("添加生日") },
                        onClick = { 
                            navController.navigate("add_birthday") 
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("常规事项") },
                        onClick = { 
                            navController.navigate("add_routine")
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (schedulesByDate.isEmpty() && completedSchedules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无日程，开启美好一天吧！", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                schedulesByDate.forEach { (date, schedules) ->
                    stickyHeader {
                        DateHeader(date = date)
                    }
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            onToggleCompletion = { viewModel.toggleScheduleCompletion(schedule) },
                            onDelete = { viewModel.deleteSchedule(schedule) }
                        )
                    }
                }

                if (completedSchedules.isNotEmpty()) {
                    stickyHeader {
                        CompletedHeader()
                    }
                    items(completedSchedules, key = { it.id }) { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            onToggleCompletion = { viewModel.toggleScheduleCompletion(schedule) },
                            onDelete = { viewModel.deleteSchedule(schedule) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = date.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CompletedHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "已完成",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(getPriorityColor(schedule.priority, schedule.isCompleted))
            )
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = schedule.isCompleted,
                    onCheckedChange = { onToggleCompletion() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = schedule.title, style = MaterialTheme.typography.titleMedium)
                    val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
                    Text(
                        text = Instant.ofEpochMilli(schedule.timestamp).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormat),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除日程", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun getPriorityColor(priority: Int, isCompleted: Boolean): Color {
    if (isCompleted) return Color.Gray.copy(alpha = 0.4f)
    return when (priority) {
        1 -> Color.Green.copy(alpha = 0.6f)
        2 -> Color.Yellow.copy(alpha = 0.6f)
        3 -> Color.Red.copy(alpha = 0.6f)
        else -> Color.Gray.copy(alpha = 0.4f)
    }
}
