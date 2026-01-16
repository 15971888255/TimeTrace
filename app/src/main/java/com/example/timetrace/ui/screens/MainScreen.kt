package com.example.timetrace.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.timetrace.di.CoroutineScopeEntryPoint
import com.example.timetrace.ui.screens.features.AddBirthdayScreen
import com.example.timetrace.ui.screens.features.AddRoutineScreen
import com.example.timetrace.ui.screens.schedule.AddScheduleDetailScreen
import com.example.timetrace.ui.screens.calendar.CalendarScreen
import com.example.timetrace.ui.screens.schedule.ScheduleDetailDialog
import com.example.timetrace.ui.screens.schedule.ScheduleScreen
import com.example.timetrace.ui.viewmodel.ScheduleViewModel
import com.example.timetrace.ui.widget.updateAllWidgets
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "日历", Icons.Default.DateRange)
    object Schedule : Screen("schedule", "日程", Icons.Default.Home)
}

val items = listOf(
    Screen.Calendar,
    Screen.Schedule,
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val hiltEntryPoint = EntryPointAccessors.fromApplication(context, CoroutineScopeEntryPoint::class.java)
    val scope = hiltEntryPoint.coroutineScope()
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()
    val schedules by scheduleViewModel.allSchedules.collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route in items.map { it.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Calendar.route, Modifier.padding(innerPadding)) {
            composable(Screen.Schedule.route) { ScheduleScreen(navController) }
            composable(Screen.Calendar.route) { CalendarScreen(navController) }
            composable("add_schedule_detail") { AddScheduleDetailScreen(navController) { updateAllWidgets(context, scope) } }
            composable("add_birthday") { AddBirthdayScreen(navController) { updateAllWidgets(context, scope) } }
            composable("add_routine") { 
                Log.d("Navigation", "Navigating to AddRoutineScreen")
                AddRoutineScreen(navController) { updateAllWidgets(context, scope) }
            }
            dialog(
                route = "schedule_detail/{scheduleId}",
                arguments = listOf(navArgument("scheduleId") { type = NavType.LongType }),
                deepLinks = listOf(navDeepLink { 
                    uriPattern = "app://com.example.timetrace/schedule_detail/{scheduleId}" 
                    action = Intent.ACTION_VIEW
                })
            ) {
                val scheduleId = it.arguments?.getLong("scheduleId")
                val schedule = schedules.find { schedule -> schedule.id == scheduleId }
                ScheduleDetailDialog(
                    schedule = schedule, 
                    onDismiss = { navController.popBackStack() },
                    onDelete = { scheduleToDelete ->
                        scope.launch {
                            scheduleViewModel.deleteSchedule(scheduleToDelete)
                            updateAllWidgets(context, scope)
                        }
                    }
                )
            }
        }
    }
}
