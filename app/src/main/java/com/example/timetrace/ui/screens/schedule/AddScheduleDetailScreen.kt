package com.example.timetrace.ui.screens.schedule

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDetailScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel(),
    onDataChanged: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }
    var month by remember { mutableStateOf((calendar.get(Calendar.MONTH) + 1).toString()) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH).toString()) }
    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY).toString()) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE).toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加日程") },
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
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("日期", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("年") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(2f))
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("月") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("时间", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = hour, onValueChange = { hour = it }, label = { Text("时") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = minute, onValueChange = { minute = it }, label = { Text("分") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        try {
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year.toInt())
                                set(Calendar.MONTH, month.toInt() - 1)
                                set(Calendar.DAY_OF_MONTH, day.toInt())
                                set(Calendar.HOUR_OF_DAY, hour.toInt())
                                set(Calendar.MINUTE, minute.toInt())
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.addSchedule(title, cal.timeInMillis, 1, notes.takeIf { it.isNotBlank() }, isLunar = false, isBirthday = false)
                            onDataChanged()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "请输入有效的日期和时间", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "标题不能为空", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("添加日程", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}