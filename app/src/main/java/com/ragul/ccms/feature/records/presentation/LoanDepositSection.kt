package com.ragul.ccms.feature.records.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.FinancialTransactionEntity
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.feature.records.data.MonthlyDueEntity
import com.ragul.ccms.feature.records.domain.LoanCalculator
import com.ragul.ccms.feature.records.domain.NepaliDate
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.theme.RecordLoanDeposit
import java.util.UUID

@Composable
fun LoanDepositSection(
    loanDeposits: List<LoanDepositEntity>,
    memberships: List<MembershipEntity> = emptyList(),
    monthlyDues: List<MonthlyDueEntity> = emptyList(),
    financialTransactions: List<FinancialTransactionEntity> = emptyList(),
    onSave: (
        existing: LoanDepositEntity?,
        memberId: Long,
        isLoan: Boolean,
        personName: String,
        amount: Double,
        time: Double,
        rate: Double,
        notes: String,
    ) -> Unit,
    onDelete: (LoanDepositEntity) -> Unit,
    onSaveTransaction: (FinancialTransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableLongStateOf(0L) }
    var transactionLoanId by rememberSaveable { mutableLongStateOf(0L) }

    val editing = loanDeposits.firstOrNull { it.id == editingId }
    val pendingDelete = loanDeposits.firstOrNull { it.id == pendingDeleteId }

    val filteredTransactions = financialTransactions
    val filteredLoans = loanDeposits

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = "Loan or Deposit",
                    description = "Money lent to members or deposited with them.",
                    countLabel = "${loanDeposits.size} total",
                )
            }

            if (loanDeposits.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "No loan or deposit records",
                        message = "Tap + to add a loan or deposit. The dashboard count updates automatically.",
                    )
                }
            } else {
                items(filteredLoans, key = { it.id }) { record ->
                    LoanDepositCard(
                        record = record,
                        transactions = financialTransactions.filter { it.memberId == record.memberId && it.loanSyncId == record.syncId },
                        visibleTransactions = filteredTransactions.filter { it.memberId == record.memberId && it.loanSyncId == record.syncId },
                        onAddTransaction = { transactionLoanId = record.id },
                        onUpdateTransaction = onSaveTransaction,
                        onEdit = {
                            editingId = record.id
                            showForm = true
                        },
                        onDelete = { pendingDeleteId = record.id },
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
            containerColor = RecordLoanDeposit,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add loan or deposit")
        }
    }

    if (showForm) {
        key(editingId) {
            LoanDepositFormDialog(
                initial = editing,
                memberships = memberships,
                existingLoans = loanDeposits.filter { it.isLoan },
                onDismiss = { showForm = false },
                onSave = { memberId, isLoan, personName, amount, time, rate, notes ->
                    onSave(editing, memberId, isLoan, personName, amount, time, rate, notes)
                    showForm = false
                },
            )
        }
    }

    pendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = 0L },
            title = { Text(text = "Delete record?") },
            text = {
                Text(
                    text = "${if (record.isLoan) "Loan" else "Deposit"} for ${record.personName} " +
                        "will be removed and the dashboard count will update.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(record)
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

    val transactionLoan = loanDeposits.firstOrNull { it.id == transactionLoanId && it.isLoan }
    transactionLoan?.let { loan ->
        FinancialTransactionDialog(
            loan = loan,
            dues = monthlyDues.filter { it.memberId == loan.memberId },
            transactions = financialTransactions.filter { it.memberId == loan.memberId },
            onDismiss = { transactionLoanId = 0L },
            onSave = { onSaveTransaction(it); transactionLoanId = 0L },
        )
    }
}

@PreviewLightDark
@Composable
private fun LoanDepositSectionPreview() {
    CCMSTheme {
        LoanDepositSection(loanDeposits = emptyList(), memberships = emptyList(), onSave = { _, _, _, _, _, _, _, _ -> }, onDelete = {}, onSaveTransaction = {})
    }
}

@Composable
private fun LoanDepositCard(
    record: LoanDepositEntity,
    transactions: List<FinancialTransactionEntity>,
    visibleTransactions: List<FinancialTransactionEntity>,
    onAddTransaction: () -> Unit,
    onUpdateTransaction: (FinancialTransactionEntity) -> Unit,
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
                        .background(RecordLoanDeposit.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = RecordLoanDeposit,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.personName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (record.memberId > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = RecordLoanDeposit.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "ID: #${record.memberId}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RecordLoanDeposit,
                                )
                            }
                        }
                    }
                    Text(
                        text = formatDate(record.date),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(record.total),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Grand Total",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    StatusPill(
                        text = if (record.isLoan) "Loan" else "Deposit",
                        color = RecordLoanDeposit,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Principal ${formatAmount(record.amount)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Interest ${formatAmount(record.interest)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
            if (record.time > 0.0 || record.rate > 0.0) {
                Text(
                    text = "Time ${record.time.asPlainNumber()} yr · Rate ${record.rate.asPlainNumber()}% per year",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (record.isLoan) {
                val paid = transactions.filter {
                    it.status == FinancialTransactionEntity.STATUS_PAID &&
                        (it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT || it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
                }.sumOf { it.amount }
                val balance = (record.total - paid).coerceAtLeast(0.0)
                Text(text = "Grand Total ${formatAmount(record.total)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Total Paid ${formatAmount(paid)} · Outstanding ${formatAmount(balance)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatusPill(text = record.loanStatus.replace('_', ' '), color = if (record.loanStatus == LoanDepositEntity.LOAN_STATUS_PAID_OFF) Color(0xFF10B981) else RecordLoanDeposit)
                visibleTransactions.sortedByDescending { it.date }.forEach { transaction ->
                    Text(text = "${transaction.kind.replace('_', ' ')} · ${formatAmount(transaction.amount)} · ${transaction.status} · ${formatBsDate(transaction.date)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (transaction.status == FinancialTransactionEntity.STATUS_PENDING) {
                        Row {
                            TextButton(onClick = { onUpdateTransaction(transaction.copy(status = FinancialTransactionEntity.STATUS_PAID)) }) { Text("Mark paid") }
                            TextButton(onClick = { onUpdateTransaction(transaction.copy(status = FinancialTransactionEntity.STATUS_CANCELLED)) }) { Text("Cancel") }
                        }
                    }
                }
                TextButton(onClick = onAddTransaction) { Text("Add financial transaction") }
            }

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = record.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit record",
                        tint = AuthPrimary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete record",
                        tint = Color(0xFFEF4444),
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialTransactionDialog(
    loan: LoanDepositEntity,
    dues: List<MonthlyDueEntity>,
    transactions: List<FinancialTransactionEntity>,
    onDismiss: () -> Unit,
    onSave: (FinancialTransactionEntity) -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf(FinancialTransactionEntity.KIND_LOAN_REPAYMENT) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var transactionSyncId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var status by rememberSaveable { mutableStateOf(FinancialTransactionEntity.STATUS_PENDING) }
    var dueSyncId by rememberSaveable { mutableStateOf("") }
    var parentSyncId by rememberSaveable { mutableStateOf("") }
    var kindMenu by remember { mutableStateOf(false) }
    var dueMenu by remember { mutableStateOf(false) }
    var parentMenu by remember { mutableStateOf(false) }
    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val needsParent = kind == FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL || kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION
    val deposits = transactions.filter { it.kind == FinancialTransactionEntity.KIND_EXTRA_DEPOSIT && it.status == FinancialTransactionEntity.STATUS_PAID }
    val remainingByDeposit = deposits.associate { deposit ->
        deposit.syncId to (deposit.amount - transactions.filter {
            it.parentSyncId == deposit.syncId && it.status == FinancialTransactionEntity.STATUS_PAID &&
                (it.kind == FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL || it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
        }.sumOf { it.amount }).coerceAtLeast(0.0)
    }
    val valid = amount != null && amount > 0.0 && (!needsParent || parentSyncId.isNotBlank())

    RecordFormDialog(
        title = "Add financial transaction · #${loan.memberId} ${loan.personName}",
        onDismiss = onDismiss,
        onSave = {
            val now = System.currentTimeMillis()
            onSave(FinancialTransactionEntity(
                ownerId = loan.ownerId,
                memberId = loan.memberId,
                loanSyncId = loan.syncId,
                dueSyncId = dueSyncId,
                parentSyncId = parentSyncId,
                kind = kind,
                amount = amount ?: 0.0,
                date = now,
                status = status,
                notes = notes.trim(),
                syncId = transactionSyncId,
                createdAt = now,
                updatedAt = now,
            ))
        },
        saveEnabled = valid,
    ) {
        Box {
            OutlinedTextField(value = kind.replace('_', ' '), onValueChange = {}, readOnly = true, label = { Text("Transaction type") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                IconButton(onClick = { kindMenu = true }) { Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Choose transaction type") }
            })
            DropdownMenu(expanded = kindMenu, onDismissRequest = { kindMenu = false }) {
                listOf(
                    FinancialTransactionEntity.KIND_LOAN_REPAYMENT,
                    FinancialTransactionEntity.KIND_EXTRA_DEPOSIT,
                    FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL,
                    FinancialTransactionEntity.KIND_EXTRA_ALLOCATION,
                ).forEach { value -> DropdownMenuItem(text = { Text(value.replace('_', ' ')) }, onClick = { kind = value; kindMenu = false }) }
            }
        }
        if (kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT && dues.isNotEmpty()) {
            Box {
                OutlinedTextField(value = dues.firstOrNull { it.syncId == dueSyncId }?.period ?: "Select monthly due (optional)", onValueChange = {}, readOnly = true,
                    label = { Text("Monthly due") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                        IconButton(onClick = { dueMenu = true }) { Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Choose monthly due") }
                    })
                DropdownMenu(expanded = dueMenu, onDismissRequest = { dueMenu = false }) {
                    DropdownMenuItem(text = { Text("No linked monthly due") }, onClick = { dueSyncId = ""; dueMenu = false })
                    dues.forEach { due -> DropdownMenuItem(text = { Text(due.period) }, onClick = { dueSyncId = due.syncId; dueMenu = false }) }
                }
            }
        }
        if (needsParent) {
            Box {
                OutlinedTextField(value = deposits.firstOrNull { it.syncId == parentSyncId }?.let { "${formatAmount(remainingByDeposit[it.syncId] ?: 0.0)} available" } ?: "Select extra deposit",
                    onValueChange = {}, readOnly = true, label = { Text("Extra deposit") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                        IconButton(onClick = { parentMenu = true }) { Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Choose extra deposit") }
                    })
                DropdownMenu(expanded = parentMenu, onDismissRequest = { parentMenu = false }) {
                    deposits.filter { (remainingByDeposit[it.syncId] ?: 0.0) > 0.0 }.forEach { deposit ->
                        DropdownMenuItem(text = { Text("${formatAmount(remainingByDeposit[deposit.syncId] ?: 0.0)} available") }, onClick = { parentSyncId = deposit.syncId; parentMenu = false })
                    }
                }
            }
        }
        FormTextField(value = amountText, onValueChange = { amountText = it }, label = "Amount *", keyboardType = KeyboardType.Decimal, isError = amountText.isNotBlank() && (amount == null || amount <= 0.0))
        Text(text = "Date: ${formatBsDate(System.currentTimeMillis())}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Payment status", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(FinancialTransactionEntity.STATUS_PENDING, FinancialTransactionEntity.STATUS_PAID, FinancialTransactionEntity.STATUS_CANCELLED).forEach { value ->
                ChoiceChip(label = value, selected = status == value, onClick = { status = value })
            }
        }
        FormTextField(value = notes, onValueChange = { notes = it }, label = "Notes", singleLine = false)
    }
}

@Composable
private fun LoanDepositFormDialog(
    initial: LoanDepositEntity?,
    memberships: List<MembershipEntity>,
    existingLoans: List<LoanDepositEntity>,
    onDismiss: () -> Unit,
    onSave: (
        memberId: Long,
        isLoan: Boolean,
        personName: String,
        amount: Double,
        time: Double,
        rate: Double,
        notes: String,
    ) -> Unit,
) {
    var isLoan by rememberSaveable(initial?.id) { mutableStateOf(initial?.isLoan ?: true) }
    var memberId by rememberSaveable(initial?.id) { mutableLongStateOf(initial?.memberId ?: 0L) }
    var personName by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.personName.orEmpty())
    }
    var amountText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.amount?.takeIf { it != 0.0 }?.asPlainNumber().orEmpty())
    }
    var timeText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.time?.takeIf { it != 0.0 }?.asPlainNumber().orEmpty())
    }
    var rateText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.rate?.asPlainNumber().orEmpty())
    }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val principal = amountText.toNumberOrNull() ?: 0.0
    val time = timeText.toNumberOrNull() ?: 0.0
    val rate = rateText.toNumberOrNull() ?: 0.0

    val amountValid = principal > 0.0
    val timeValid = !isLoan || time > 0.0
    val rateValid = !isLoan || rate >= 0.0
    val valuesValid = amountValid && timeValid && rateValid

    val interest = if (valuesValid && isLoan) LoanCalculator.interest(principal, time, rate) else 0.0
    val total = if (valuesValid && isLoan) LoanCalculator.total(principal, time, rate) else principal

    RecordFormDialog(
        title = if (initial == null) "Add Record" else "Edit Record",
        onDismiss = onDismiss,
        onSave = {
            onSave(
                memberId,
                isLoan,
                personName,
                principal,
                if (isLoan) time else 0.0,
                if (isLoan) rate else 0.0,
                notes,
            )
        },
        saveEnabled = personName.isNotBlank() && memberId > 0L && valuesValid,
    ) {
        Text(text = "Type", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(
                label = "Loan",
                selected = isLoan,
                onClick = { isLoan = true },
            )
            ChoiceChip(
                label = "Deposit",
                selected = !isLoan,
                onClick = { isLoan = false },
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (memberId > 0) "#$memberId - $personName" else personName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Member Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "Select member")
                        }
                    },
                    isError = personName.isBlank(),
                    shape = RoundedCornerShape(10.dp),
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f),
                ) {
                    memberships.forEach { m ->
                        val existingMemberLoan = existingLoans.firstOrNull { it.memberId == m.memberId }
                        DropdownMenuItem(
                            text = {
                                Text(text = "#${m.memberId} - ${m.name}${if (existingMemberLoan != null && existingMemberLoan.id != initial?.id) " · Loan exists" else ""}", fontWeight = FontWeight.Bold)
                            },
                            enabled = !isLoan || existingMemberLoan == null || existingMemberLoan.id == initial?.id,
                            onClick = {
                                memberId = m.memberId
                                personName = m.name
                                dropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                            },
                        )
                    }
                }
        }

        FormTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount (Principal) *",
            keyboardType = KeyboardType.Decimal,
            isError = amountText.isNotBlank() && !amountValid,
            supportingText = when {
                amountText.isBlank() -> "Enter an amount greater than zero."
                !amountValid -> "Enter a valid amount."
                else -> null
            },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = "Time (Years) *",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal,
                isError = timeText.isNotBlank() && !timeValid,
                supportingText = when {
                    timeText.isBlank() -> "Duration in years."
                    !timeValid -> "Must be greater than zero."
                    else -> null
                },
            )
            FormTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = "Rate (% per Year) *",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal,
                isError = rateText.isNotBlank() && !rateValid,
                supportingText = when {
                    rateText.isBlank() -> "Annual interest rate."
                    !rateValid -> "Must be zero or more."
                    else -> null
                },
            )
        }

        if (valuesValid) {
            Surface(
                color = RecordLoanDeposit.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interest",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatAmount(interest),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total (Amount + Interest)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatAmount(total),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RecordLoanDeposit,
                        )
                    }
                }
            }
        }

        FormTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
        )
    }
}

private fun String.toNumberOrNull(): Double? = replace(',', '.').toDoubleOrNull()

private fun Double.asPlainNumber(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()

