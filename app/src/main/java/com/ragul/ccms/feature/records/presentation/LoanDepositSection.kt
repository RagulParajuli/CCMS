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
import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.theme.RecordLoanDeposit

@Composable
fun LoanDepositSection(
    loanDeposits: List<LoanDepositEntity>,
    memberships: List<MembershipEntity> = emptyList(),
    onSave: (
        existing: LoanDepositEntity?,
        memberId: Long,
        isLoan: Boolean,
        personName: String,
        amount: Double,
        notes: String,
    ) -> Unit,
    onDelete: (LoanDepositEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableLongStateOf(0L) }

    val editing = loanDeposits.firstOrNull { it.id == editingId }
    val pendingDelete = loanDeposits.firstOrNull { it.id == pendingDeleteId }

    val totalLoans = loanDeposits.filter { it.isLoan }.sumOf { it.amount }
    val totalDeposits = loanDeposits.filterNot { it.isLoan }.sumOf { it.amount }

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

            if (loanDeposits.isNotEmpty()) {
                item {
                    SummaryRow(
                        leftLabel = "Loans",
                        leftValue = formatAmount(totalLoans),
                        rightLabel = "Deposits",
                        rightValue = formatAmount(totalDeposits),
                    )
                }
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
                items(loanDeposits, key = { it.id }) { record ->
                    LoanDepositCard(
                        record = record,
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
                onDismiss = { showForm = false },
                onSave = { memberId, isLoan, personName, amount, notes ->
                    onSave(editing, memberId, isLoan, personName, amount, notes)
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
}

@PreviewLightDark
@Composable
private fun LoanDepositSectionPreview() {
    CCMSTheme {
        LoanDepositSection(loanDeposits = emptyList(), memberships = emptyList(), onSave = { _, _, _, _, _, _ -> }, onDelete = {})
    }
}

@Composable
private fun SummaryRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryBox(
            label = leftLabel,
            value = leftValue,
            modifier = Modifier.weight(1f),
        )
        SummaryBox(
            label = rightLabel,
            value = rightValue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RecordLoanDeposit.copy(alpha = 0.08f)),
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
private fun LoanDepositCard(
    record: LoanDepositEntity,
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
                        text = formatAmount(record.amount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusPill(
                        text = if (record.isLoan) "Loan" else "Deposit",
                        color = RecordLoanDeposit,
                    )
                }
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
private fun LoanDepositFormDialog(
    initial: LoanDepositEntity?,
    memberships: List<MembershipEntity>,
    onDismiss: () -> Unit,
    onSave: (memberId: Long, isLoan: Boolean, personName: String, amount: Double, notes: String) -> Unit,
) {
    var isLoan by rememberSaveable(initial?.id) { mutableStateOf(initial?.isLoan ?: true) }
    var memberId by rememberSaveable(initial?.id) { mutableLongStateOf(initial?.memberId ?: 0L) }
    var personName by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.personName.orEmpty())
    }
    var amountText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.amount?.takeIf { it != 0.0 }?.toString().orEmpty())
    }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val amountValid = amount != null && amount > 0

    RecordFormDialog(
        title = if (initial == null) "Add Record" else "Edit Record",
        onDismiss = onDismiss,
        onSave = { onSave(memberId, isLoan, personName, amount ?: 0.0, notes) },
        saveEnabled = personName.isNotBlank() && amountValid,
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

        if (memberships.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (memberId > 0) "#$memberId - $personName" else personName,
                    onValueChange = {
                        personName = it
                        memberId = 0L
                    },
                    label = { Text(text = "Member Name (Select or Type) *") },
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
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "#${m.memberId} - ${m.name}",
                                    fontWeight = FontWeight.Bold,
                                )
                            },
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
        } else {
            FormTextField(
                value = personName,
                onValueChange = { personName = it },
                label = "Member Name *",
                isError = personName.isBlank(),
            )
        }

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

        FormTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
        )
    }
}
