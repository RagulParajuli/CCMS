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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.theme.RecordMembership
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MembershipSection(
    memberships: List<MembershipEntity>,
    onSave: (
        existing: MembershipEntity?,
        name: String,
        gender: String,
        dob: String,
        parentName: String,
        grandfatherName: String,
        citizenshipNumber: String,
        address: String,
        phone: String,
        email: String,
        status: String,
        notes: String,
    ) -> Unit,
    onDelete: (MembershipEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    var pendingDeleteId by rememberSaveable { mutableLongStateOf(0L) }

    val filteredMemberships = remember(memberships, searchQuery) {
        if (searchQuery.isBlank()) {
            memberships
        } else {
            val q = searchQuery.trim().lowercase()
            memberships.filter { m ->
                m.name.lowercase().contains(q) ||
                    "#${m.memberId}".contains(q) ||
                    m.memberId.toString().contains(q) ||
                    m.phone.lowercase().contains(q) ||
                    m.citizenshipNumber.lowercase().contains(q) ||
                    m.address.lowercase().contains(q)
            }
        }
    }

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
                    description = "Registered members & family details.",
                    countLabel = "${memberships.size} total",
                )
            }

            if (memberships.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Search by ID, name, mobile, citizenship #") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }

            if (filteredMemberships.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = if (memberships.isEmpty()) "No members yet" else "No matching members",
                        message = if (memberships.isEmpty()) {
                            "Tap + to register the first member with auto-generated ID."
                        } else {
                            "No members found matching \"$searchQuery\"."
                        },
                    )
                }
            } else {
                items(filteredMemberships, key = { it.id }) { membership ->
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
                nextAutoId = (memberships.maxOfOrNull { it.memberId } ?: 0L) + 1L,
                onDismiss = { showForm = false },
                onSave = { name, gender, dob, parentName, grandfatherName, citizenshipNumber, address, phone, email, status, notes ->
                    onSave(
                        editing,
                        name,
                        gender,
                        dob,
                        parentName,
                        grandfatherName,
                        citizenshipNumber,
                        address,
                        phone,
                        email,
                        status,
                        notes,
                    )
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
                    text = "${membership.name} (ID: #${membership.memberId}) will be removed. Deleted IDs will not be reused.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(membership)
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
private fun MembershipSectionPreview() {
    CCMSTheme {
        MembershipSection(
            memberships = emptyList(),
            onSave = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onDelete = {},
        )
    }
}

@Composable
private fun MembershipCard(
    membership: MembershipEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(membership.id) { mutableStateOf(false) }

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
                        .size(44.dp)
                        .background(RecordMembership.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (membership.memberId > 0) "#${membership.memberId}" else membership.name.take(1).uppercase(),
                        color = RecordMembership,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = membership.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = RecordMembership.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "ID: #${membership.memberId}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RecordMembership,
                            )
                        }
                    }
                    if (membership.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = membership.phone,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                StatusPill(
                    text = membership.status,
                    color = if (membership.status == "Active") {
                        Color(0xFF10B981)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (membership.address.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Home,
                        label = "Address",
                        value = membership.address,
                    )
                }
                if (membership.citizenshipNumber.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Badge,
                        label = "Citizenship #",
                        value = membership.citizenshipNumber,
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (membership.gender.isNotBlank()) {
                        DetailRow(icon = Icons.Outlined.Person, label = "Gender", value = membership.gender)
                    }
                    if (membership.dob.isNotBlank()) {
                        DetailRow(icon = Icons.Outlined.Person, label = "Date of Birth", value = membership.dob)
                    }
                    if (membership.parentName.isNotBlank()) {
                        DetailRow(icon = Icons.Outlined.Person, label = "Father/Mother", value = membership.parentName)
                    }
                    if (membership.grandfatherName.isNotBlank()) {
                        DetailRow(icon = Icons.Outlined.Person, label = "Grandfather", value = membership.grandfatherName)
                    }
                    if (membership.email.isNotBlank()) {
                        DetailRow(icon = Icons.Outlined.Email, label = "Email", value = membership.email)
                    }
                    if (membership.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${membership.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (expanded) "Show less" else "More details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthPrimary,
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = AuthPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

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
                        tint = Color(0xFFEF4444),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembershipFormDialog(
    initial: MembershipEntity?,
    nextAutoId: Long,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        gender: String,
        dob: String,
        parentName: String,
        grandfatherName: String,
        citizenshipNumber: String,
        address: String,
        phone: String,
        email: String,
        status: String,
        notes: String,
    ) -> Unit,
) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var gender by rememberSaveable(initial?.id) { mutableStateOf(initial?.gender ?: "Male") }
    var dob by rememberSaveable(initial?.id) { mutableStateOf(initial?.dob.orEmpty()) }
    var parentName by rememberSaveable(initial?.id) { mutableStateOf(initial?.parentName.orEmpty()) }
    var grandfatherName by rememberSaveable(initial?.id) { mutableStateOf(initial?.grandfatherName.orEmpty()) }
    var citizenshipNumber by rememberSaveable(initial?.id) { mutableStateOf(initial?.citizenshipNumber.orEmpty()) }
    var address by rememberSaveable(initial?.id) { mutableStateOf(initial?.address.orEmpty()) }
    var phone by rememberSaveable(initial?.id) { mutableStateOf(initial?.phone.orEmpty()) }
    var email by rememberSaveable(initial?.id) { mutableStateOf(initial?.email.orEmpty()) }
    var status by rememberSaveable(initial?.id) { mutableStateOf(initial?.status ?: "Active") }
    var notes by rememberSaveable(initial?.id) { mutableStateOf(initial?.notes.orEmpty()) }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dob = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val displayedId = if (initial != null && initial.memberId > 0) {
        "#${initial.memberId}"
    } else {
        "#$nextAutoId (Auto-generated)"
    }

    RecordFormDialog(
        title = if (initial == null) "Register Member" else "Edit Member",
        onDismiss = onDismiss,
        onSave = {
            onSave(
                name,
                gender,
                dob,
                parentName,
                grandfatherName,
                citizenshipNumber,
                address,
                phone,
                email,
                status,
                notes,
            )
        },
        saveEnabled = name.isNotBlank(),
    ) {
        Surface(
            color = AuthPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Membership ID: ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = displayedId,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthPrimary,
                )
            }
        }

        FormTextField(
            value = name,
            onValueChange = { name = it },
            label = "Member's Name *",
            isError = name.isBlank(),
            supportingText = if (name.isBlank()) "Name is required." else null,
        )

        Text(
            text = "Gender",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(
                label = "Male",
                selected = gender == "Male",
                onClick = { gender = "Male" },
            )
            ChoiceChip(
                label = "Female",
                selected = gender == "Female",
                onClick = { gender = "Female" },
            )
            ChoiceChip(
                label = "Other",
                selected = gender == "Other",
                onClick = { gender = "Other" },
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text(text = "Date of Birth (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = "Select date of birth")
                    }
                },
                shape = RoundedCornerShape(10.dp),
            )
        }

        FormTextField(
            value = parentName,
            onValueChange = { parentName = it },
            label = "Father's or Mother's Name",
        )

        FormTextField(
            value = grandfatherName,
            onValueChange = { grandfatherName = it },
            label = "Grandfather's Name",
        )

        FormTextField(
            value = citizenshipNumber,
            onValueChange = { citizenshipNumber = it },
            label = "Citizenship Number",
        )

        FormTextField(
            value = address,
            onValueChange = { address = it },
            label = "Address",
        )

        FormTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Mobile Number",
            keyboardType = KeyboardType.Phone,
        )

        FormTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            keyboardType = KeyboardType.Email,
        )

        Text(
            text = "Status",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
