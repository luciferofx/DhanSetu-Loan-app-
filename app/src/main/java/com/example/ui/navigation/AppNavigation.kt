package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.auth.LoginScreen
import com.example.ui.kyc.KYCScreen
import com.example.ui.loan.ApplicationSummaryScreen
import com.example.ui.loan.DisbursementTrackingScreen
import com.example.ui.loan.LoanEligibilityCalcScreen
import com.example.ui.repayment.RepaymentDashboardScreen
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.LoanViewModel
import com.example.ui.viewmodel.UiEvent

enum class ScreenRoute(val title: String) {
    AUTH("Login & Auth"),
    CALCULATOR("Eligibility Calculator"),
    SUMMARY("Application & Summary"),
    TRACKING("Disbursement Pipeline"),
    REPAYMENT("Repay & Schedule"),
    KYC("Profile & KYC")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ScreenRoute.CALCULATOR) }
    var trackingLoanId by remember { mutableStateOf("LOAN-DEMO-01") }
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeLoan by viewModel.activeLoan.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.LoanSubmittedSuccess -> {
                    trackingLoanId = event.loanId
                    snackbarHostState.showSnackbar("Loan submitted! Tracking disbursement...")
                }
                is UiEvent.PaymentSuccess -> {
                    snackbarHostState.showSnackbar("Payment of $${event.amount} recorded!")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Instant Loan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentScreen.title,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (userProfile != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Limit $${userProfile?.maxEligibleAmount?.toInt() ?: 8000}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Calculator
                NavigationBarItem(
                    selected = currentScreen == ScreenRoute.CALCULATOR || currentScreen == ScreenRoute.SUMMARY,
                    onClick = { currentScreen = ScreenRoute.CALCULATOR },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == ScreenRoute.CALCULATOR) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                            contentDescription = "Calculator"
                        )
                    },
                    label = { Text("Calculate", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_calculator"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 2: Disbursement Tracking
                NavigationBarItem(
                    selected = currentScreen == ScreenRoute.TRACKING,
                    onClick = { currentScreen = ScreenRoute.TRACKING },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == ScreenRoute.TRACKING) Icons.Filled.HourglassTop else Icons.Outlined.HourglassTop,
                            contentDescription = "Disbursement Tracking"
                        )
                    },
                    label = { Text("Tracking", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_tracking"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 3: Repayment Dashboard
                NavigationBarItem(
                    selected = currentScreen == ScreenRoute.REPAYMENT,
                    onClick = { currentScreen = ScreenRoute.REPAYMENT },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == ScreenRoute.REPAYMENT) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                            contentDescription = "Repayment"
                        )
                    },
                    label = { Text("Repayment", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_repayment"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 4: KYC & Profile
                NavigationBarItem(
                    selected = currentScreen == ScreenRoute.KYC,
                    onClick = { currentScreen = ScreenRoute.KYC },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == ScreenRoute.KYC) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "KYC Profile"
                        )
                    },
                    label = { Text("KYC", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_kyc"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_crossfade"
        ) { screen ->
            when (screen) {
                ScreenRoute.AUTH -> LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { currentScreen = ScreenRoute.CALCULATOR }
                )
                ScreenRoute.CALCULATOR -> LoanEligibilityCalcScreen(
                    viewModel = viewModel,
                    onProceedToSummary = { currentScreen = ScreenRoute.SUMMARY }
                )
                ScreenRoute.SUMMARY -> ApplicationSummaryScreen(
                    viewModel = viewModel,
                    onNavToTracking = { id ->
                        trackingLoanId = id
                        currentScreen = ScreenRoute.TRACKING
                    }
                )
                ScreenRoute.TRACKING -> DisbursementTrackingScreen(
                    viewModel = viewModel,
                    loanId = trackingLoanId,
                    onNavigateToRepayment = { currentScreen = ScreenRoute.REPAYMENT }
                )
                ScreenRoute.REPAYMENT -> RepaymentDashboardScreen(
                    viewModel = viewModel,
                    onApplyNewLoan = { currentScreen = ScreenRoute.CALCULATOR }
                )
                ScreenRoute.KYC -> KYCScreen(
                    viewModel = viewModel,
                    onProceedToCalculator = { currentScreen = ScreenRoute.CALCULATOR }
                )
            }
        }
    }
}
