package com.example.timetrace.ui.screens.calendar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, viewModel: ScheduleViewModel = hiltViewModel()) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { currentMonth = YearMonth.now() }) {
                        Icon(Icons.Filled.Today, contentDescription = "今天")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            CalendarHeader(
                yearMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeekHeader()
            Spacer(modifier = Modifier.height(8.dp))
            CalendarGrid(yearMonth = currentMonth, schedules = schedules)
        }
    }
}

@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy / MM")
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
        }
        Text(
            text = yearMonth.format(formatter),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
        }
    }
}

@Composable
fun WeekHeader() {
    val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CalendarGrid(yearMonth: YearMonth, schedules: List<Schedule>) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday is 0, Saturday is 6

    val dates = mutableListOf<LocalDate?>()
    for (i in 0 until firstDayOfWeek) {
        dates.add(null) // Add placeholders for empty days
    }
    for (day in 1..daysInMonth) {
        dates.add(yearMonth.atDay(day))
    }

    val schedulesByDate = schedules.groupBy { 
        Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Column {
        val chunkedDates = dates.chunked(7)
        chunkedDates.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().height(64.dp)) { 
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) { 
                        if (date != null) {
                            DayCell(date = date, hasEvent = schedulesByDate.containsKey(date))
                        }
                    }
                }
                if (week.size < 7) {
                    for (i in 0 until (7 - week.size)) {
                        Spacer(modifier = Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(date: LocalDate, hasEvent: Boolean) {
    val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
    val lunar = Lunar(solar)

    val allFestivals = (solar.festivals + lunar.festivals + lunar.otherFestivals).distinct()

    val lunarText = when {
        allFestivals.isNotEmpty() -> allFestivals.first()
        lunar.jieQi.isNotBlank() -> lunar.jieQi
        lunar.dayInChinese == "初一" -> "${lunar.monthInChinese}月"
        else -> lunar.dayInChinese
    }

    val isToday = date.isEqual(LocalDate.now())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    if (isToday) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier
                    }
                )
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.Center) {
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                )
            } else {
                Text(
                    text = lunarText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
