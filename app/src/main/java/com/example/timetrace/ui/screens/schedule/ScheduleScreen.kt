package com.example.timetrace.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timetrace.R
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val schedules by viewModel.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val (ongoing, completed) = schedules.partition { !it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的日程") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_add), contentDescription = "添加日程")
            }
        }
    ) { paddingValues ->
        if (schedules.isEmpty()) {
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
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        text = "进行中",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                items(ongoing, key = { it.id }) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onToggleCompletion = { viewModel.toggleScheduleCompletion(schedule) },
                        onDelete = { viewModel.deleteSchedule(schedule) }
                    )
                }

                item {
                    Text(
                        text = "已完成",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                items(completed, key = { it.id }) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onToggleCompletion = { viewModel.toggleScheduleCompletion(schedule) },
                        onDelete = { viewModel.deleteSchedule(schedule) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddScheduleDialog(
            onDismiss = { showDialog = false },
            onConfirm = { title, timestamp, priority, isLunar, isBirthday ->
                viewModel.addSchedule(title, timestamp, priority, isLunar, isBirthday)
                showDialog = false
            }
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
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(getPriorityColor(schedule.priority))
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
                    Text(
                        text = SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(Date(schedule.timestamp)),
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
fun getPriorityColor(priority: Int): Color {
    return when (priority) {
        1 -> Color.Green.copy(alpha = 0.6f)
        2 -> Color.Yellow.copy(alpha = 0.6f)
        3 -> Color.Red.copy(alpha = 0.6f)
        else -> Color.Gray.copy(alpha = 0.4f)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(onDismiss: () -> Unit, onConfirm: (String, Long, Int, Boolean, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isLunar by remember { mutableStateOf(false) }
    var isBirthday by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState(initialHour = calendar.get(Calendar.HOUR_OF_DAY), initialMinute = calendar.get(Calendar.MINUTE))

    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Text(text = "添加新日程", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(12.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(8.dp))

                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("农历")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isLunar, onCheckedChange = { isLunar = it })
                    Spacer(modifier = Modifier.weight(1f))
                    Text("设为生日")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isBirthday, onCheckedChange = { isBirthday = it })
                }

                if (selectedTimestamp != null) {
                    Text(
                        text = "已选: ${SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(Date(selectedTimestamp!!))}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                     Text(
                        text = "请选择时间",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row {
                    Button(onClick = { showDatePicker = true }) {
                        Text("选择日期")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showTimePicker = true }) {
                        Text("选择时间")
                    }
                }
                Spacer(modifier = Modifier.padding(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                         val finalTimestamp = selectedTimestamp ?: System.currentTimeMillis()
                        onConfirm(title, finalTimestamp, 1, isLunar, isBirthday)
                    }) {
                        Text("添加")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = Calendar.getInstance().apply {
                        timeInMillis = datePickerState.selectedDateMillis!!
                    }
                    val current = Calendar.getInstance().apply {
                        timeInMillis = selectedTimestamp ?: System.currentTimeMillis()
                    }
                    current.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
                    selectedTimestamp = current.timeInMillis
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            ElevatedCard {
                 Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val current = Calendar.getInstance().apply {
                                timeInMillis = selectedTimestamp ?: System.currentTimeMillis()
                            }
                            current.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            current.set(Calendar.MINUTE, timePickerState.minute)
                            selectedTimestamp = current.timeInMillis
                            showTimePicker = false
                        }) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}
