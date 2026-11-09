package com.ragul.ccms.feature.records.presentation

import androidx.compose.material3.MaterialTheme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import com.ragul.ccms.ui.localization.Text
import com.ragul.ccms.ui.localization.LanguageToggle
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragul.ccms.R
import com.ragul.ccms.feature.auth.domain.AuthProvider
import com.ragul.ccms.feature.auth.domain.AuthUser
import com.ragul.ccms.feature.auth.presentation.AuthUiState
import com.ragul.ccms.feature.auth.presentation.MessageBanner
import com.ragul.ccms.feature.records.data.HistoryEntity
import com.ragul.ccms.feature.records.data.FinancialTransactionEntity
import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.feature.records.data.MonthlyDueEntity
import com.ragul.ccms.feature.records.data.RecordCounts
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.AuthPrimaryDark
import com.ragul.ccms.ui.theme.DarkBlueColorScheme
import com.ragul.ccms.ui.theme.RecordLoanDeposit
import com.ragul.ccms.ui.theme.RecordMembership
import com.ragul.ccms.ui.theme.RecordMonthlyDue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    user: AuthUser,
    authState: AuthUiState,
    onSignOut: () -> Unit,
    onResendVerification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: RecordsViewModel = viewModel(
        key = "records_${user.uid}",
        factory = RecordsViewModel.factory(context, user.uid),
    )

    val counts by viewModel.counts.collectAsStateWithLifecycle()
    val memberships by viewModel.memberships.collectAsStateWithLifecycle()
    val loanDeposits by viewModel.loanDeposits.collectAsStateWithLifecycle()
    val monthlyDues by viewModel.monthlyDues.collectAsStateWithLifecycle()
    val financialTransactions by viewModel.financialTransactions.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = DarkBlueColorScheme) {
        DashboardContent(
            user = user,
            authState = authState,
            counts = counts,
            memberships = memberships,
            loanDeposits = loanDeposits,
            monthlyDues = monthlyDues,
            financialTransactions = financialTransactions,
            history = history,
            message = message,
            onSignOut = onSignOut,
            onResendVerification = onResendVerification,
            onSaveMembership = viewModel::saveMembership,
            onDeleteMembership = viewModel::deleteMembership,
            onSaveLoanDeposit = viewModel::saveLoanDeposit,
            onDeleteLoanDeposit = viewModel::deleteLoanDeposit,
            onSaveFinancialTransaction = viewModel::saveFinancialTransaction,
            onSaveMonthlyDue = viewModel::saveMonthlyDue,
            onDeleteMonthlyDue = viewModel::deleteMonthlyDue,
            onClearHistory = viewModel::clearHistory,
            onMessageShown = viewModel::messageShown,
            modifier = modifier,
        )
    }
}

@Composable
fun DashboardContent(
    user: AuthUser,
    authState: AuthUiState,
    counts: RecordCounts,
    memberships: List<MembershipEntity>,
    loanDeposits: List<LoanDepositEntity>,
    monthlyDues: List<MonthlyDueEntity>,
    financialTransactions: List<FinancialTransactionEntity>,
    history: List<HistoryEntity>,
    message: String?,
    onSignOut: () -> Unit,
    onResendVerification: () -> Unit,
    onSaveMembership: (
        MembershipEntity?,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
    ) -> Unit,
    onDeleteMembership: (MembershipEntity) -> Unit,
    onSaveLoanDeposit: (LoanDepositEntity?, Long, Boolean, String, Double, Double, Double, String) -> Unit,
    onDeleteLoanDeposit: (LoanDepositEntity) -> Unit,
    onSaveFinancialTransaction: (FinancialTransactionEntity) -> Unit,
    onSaveMonthlyDue: (MonthlyDueEntity?, Long, String, String, Double, Double, String) -> Unit,
    onDeleteMonthlyDue: (MonthlyDueEntity) -> Unit,
    onClearHistory: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var sectionName by rememberSaveable { mutableStateOf(DashboardSection.DASHBOARD.name) }
    val section = DashboardSection.entries.firstOrNull { it.name == sectionName }
        ?: DashboardSection.DASHBOARD

    fun select(target: DashboardSection) {
        sectionName = target.name
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    BackHandler(enabled = drawerState.isOpen || section != DashboardSection.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            sectionName = DashboardSection.DASHBOARD.name
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usePermanentDrawer = maxWidth >= 900.dp

        val screen: @Composable () -> Unit = {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    DashboardTopBar(
                        section = section,
                        showMenu = !usePermanentDrawer,
                        user = user,
                        onMenu = { scope.launch { drawerState.open() } },
                        onProfile = { select(DashboardSection.ABOUT) },
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    when (section) {
                        DashboardSection.DASHBOARD -> DashboardHome(
                            user = user,
                            authState = authState,
                            counts = counts,
                            onOpenSection = { select(it) },
                            onResendVerification = onResendVerification,
                        )

                        DashboardSection.MEMBERSHIP -> MembershipSection(
                            memberships = memberships,
                            onSave = onSaveMembership,
                            onDelete = onDeleteMembership,
                        )

                        DashboardSection.LOAN_DEPOSIT -> LoanDepositSection(
                            loanDeposits = loanDeposits,
                            memberships = memberships,
                            monthlyDues = monthlyDues,
                            financialTransactions = financialTransactions,
                            onSave = onSaveLoanDeposit,
                            onSaveTransaction = onSaveFinancialTransaction,
                            onDelete = onDeleteLoanDeposit,
                        )

                        DashboardSection.MONTHLY_DUE -> MonthlyDueSection(
                            monthlyDues = monthlyDues,
                            memberships = memberships,
                            loans = loanDeposits.filter { it.isLoan },
                            financialTransactions = financialTransactions,
                            onSave = onSaveMonthlyDue,
                            onSaveTransaction = onSaveFinancialTransaction,
                            onDelete = onDeleteMonthlyDue,
                        )

                        DashboardSection.ABOUT -> AboutSection(user = user, counts = counts)

                        DashboardSection.HISTORY -> HistorySection(
                            history = history,
                            onClear = onClearHistory,
                        )
                    }
                }
            }
        }

        if (usePermanentDrawer) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.width(300.dp),
                    ) {
                        DrawerBody(
                            user = user,
                            selected = section,
                            counts = counts,
                            onSelect = { select(it) },
                            onSignOut = onSignOut,
                        )
                    }
                },
                content = screen,
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.widthIn(max = 320.dp),
                    ) {
                        DrawerBody(
                            user = user,
                            selected = section,
                            counts = counts,
                            onSelect = { select(it) },
                            onSignOut = onSignOut,
                        )
                    }
                },
                content = screen,
            )
        }
    }
}

@Composable
private fun DashboardTopBar(
    section: DashboardSection,
    showMenu: Boolean,
    user: AuthUser,
    onMenu: () -> Unit,
    onProfile: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(62.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showMenu) {
                IconButton(onClick = onMenu) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Open navigation",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = section.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.weight(1f))

            LanguageToggle()

            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(38.dp)
                    .clickable(onClick = onProfile),
                shape = CircleShape,
                color = AuthPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.initial,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerBody(
    user: AuthUser,
    selected: DashboardSection,
    counts: RecordCounts,
    onSelect: (DashboardSection) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_leaf),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CCMS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Community records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AuthPrimary.copy(alpha = 0.07f),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(AuthPrimary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = user.initial,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.friendlyName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = user.email ?: "Signed in",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (user.provider == AuthProvider.GOOGLE) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = "Signed in with Google",
                        tint = RecordLoanDeposit,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "MENU",
            modifier = Modifier.padding(horizontal = 14.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        val itemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = AuthPrimary.copy(alpha = 0.12f),
            selectedIconColor = AuthPrimary,
            selectedTextColor = AuthPrimary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
        )

        DashboardSection.entries.forEach { item ->
            val badge = when (item) {
                DashboardSection.MEMBERSHIP -> counts.memberships
                DashboardSection.LOAN_DEPOSIT -> counts.loanDeposits
                DashboardSection.MONTHLY_DUE -> counts.monthlyDues
                else -> null
            }
            NavigationDrawerItem(
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = if (item == selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                        )
                        if (badge != null) {
                            Surface(
                                color = if (item == selected) {
                                    AuthPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = badge.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item == selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                },
                selected = item == selected,
                onClick = { onSelect(item) },
                colors = itemColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 1.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(text = "Logout", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            },
            selected = false,
            onClick = onSignOut,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = Color(0xFFF87171),
                unselectedTextColor = Color(0xFFF87171),
            ),
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Records are stored on this device",
            modifier = Modifier.padding(horizontal = 14.dp),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DashboardHome(
    user: AuthUser,
    authState: AuthUiState,
    counts: RecordCounts,
    onOpenSection: (DashboardSection) -> Unit,
    onResendVerification: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            WelcomeCard(user = user)
        }

        if (user.provider == AuthProvider.PASSWORD && !user.isEmailVerified) {
            item {
                Column {
                    MessageBanner(
                        message = "Your email is not verified yet. Check your inbox for the " +
                            "verification link.",
                        isError = false,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = onResendVerification,
                            enabled = !authState.isSubmitting,
                        ) {
                            Text(text = "Resend verification email", color = AuthPrimary)
                        }
                    }
                }
            }
        }

        authState.infoMessage?.let { message ->
            item {
                MessageBanner(message = message, isError = false)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SectionLabel(text = "Overview")
                Text(
                    text = if (counts.total == 1) "1 record" else "${counts.total} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            CountCard(
                label = "Membership",
                subtitle = "Registered members",
                count = counts.memberships,
                color = RecordMembership,
                icon = Icons.Outlined.Groups,
                onClick = { onOpenSection(DashboardSection.MEMBERSHIP) },
            )
        }

        item {
            CountCard(
                label = "Loan or Deposit",
                subtitle = "Money lent or deposited",
                count = counts.loanDeposits,
                color = RecordLoanDeposit,
                icon = Icons.Outlined.AccountBalanceWallet,
                onClick = { onOpenSection(DashboardSection.LOAN_DEPOSIT) },
            )
        }

        item {
            CountCard(
                label = "Monthly Due",
                subtitle = "Monthly contribution entries",
                count = counts.monthlyDues,
                color = RecordMonthlyDue,
                icon = Icons.Outlined.CalendarMonth,
                onClick = { onOpenSection(DashboardSection.MONTHLY_DUE) },
            )
        }

        item {
            RecordsPieChartCard(counts = counts)
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            SectionLabel(text = "More")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BottomLinkCard(
                    title = "About App",
                    subtitle = "Version & data",
                    icon = Icons.Outlined.Info,
                    color = AuthPrimary,
                    onClick = { onOpenSection(DashboardSection.ABOUT) },
                    modifier = Modifier.weight(1f),
                )
                BottomLinkCard(
                    title = "History",
                    subtitle = "Recent activity",
                    icon = Icons.Outlined.History,
                    color = RecordLoanDeposit,
                    onClick = { onOpenSection(DashboardSection.HISTORY) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard(user: AuthUser) {
    val today = remember {
        SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date())
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AuthPrimary, AuthPrimaryDark),
                    ),
                    shape = RoundedCornerShape(22.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = today,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hello, ${user.friendlyName}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Here is the latest snapshot of your community records.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user.provider == AuthProvider.GOOGLE) {
                                "Signed in with Google"
                            } else {
                                "Signed in with email"
                            },
                            fontSize = 12.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountCard(
    label: String,
    subtitle: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = count.toString(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = if (count == 1) "record" else "records",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BottomLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    MaterialTheme(colorScheme = DarkBlueColorScheme) {
        DashboardContent(
            user = AuthUser(
                uid = "preview-user",
                email = "alex@example.com",
                displayName = "Alex Morgan",
                photoUrl = null,
                isEmailVerified = true,
                provider = AuthProvider.PASSWORD,
            ),
            authState = AuthUiState(
                isInitializing = false,
                isConfigured = true,
            ),
            counts = RecordCounts(
                memberships = 12,
                loanDeposits = 5,
                monthlyDues = 8,
            ),
            memberships = emptyList(),
            loanDeposits = emptyList(),
            monthlyDues = emptyList(),
            financialTransactions = emptyList(),
            history = emptyList(),
            message = null,
            onSignOut = {},
            onResendVerification = {},
            onSaveMembership = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onDeleteMembership = {},
            onSaveLoanDeposit = { _, _, _, _, _, _, _, _ -> },
            onDeleteLoanDeposit = {},
            onSaveFinancialTransaction = {},
            onSaveMonthlyDue = { _, _, _, _, _, _, _ -> },
            onDeleteMonthlyDue = {},
            onClearHistory = {},
            onMessageShown = {},
        )
    }
}
