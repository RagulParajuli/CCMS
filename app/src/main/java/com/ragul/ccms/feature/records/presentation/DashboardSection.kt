package com.ragul.ccms.feature.records.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector

/** Every destination reachable from the sidebar. */
enum class DashboardSection(
    val title: String,
    val description: String,
    val icon: ImageVector,
) {
    DASHBOARD(
        title = "Dashboard",
        description = "A quick overview of your community records.",
        icon = Icons.Outlined.Dashboard,
    ),
    MEMBERSHIP(
        title = "Membership",
        description = "People registered under this account.",
        icon = Icons.Outlined.Groups,
    ),
    LOAN_DEPOSIT(
        title = "Loan or Deposit",
        description = "Money lent to members or deposited with them.",
        icon = Icons.Outlined.AccountBalanceWallet,
    ),
    MONTHLY_DUE(
        title = "Monthly Due",
        description = "Monthly contributions and their payment status.",
        icon = Icons.Outlined.CalendarMonth,
    ),
    ABOUT(
        title = "About App",
        description = "What CCMS does and how your data is stored.",
        icon = Icons.Outlined.Info,
    ),
    HISTORY(
        title = "History",
        description = "Every record change made on this account.",
        icon = Icons.Outlined.History,
    ),
}
