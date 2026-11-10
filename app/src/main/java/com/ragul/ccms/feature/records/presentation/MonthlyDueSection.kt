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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.feature.records.data.MonthlyDueEntity
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.theme.RecordMonthlyDue

@Composable
fun MonthlyDueSection(
    monthlyDues: List<MonthlyDueEntity>,
    memberships: List<MembershipEntity> = emptyList(),
    onSave: (
        existing: MonthlyDueEntity?,
        memberId: Long,
        title: String,
        period: String,
        amount: Double,
        isPaid: Boolean,
        notes: String,
    ) -> Unit,
    onDelete: (MonthlyDueEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableLongStateOf(0L) }

    val editing = monthlyDues.firstOrNull { it.id == editingId }
    val pendingDelete = monthlyDues.firstOrNull { it.id == pendingDeleteId }

    val collected = monthlyDues.filter { it.isPaid }.sumOf { it.amount }
    val pending = monthlyDues.filterNot { it.isPaid }.sumOf { it.amount }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = "Monthly Due",
                    description = "Monthly contributions and payment status.",
                    countLabel = "${monthlyDues.size} total",
                )
            }

            if (monthlyDues.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DueSummaryBox(
                            label = "Collected",
                            value = formatAmount(collected),
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                        )
                        DueSummaryBox(
                            label = "Pending",
                            value = formatAmount(pending),
                            color = RecordMonthlyDue,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (monthlyDues.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "No monthly dues yet",
                        message = "Tap + to add a due. The dashboard count updates automatically.",
                    )
                }
            } else {
                items(monthlyDues, key = { it.id }) { due ->
                    MonthlyDueCard(
                        due = due,
                        onEdit = {
                            editingId = due.id
                            showForm = true
                        },
                        onDelete = { pendingDeleteId = due.id },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingId = 0L
                showForm = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = RecordMonthlyDue,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add monthly due")
        }
    }

    if (showForm) {
        key(editingId) {
            MonthlyDueFormDialog(
                initial = editing,
                memberships = memberships,
                onDismiss = { showForm = false },
                onSave = { memberId, title, period, amount, isPaid, notes ->
                    onSave(editing, memberId, title, period, amount, isPaid, notes)
                    showForm = false
                },
            )
        }
    }

    pendingDelete?.let { due ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = 0L },
            title = { Text(text = "Delete monthly due?") },
            text = {
                Text(
                    text = "${due.title} for ${due.period} will be removed and the dashboard " +
                        "count will update.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(due)
                        pendingDeleteId = 0L
                    },
                ) {
                    Text(text = "Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = 0L }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun MonthlyDueSectionPreview() {
    CCMSTheme {
        MonthlyDueSection(monthlyDues = emptyList(), memberships = emptyList(), onSave = { _, _, _, _, _, _, _ -> }, onDelete = {})
    }
}

@Composable
private fun DueSummaryBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MonthlyDueCard(
    due: MonthlyDueEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(RecordMonthlyDue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = RecordMonthlyDue,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = due.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (due.memberId > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = RecordMonthlyDue.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "ID: #${due.memberId}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RecordMonthlyDue,
                                )
                            }
                        }
                    }
                    Text(text = due.period, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(due.amount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusPill(
                        text = if (due.isPaid) "Paid" else "Pending",
                        color = if (due.isPaid) Color(0xFF10B981) else RecordMonthlyDue,
                    )
                }
            }

            if (due.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = due.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Added ${formatDate(due.dueDate)}",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit due",
                        tint = AuthPrimary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete due",
                        tint = Color(0xFFEF4444),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyDueFormDialog(
    initial: MonthlyDueEntity?,
    memberships: List<MembershipEntity>,
    onDismiss: () -> Unit,
    onSave: (memberId: Long, title: String, period: String, amount: Double, isPaid: Boolean, notes: String) -> Unit,
) {
    var memberId by rememberSaveable(initial?.id) { mutableLongStateOf(initial?.memberId ?: 0L) }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var period by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.period ?: currentPeriod())
    }
    var amountText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.amount?.takeIf { it != 0.0 }?.toString().orEmpty())
    }
    var isPaid by rememberSaveable(initial?.id) { mutableStateOf(initial?.isPaid ?: false) }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val amountValid = amount != null && amount > 0.0

    RecordFormDialog(
        title = if (initial == null) "Add monthly due" else "Edit monthly due",
        onDismiss = onDismiss,
        onSave = { onSave(memberId, title, period, amount ?: 0.0, isPaid, notes) },
        saveEnabled = title.isNotBlank() && period.isNotBlank() && amountValid,
    ) {
        if (memberships.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (memberId > 0) "#$memberId - $title" else title,
                    onValueChange = {
                        title = it
                        memberId = 0L
                    },
                    label = { Text(text = "Member or title (Select or Type) *") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "Select member")
                        }
                    },
                    isError = title.isBlank(),
                    shape = RoundedCornerShape(10.dp),
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f),
                ) {
                    memberships.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "#${m.memberId} - ${m.name}",
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            onClick = {
                                memberId = m.memberId
                                title = m.name
                                dropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                            },
                        )
                    }
                }
            }
        } else {
            FormTextField(
                value = title,
                onValueChange = { title = it },
                label = "Member or title *",
                isError = title.isBlank(),
            )
        }

        FormTextField(
            value = period,
            onValueChange = { period = it },
            label = "Period *",
            isError = period.isBlank(),
            supportingText = "For example: ${currentPeriod()}",
        )
        FormTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount *",
            keyboardType = KeyboardType.Decimal,
            isError = amountText.isNotBlank() && !amountValid,
            supportingText = when {
                amountText.isBlank() -> "Enter an amount greater than zero."
                !amountValid -> "Enter a valid amount."
                else -> null
            },
        )
        Text(text = "Payment status", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(
                label = "Pending",
                selected = !isPaid,
                onClick = { isPaid = false },
            )
            ChoiceChip(
                label = "Paid",
                selected = isPaid,
                onClick = { isPaid = true },
            )
        }
        FormTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
        )
    }
}
