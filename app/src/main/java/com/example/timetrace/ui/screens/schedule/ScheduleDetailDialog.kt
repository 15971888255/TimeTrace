package com.example.timetrace.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.timetrace.data.model.Schedule

@Composable
fun ScheduleDetailDialog(
    schedule: Schedule?,
    onDismiss: () -> Unit,
    onDelete: (Schedule) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (schedule != null) {
                    Text(text = schedule.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = schedule.notes ?: "没有备注",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            onDelete(schedule)
                            onDismiss()
                        }) {
                            Text("删除")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onDismiss) {
                            Text("关闭")
                        }
                    }
                } else {
                    Text("加载中...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
