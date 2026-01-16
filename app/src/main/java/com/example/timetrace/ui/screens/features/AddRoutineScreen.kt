package com.example.timetrace.ui.screens.features

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.data.model.Routine
import com.example.timetrace.ui.viewmodel.RoutineViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val routines by viewModel.routines.collectAsState()
    var newRoutineTitle by remember { mutableStateOf("") }
    val selectedWeekdays = remember { mutableStateListOf<Int>() }

    val calendar = Calendar.getInstance()
    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY).toString()) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE).toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("周期事项管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newRoutineTitle,
                onValueChange = { newRoutineTitle = it },
                label = { Text("输入周期性事项，如“跑步”") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("选择重复日：", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            WeekdaySelector(selectedWeekdays = selectedWeekdays) { day ->
                if (selectedWeekdays.contains(day)) {
                    selectedWeekdays.remove(day)
                } else {
                    selectedWeekdays.add(day)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("设定时间：", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = hour, onValueChange = { hour = it }, label = { Text("时") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = minute, onValueChange = { minute = it }, label = { Text("分") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                if (newRoutineTitle.isBlank()) {
                    Toast.makeText(context, "标题不能为空", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selectedWeekdays.isEmpty()) {
                    Toast.makeText(context, "请至少选择一个重复日", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                try {
                    val hourInt = hour.toInt()
                    val minuteInt = minute.toInt()
                    if (hourInt !in 0..23 || minuteInt !in 0..59) {
                        Toast.makeText(context, "请输入有效的时间", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addRoutine(newRoutineTitle, selectedWeekdays.sorted(), hourInt, minuteInt)
                    newRoutineTitle = ""
                    selectedWeekdays.clear()
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "时间格式不正确", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("添加并生成日程")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("已保存的周期事项", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(routines, key = { it.id }) { routine ->
                    RoutineItem(routine = routine, onDelete = { viewModel.deleteRoutine(routine) })
                }
            }
        }
    }
}

@Composable
fun WeekdaySelector(selectedWeekdays: List<Int>, onDaySelected: (Int) -> Unit) {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEachIndexed { index, day ->
            val dayIndex = index + 1
            val isSelected = selectedWeekdays.contains(dayIndex)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onDaySelected(dayIndex) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RoutineItem(routine: Routine, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(routine.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = formatWeekdaysWithTime(routine.weekdays, routine.hour, routine.minute),
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

private fun formatWeekdaysWithTime(weekdays: List<Int>, hour: Int, minute: Int): String {
    if (weekdays.isEmpty()) return "未指定周期"
    val dayNames = weekdays.sorted().map { 
        DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.CHINA)
    }
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    return "每周：${dayNames.joinToString(", ")} | 时间：$timeString"
}
