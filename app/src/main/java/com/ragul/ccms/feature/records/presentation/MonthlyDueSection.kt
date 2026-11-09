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
import com.ragul.ccms.ui.localization.Text
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
import com.ragul.ccms.feature.records.data.FinancialTransactionEntity
import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.MonthlyDueEntity
import com.ragul.ccms.feature.records.domain.NepaliDate
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.theme.RecordMonthlyDue
import java.util.UUID

private fun parseBsPeriod(period: String?): NepaliDate? {
    val parts = period?.trim()?.split(' ') ?: return null
    if (parts.size < 2) return null
    val month = NepaliDate.MONTH_NAMES.indexOfFirst { it.equals(parts[0], ignoreCase = true) } + 1
    val year = parts[1].toIntOrNull() ?: return null
    return NepaliDate(year, month, 1).takeIf { NepaliDate.daysInMonth(year, month) > 0 }
}

@Composable
fun MonthlyDueSection(
    monthlyDues: List<MonthlyDueEntity>,
    memberships: List<MembershipEntity> = emptyList(),
    loans: List<LoanDepositEntity> = emptyList(),
    financialTransactions: List<FinancialTransactionEntity> = emptyList(),
    onSave: (
        existing: MonthlyDueEntity?,
        memberId: Long,
        title: String,
        period: String,
        grandTotal: Double,
        monthlyPaymentAmount: Double,
        notes: String,
    ) -> Unit,
    onSaveTransaction: (FinancialTransactionEntity) -> Unit,
    onDelete: (MonthlyDueEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableLongStateOf(0L) }
    var paymentDueId by rememberSaveable { mutableLongStateOf(0L) }

    val editing = monthlyDues.firstOrNull { it.id == editingId }
    val pendingDelete = monthlyDues.firstOrNull { it.id == pendingDeleteId }

    val dueSyncIds = monthlyDues.mapTo(mutableSetOf()) { it.syncId }
    val collected = financialTransactions.filter {
        it.dueSyncId in dueSyncIds && it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT &&
            it.status == FinancialTransactionEntity.STATUS_PAID
    }.sumOf { it.amount }
    val pending = monthlyDues.sumOf { due ->
        (due.monthlyPaymentAmount - financialTransactions.filter {
            it.dueSyncId == due.syncId && it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT &&
                it.status == FinancialTransactionEntity.STATUS_PAID
        }.sumOf { it.amount }).coerceAtLeast(0.0)
    }

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
                        memberships = memberships,
                        loan = loans.firstOrNull { it.memberId == due.memberId },
                        transactions = financialTransactions.filter { it.memberId == due.memberId },
                        onAddPayment = { paymentDueId = due.id },
                        onUpdateTransaction = onSaveTransaction,
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
                loans = loans,
                onDismiss = { showForm = false },
                onSave = { memberId, title, period, grandTotal, monthlyPaymentAmount, notes ->
                    onSave(editing, memberId, title, period, grandTotal, monthlyPaymentAmount, notes)
                    showForm = false
                },
            )
        }
    }

    val paymentDue = monthlyDues.firstOrNull { it.id == paymentDueId }
    val paymentLoan = paymentDue?.let { due -> loans.firstOrNull { it.memberId == due.memberId } }
    if (paymentDue != null && paymentLoan != null) {
        MonthlyDuePaymentDialog(
            due = paymentDue,
            loan = paymentLoan,
            paidForDue = financialTransactions.filter {
                it.dueSyncId == paymentDue.syncId && it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT &&
                    it.status == FinancialTransactionEntity.STATUS_PAID
            }.sumOf { it.amount },
            onDismiss = { paymentDueId = 0L },
            onSave = { onSaveTransaction(it); paymentDueId = 0L },
        )
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
        MonthlyDueSection(monthlyDues = emptyList(), memberships = emptyList(), loans = emptyList(), financialTransactions = emptyList(), onSave = { _, _, _, _, _, _, _ -> }, onSaveTransaction = {}, onDelete = {})
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
    memberships: List<MembershipEntity>,
    loan: LoanDepositEntity?,
    transactions: List<FinancialTransactionEntity>,
    onAddPayment: () -> Unit,
    onUpdateTransaction: (FinancialTransactionEntity) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val loanPayments = transactions.filter {
        loan != null && it.loanSyncId == loan.syncId && it.status == FinancialTransactionEntity.STATUS_PAID &&
            (it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT || it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
    }
    val totalPaid = loanPayments.sumOf { it.amount }
    val remaining = ((loan?.total ?: due.grandTotal) - totalPaid).coerceAtLeast(0.0)
    val memberName = memberships.firstOrNull { it.memberId == due.memberId }?.name ?: due.title
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
                    Text(text = "${due.period} · $memberName", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(remaining),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusPill(text = if (remaining <= 0.0) "Paid off" else "Outstanding", color = if (remaining <= 0.0) Color(0xFF10B981) else RecordMonthlyDue)
                }
            }

            if (due.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = due.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("Member: #${due.memberId} · $memberName", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Original Grand Total: ${formatAmount(loan?.total ?: due.grandTotal)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Monthly Payment Amount: ${formatAmount(due.monthlyPaymentAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Total Paid: ${formatAmount(totalPaid)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Remaining Balance: ${formatAmount(remaining)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            transactions.filter { it.dueSyncId == due.syncId }.sortedByDescending { it.date }.forEach { payment ->
                Text("${formatBsDate(payment.date)} · ${formatAmount(payment.amount)} · ${payment.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (payment.status == FinancialTransactionEntity.STATUS_PENDING) {
                    Row {
                        TextButton(onClick = { onUpdateTransaction(payment.copy(status = FinancialTransactionEntity.STATUS_PAID)) }) { Text("Mark paid") }
                        TextButton(onClick = { onUpdateTransaction(payment.copy(status = FinancialTransactionEntity.STATUS_CANCELLED)) }) { Text("Cancel") }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val paidForDue = transactions.filter {
                    it.dueSyncId == due.syncId && it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT &&
                        it.status == FinancialTransactionEntity.STATUS_PAID
                }.sumOf { it.amount }
                TextButton(onClick = onAddPayment, enabled = loan != null && due.monthlyPaymentAmount - paidForDue > 0.000001) { Text("Record payment") }
                Text(
                    text = "Added ${NepaliDate.today(java.util.Calendar.getInstance().apply { timeInMillis = due.dueDate })?.format() ?: "—"}",
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
    loans: List<LoanDepositEntity>,
    onDismiss: () -> Unit,
    onSave: (memberId: Long, title: String, period: String, grandTotal: Double, monthlyPaymentAmount: Double, notes: String) -> Unit,
) {
    var memberId by rememberSaveable(initial?.id) { mutableLongStateOf(initial?.memberId ?: 0L) }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    val today = remember { NepaliDate.today() ?: NepaliDate(2083, 1, 1) }
    val initialDate = remember(initial?.id) { parseBsPeriod(initial?.period) ?: today }
    var bsMonth by rememberSaveable(initial?.id) { mutableStateOf(initialDate.month) }
    var bsYear by rememberSaveable(initial?.id) { mutableStateOf(initialDate.year) }
    var monthlyPaymentText by rememberSaveable(initial?.id) { mutableStateOf(initial?.monthlyPaymentAmount?.takeIf { it > 0 }?.toString().orEmpty()) }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val monthlyPayment = monthlyPaymentText.replace(',', '.').toDoubleOrNull()
    val selectedMember = memberships.firstOrNull { it.memberId == memberId }
    val selectedLoan = loans.firstOrNull { it.memberId == memberId }
    val originalGrandTotal = initial?.takeIf { it.memberId == memberId }?.grandTotal?.takeIf { it > 0.0 }
        ?: selectedLoan?.total
    val period = "${NepaliDate.monthName(bsMonth)} $bsYear BS"

    RecordFormDialog(
        title = if (initial == null) "Add Monthly Due" else "Edit Monthly Due",
        onDismiss = onDismiss,
        onSave = { onSave(memberId, selectedMember?.name.orEmpty(), period, originalGrandTotal ?: 0.0, monthlyPayment ?: 0.0, notes) },
        saveEnabled = selectedMember != null && selectedLoan != null && monthlyPayment != null && monthlyPayment > 0.0,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedMember?.let { "#${it.memberId} - ${it.name}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Member *") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { dropdownExpanded = true }) {
                        Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "Select member")
                    }
                },
                shape = RoundedCornerShape(10.dp),
            )
            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.fillMaxWidth(0.85f)) {
                memberships.forEach { member ->
                    val memberLoans = loans.filter { it.memberId == member.memberId }
                    if (memberLoans.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("#${member.memberId} - ${member.name} · No loan") },
                            enabled = false,
                            onClick = {},
                            leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
                        )
                    } else {
                        memberLoans.forEach { memberLoan ->
                            DropdownMenuItem(
                                text = { Text("#${member.memberId} - ${member.name} (${formatAmount(memberLoan.total)})") },
                                enabled = true,
                                onClick = { memberId = member.memberId; title = member.name; dropdownExpanded = false },
                                leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
                            )
                        }
                    }
                }
            }
        }
        if (selectedLoan == null) {
            Text(text = "Select a member with a loan record to create a monthly due.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        } else {
            Text(text = "Original Grand Total: ${formatAmount(originalGrandTotal ?: selectedLoan.total)}", fontWeight = FontWeight.SemiBold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var monthExpanded by remember { mutableStateOf(false) }
            var yearExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1.2f)) {
                OutlinedTextField(value = NepaliDate.monthName(bsMonth), onValueChange = {}, readOnly = true,
                    label = { Text("BS Month") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                        IconButton(onClick = { monthExpanded = true }) { Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select BS month") }
                    })
                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                    NepaliDate.MONTH_NAMES.forEachIndexed { index, name -> DropdownMenuItem(text = { Text(name) }, onClick = { bsMonth = index + 1; monthExpanded = false }) }
                }
            }
            Box(modifier = Modifier.weight(0.8f)) {
                OutlinedTextField(value = bsYear.toString(), onValueChange = {}, readOnly = true,
                    label = { Text("BS Year") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                        IconButton(onClick = { yearExpanded = true }) { Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select BS year") }
                    })
                DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                    (NepaliDate.MIN_YEAR..NepaliDate.MAX_YEAR).forEach { year -> DropdownMenuItem(text = { Text(year.toString()) }, onClick = { bsYear = year; yearExpanded = false }) }
                }
            }
        }
        FormTextField(value = monthlyPaymentText, onValueChange = { monthlyPaymentText = it }, label = "Expected Monthly Payment Amount *", keyboardType = KeyboardType.Decimal,
            isError = monthlyPaymentText.isNotBlank() && (monthlyPayment == null || monthlyPayment <= 0.0),
            supportingText = "The owner sets this amount; no installment formula is assumed.")

        FormTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
        )
    }
}

@Composable
private fun MonthlyDuePaymentDialog(
    due: MonthlyDueEntity,
    loan: LoanDepositEntity,
    paidForDue: Double,
    onDismiss: () -> Unit,
    onSave: (FinancialTransactionEntity) -> Unit,
) {
    var amountText by rememberSaveable(due.id) { mutableStateOf("") }
    var status by rememberSaveable(due.id) { mutableStateOf(FinancialTransactionEntity.STATUS_PENDING) }
    var notes by rememberSaveable(due.id) { mutableStateOf("") }
    var transactionSyncId by rememberSaveable(due.id) { mutableStateOf(UUID.randomUUID().toString()) }
    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val monthlyBalance = (due.monthlyPaymentAmount - paidForDue).coerceAtLeast(0.0)
    RecordFormDialog(
        title = "Record payment · ${due.period}",
        onDismiss = onDismiss,
        onSave = {
            val now = System.currentTimeMillis()
            onSave(FinancialTransactionEntity(
                ownerId = due.ownerId,
                memberId = due.memberId,
                loanSyncId = loan.syncId,
                dueSyncId = due.syncId,
                kind = FinancialTransactionEntity.KIND_LOAN_REPAYMENT,
                amount = amount ?: 0.0,
                date = now,
                status = status,
                notes = notes.trim(),
                syncId = transactionSyncId,
                createdAt = now,
                updatedAt = now,
            ))
        },
        saveEnabled = amount != null && amount > 0.0 && amount <= monthlyBalance + 0.000001,
    ) {
        Text(text = "Member: #${due.memberId} · ${due.title}")
        Text(text = "Expected monthly payment: ${formatAmount(due.monthlyPaymentAmount)}")
        Text(text = "Unpaid monthly amount: ${formatAmount(monthlyBalance)}")
        FormTextField(value = amountText, onValueChange = { amountText = it }, label = "Payment amount *", keyboardType = KeyboardType.Decimal,
            isError = amountText.isNotBlank() && (amount == null || amount <= 0.0 || amount > monthlyBalance))
        Text(text = "Payment date: ${formatBsDate(System.currentTimeMillis())}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(FinancialTransactionEntity.STATUS_PENDING, FinancialTransactionEntity.STATUS_PAID, FinancialTransactionEntity.STATUS_CANCELLED).forEach {
                ChoiceChip(label = it, selected = status == it, onClick = { status = it })
            }
        }
        FormTextField(value = notes, onValueChange = { notes = it }, label = "Notes", singleLine = false)
    }
}
