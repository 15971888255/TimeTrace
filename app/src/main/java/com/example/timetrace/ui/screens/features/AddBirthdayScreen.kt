package com.example.timetrace.ui.screens.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayScreen(navController: NavController, viewModel: ScheduleViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    var isLunar by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }
    var month by remember { mutableStateOf((calendar.get(Calendar.MONTH) + 1).toString()) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH).toString()) }

    val birthdays by viewModel.birthdays.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加生日") },
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
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("农历生日")
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = isLunar, onCheckedChange = { isLunar = it })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = year, onValueChange = { year = it }, label = { Text("年") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                TextField(value = month, onValueChange = { month = it }, label = { Text("月") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                TextField(value = day, onValueChange = { day = it }, label = { Text("日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cal = Calendar.getInstance()
                        cal.set(
                            year.toIntOrNull() ?: cal.get(Calendar.YEAR),
                            (month.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1,
                            day.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                        )
                        viewModel.addSchedule(name, cal.timeInMillis, 1, isLunar, isBirthday = true)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存生日")
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(birthdays) { birthday ->
                    Text(text = "${birthday.title} - ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(birthday.timestamp)}")
                }
            }
        }
    }
}
