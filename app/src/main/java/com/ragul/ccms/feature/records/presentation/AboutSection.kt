package com.ragul.ccms.feature.records.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.BuildConfig
import com.ragul.ccms.R
import com.ragul.ccms.feature.auth.domain.AuthProvider
import com.ragul.ccms.feature.auth.domain.AuthUser
import com.ragul.ccms.feature.records.data.RecordCounts
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.AuthTextPrimary
import com.ragul.ccms.ui.theme.AuthTextSecondary
import com.ragul.ccms.ui.theme.RecordLoanDeposit
import com.ragul.ccms.ui.theme.RecordMembership
import com.ragul.ccms.ui.theme.RecordMonthlyDue

@Composable
fun AboutSection(
    user: AuthUser,
    counts: RecordCounts,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(
                title = "About App",
                description = "What CCMS does and how your data is stored.",
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_leaf),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "CCMS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuthTextPrimary,
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            fontSize = 13.sp,
                            color = AuthTextSecondary,
                        )
                    }
                }
                HorizontalDivider(color = AuthTextSecondary.copy(alpha = 0.15f))
                Text(
                    text = "A simple community management workspace for tracking members, " +
                        "loans or deposits and monthly dues in one place. The dashboard always " +
                        "reflects the records saved in the app database.",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 14.sp,
                    color = AuthTextSecondary,
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Modules",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ModuleRow(
                        icon = Icons.Outlined.Groups,
                        color = RecordMembership,
                        label = "Membership",
                        value = "${counts.memberships} records",
                    )
                    ModuleRow(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        color = RecordLoanDeposit,
                        label = "Loan or Deposit",
                        value = "${counts.loanDeposits} records",
                    )
                    ModuleRow(
                        icon = Icons.Outlined.CalendarMonth,
                        color = RecordMonthlyDue,
                        label = "Monthly Due",
                        value = "${counts.monthlyDues} records",
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Signed-in account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AboutRow("Name", user.displayName?.takeIf { it.isNotBlank() } ?: "Not set")
                    AboutRow("Email", user.email ?: "Not available")
                    AboutRow(
                        "Sign-in method",
                        if (user.provider == AuthProvider.GOOGLE) "Google" else "Email & password",
                    )
                    AboutRow("Email verified", if (user.isEmailVerified) "Yes" else "No")
                    AboutRow("Account ID", user.uid)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AuthPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = AuthPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Where your data is stored",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuthTextPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Records are saved in a private on-device database and are " +
                                "scoped to your account. Firebase is used only for sign-in. " +
                                "Clearing app data or uninstalling the app removes local records.",
                            fontSize = 13.sp,
                            color = AuthTextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleRow(
    icon: ImageVector,
    color: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = AuthTextPrimary,
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AuthTextSecondary,
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 13.sp, color = AuthTextSecondary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AuthTextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}
