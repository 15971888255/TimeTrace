package com.example.timetrace.ui.screens.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Inbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import com.example.timetrace.ui.widget.updateAllWidgets
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的日程", style = MaterialTheme.typography.headlineMedium) })
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
            EmptyScheduleContent()
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
                            onClick = { navController.navigate("schedule_detail/${schedule.id}") },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteSchedule(schedule)
                                    updateAllWidgets(context)
                                }
                            }
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
                            onClick = { navController.navigate("schedule_detail/${schedule.id}") },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteSchedule(schedule)
                                    updateAllWidgets(context)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = "空日程",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无日程，开启美好一天吧！",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
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
            style = MaterialTheme.typography.titleSmall,
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
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = schedule.isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title, 
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (schedule.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (schedule.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
                Text(
                    text = Instant.ofEpochMilli(schedule.timestamp).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormat),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (schedule.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
