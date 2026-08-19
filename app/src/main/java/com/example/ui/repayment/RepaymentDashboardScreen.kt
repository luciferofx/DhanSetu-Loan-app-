package com.example.ui.repayment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EmiItem
import com.example.domain.model.LoanStatus
import com.example.domain.model.TransactionRecord
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.GoldTertiaryDark
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun RepaymentDashboardScreen(
    viewModel: LoanViewModel,
    onApplyNewLoan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLoan by viewModel.activeLoan.collectAsStateWithLifecycle()
    val emis by viewModel.currentLoanEmis.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val repaymentState by viewModel.repaymentState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI / Google Pay") }

    val nextUnpaidEmi = emis.firstOrNull { !it.isPaid }
    val paidCount = emis.count { it.isPaid }
    val totalEmis = emis.size.coerceAtLeast(1)
    val remainingBalance = emis.filter { !it.isPaid }.sumOf { it.amount }
    val isLoanFullyClosed = activeLoan?.status == LoanStatus.CLOSED || (emis.isNotEmpty() && paidCount == emis.size)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Active Loan Financial Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isLoanFullyClosed) "Loan Status: Closed & Settled" else "Active Loan Repayment",
                            color = if (isLoanFullyClosed) SuccessGreen else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = activeLoan?.applicationRef ?: "APP-8942",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLoanFullyClosed) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isLoanFullyClosed) "NOC ISSUED" else "$paidCount / $totalEmis EMIs Paid",
                            color = if (isLoanFullyClosed) SuccessGreen else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Outstanding Balance", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = "$${String.format("%.2f", remainingBalance)}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Next Due Amount", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = if (nextUnpaidEmi != null) "$${nextUnpaidEmi.amount}" else "$0.00",
                            color = if (nextUnpaidEmi != null) PendingOrange else SuccessGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (nextUnpaidEmi != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PendingOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next EMI Due Date: ${nextUnpaidEmi.dueDate}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "Auto-Debit Active",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Pay Card (if next EMI is pending)
        if (nextUnpaidEmi != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pay Upcoming EMI #${nextUnpaidEmi.emiNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pay early to improve your Credit Bureau Score",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.selectEmiForPayment(nextUnpaidEmi) },
                        modifier = Modifier.testTag("pay_emi_quick_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Pay $${nextUnpaidEmi.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Action Chips (Statement, NOC, Apply New Loan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.toggleStatementDialog(true) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("statement_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Statement", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { viewModel.toggleNocDialog(true) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("noc_certificate_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NOC Letter", fontSize = 11.sp)
            }

            Button(
                onClick = onApplyNewLoan,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("apply_new_loan_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+ New Loan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: EMI Schedule vs Transaction History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("EMI Schedule (${emis.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Transactions (${transactions.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // EMI Amortization Schedule List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emis.forEach { emi ->
                    EmiItemCard(
                        emi = emi,
                        onPayClick = { viewModel.selectEmiForPayment(emi) }
                    )
                }

                if (emis.isEmpty()) {
                    Text(
                        text = "No active EMI schedule found. Apply for a loan to generate schedule.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Transaction History Timeline
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { txn ->
                    TransactionItemCard(txn = txn)
                }

                if (transactions.isEmpty()) {
                    Text(
                        text = "No transaction records logged yet.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Demo Data helper button
        OutlinedButton(
            onClick = { viewModel.resetData() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_demo_data_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Demo Loan Data for Testing", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Payment Gateway Bottom / Dialog Modal
    val emiToPay = repaymentState.selectedEmiForPayment
    if (emiToPay != null) {
        AlertDialog(
            onDismissRequest = { viewModel.selectEmiForPayment(null) },
            title = {
                Text(
                    text = "Pay EMI #${emiToPay.emiNumber} - $${emiToPay.amount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Due Date: ${emiToPay.dueDate} • Principal: $${emiToPay.principalPart} • Interest: $${emiToPay.interestPart}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Select Payment Gateway", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("UPI / Google Pay / PhonePe", "Debit Card / NetBanking", "Direct Auto-Debit / ACH").forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedPaymentMethod = method },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (method.contains("UPI")) Icons.Default.QrCode else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = method,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.processEmiPayment(selectedPaymentMethod) },
                    enabled = !repaymentState.isPaymentProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_payment_modal_button")
                ) {
                    if (repaymentState.isPaymentProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Processing...")
                    } else {
                        Text("Authorize Payment")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectEmiForPayment(null) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Payment Success Alert
    if (repaymentState.paymentSuccessDialogShown) {
        val lastTxn = repaymentState.lastPaidTransaction
        AlertDialog(
            onDismissRequest = { viewModel.dismissPaymentSuccessDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Payment Successful!", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${lastTxn?.amount ?: 604.12} has been credited toward your loan.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ref: ${lastTxn?.refNumber ?: "TXN-SUCCESS"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissPaymentSuccessDialog() }) {
                    Text("Done")
                }
            }
        )
    }

    // Statement Dialog
    if (repaymentState.statementDialogShown) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleStatementDialog(false) },
            title = { Text("Loan Account Statement", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Account: ${activeLoan?.bankAccount?.accountNumber ?: "•••• 4921"} • Loan Ref: ${activeLoan?.applicationRef ?: "APP-8942"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Total Sanctioned: $${activeLoan?.principalAmount ?: 3500.0}")
                    Text(text = "Net Disbursed: $${activeLoan?.netDisbursementAmount ?: 3447.50}")
                    Text(text = "Total Repaid: $${String.format("%.2f", emis.filter { it.isPaid }.sumOf { it.amount })}")
                    Text(text = "Remaining Principal: $${String.format("%.2f", remainingBalance)}")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.toggleStatementDialog(false) }) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF")
                }
            }
        )
    }

    // NOC Certificate Dialog
    if (repaymentState.nocCertificateDialogShown) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleNocDialog(false) },
            title = { Text("No Objection Certificate (NOC)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "This certificate certifies that loan account ${activeLoan?.applicationRef ?: "APP-8942"} for borrower Alex Morgan has no pending dues or liens and is in full compliance with lending standards.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Issued by RBI/NBFC Certified Lending Entity",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.toggleNocDialog(false) }) {
                    Text("Download Official NOC")
                }
            }
        )
    }
}

@Composable
private fun EmiItemCard(
    emi: EmiItem,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (emi.isPaid) SuccessGreen.copy(alpha = 0.15f) else PendingOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${emi.emiNumber}",
                        fontWeight = FontWeight.Bold,
                        color = if (emi.isPaid) SuccessGreen else PendingOrange,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Due: ${emi.dueDate}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Principal: $${emi.principalPart} • Int: $${emi.interestPart}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (emi.isPaid && emi.paidAt != null) {
                        Text(
                            text = "Paid on ${emi.paidAt} (${emi.transactionRef ?: ""})",
                            fontSize = 10.sp,
                            color = SuccessGreen
                        )
                    }
                }
            }

            if (emi.isPaid) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PAID ✓",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("pay_emi_item_${emi.emiNumber}")
                ) {
                    Text("Pay $${emi.amount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(txn: TransactionRecord) {
    val isDisbursement = txn.type == "DISBURSEMENT"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDisbursement) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDisbursement) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isDisbursement) SuccessGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = txn.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${txn.date} • ${txn.refNumber}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = txn.note,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isDisbursement) "+" else "-"} $${txn.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDisbursement) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
