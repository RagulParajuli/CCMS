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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.AuthTextPrimary
import com.ragul.ccms.ui.theme.AuthTextSecondary
import com.ragul.ccms.ui.theme.RecordMembership

@Composable
fun MembershipSection(
    memberships: List<MembershipEntity>,
    onSave: (MembershipEntity?, String, String, String, String) -> Unit,
    onDelete: (MembershipEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableStateOf(0L) }

    val editing = memberships.firstOrNull { it.id == editingId }
    val pendingDelete = memberships.firstOrNull { it.id == pendingDeleteId }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = "Membership",
                    description = "People registered under this account.",
                    countLabel = "${memberships.size} total",
                )
            }

            if (memberships.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = "No members yet",
                        message = "Tap + to add the first member. The dashboard count updates automatically.",
                    )
                }
            } else {
                items(memberships, key = { it.id }) { membership ->
                    MembershipCard(
                        membership = membership,
                        onEdit = {
                            editingId = membership.id
                            showForm = true
                        },
                        onDelete = { pendingDeleteId = membership.id },
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
            containerColor = AuthPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add member")
        }
    }

    if (showForm) {
        key(editingId) {
            MembershipFormDialog(
                initial = editing,
                onDismiss = { showForm = false },
                onSave = { name, phone, status, notes ->
                    onSave(editing, name, phone, status, notes)
                    showForm = false
                },
            )
        }
    }

    pendingDelete?.let { membership ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = 0L },
            title = { Text(text = "Delete member?") },
            text = {
                Text(
                    text = "${membership.name} will be removed and the dashboard count will update.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(membership)
                        pendingDeleteId = 0L
                    },
                ) {
                    Text(text = "Delete", color = Color(0xFFB71C1C))
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

@Composable
private fun MembershipCard(
    membership: MembershipEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(RecordMembership.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = membership.name.take(1).uppercase(),
                        color = RecordMembership,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = membership.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTextPrimary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = AuthTextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = membership.phone.ifBlank { "No phone number" },
                            fontSize = 13.sp,
                            color = AuthTextSecondary,
                        )
                    }
                }
                StatusPill(
                    text = membership.status,
                    color = if (membership.status == "Active") {
                        Color(0xFF2E7D32)
                    } else {
                        Color(0xFF757575)
                    },
                )
            }

            if (membership.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = membership.notes,
                    fontSize = 13.sp,
                    color = AuthTextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Joined ${formatDate(membership.joinedOn)}",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = AuthTextSecondary,
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit member",
                        tint = AuthPrimary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete member",
                        tint = Color(0xFFB71C1C),
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipFormDialog(
    initial: MembershipEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, status: String, notes: String) -> Unit,
) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var phone by rememberSaveable(initial?.id) { mutableStateOf(initial?.phone.orEmpty()) }
    var status by rememberSaveable(initial?.id) { mutableStateOf(initial?.status ?: "Active") }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    RecordFormDialog(
        title = if (initial == null) "Add member" else "Edit member",
        onDismiss = onDismiss,
        onSave = { onSave(name, phone, status, notes) },
        saveEnabled = name.isNotBlank(),
    ) {
        FormTextField(
            value = name,
            onValueChange = { name = it },
            label = "Member name *",
            isError = name.isBlank(),
            supportingText = if (name.isBlank()) "Name is required." else null,
        )
        FormTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone number",
            keyboardType = KeyboardType.Phone,
        )
        Text(
            text = "Status",
            fontSize = 13.sp,
            color = AuthTextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(
                label = "Active",
                selected = status == "Active",
                onClick = { status = "Active" },
            )
            ChoiceChip(
                label = "Inactive",
                selected = status == "Inactive",
                onClick = { status = "Inactive" },
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
