package com.ragul.ccms.feature.records.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.feature.records.data.HistoryEntity
import com.ragul.ccms.feature.records.data.RecordsRepository
import com.ragul.ccms.ui.theme.AuthTextPrimary
import com.ragul.ccms.ui.theme.AuthTextSecondary
import com.ragul.ccms.ui.theme.RecordMembership

@Composable
fun HistorySection(
    history: List<HistoryEntity>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PageHeader(
                    title = "History",
                    description = "Every record change made on this account.",
                    countLabel = "${history.size} events",
                    modifier = Modifier.weight(1f),
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = { confirmClear = true }) {
                        Text(text = "Clear", color = Color(0xFFB71C1C))
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.History,
                    title = "No activity yet",
                    message = "Add, edit or delete a record and the change will appear here.",
                )
            }
        } else {
            items(history, key = { it.id }) { entry ->
                HistoryCard(entry = entry)
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(text = "Clear history?") },
            text = {
                Text(
                    text = "Only the activity log is cleared. Your membership, loan, deposit " +
                        "and monthly due records are not affected.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClear()
                        confirmClear = false
                    },
                ) {
                    Text(text = "Clear history", color = Color(0xFFB71C1C))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntity) {
    val (icon, color) = when (entry.action) {
        RecordsRepository.ACTION_ADDED -> Icons.Outlined.AddCircleOutline to Color(0xFF2E7D32)
        RecordsRepository.ACTION_DELETED -> Icons.Outlined.DeleteOutline to Color(0xFFB71C1C)
        else -> Icons.Outlined.Edit to RecordMembership
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                HistoryIcon(icon = icon, color = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.action} · ${entry.section}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuthTextPrimary,
                )
                Text(
                    text = entry.summary,
                    fontSize = 13.sp,
                    color = AuthTextSecondary,
                )
                Text(
                    text = formatDateTime(entry.timestamp),
                    fontSize = 11.sp,
                    color = AuthTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun HistoryIcon(icon: ImageVector, color: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(20.dp),
    )
}
