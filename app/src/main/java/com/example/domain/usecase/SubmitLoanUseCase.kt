package com.example.domain.usecase

import com.example.domain.model.BankAccount
import com.example.domain.model.EmiItem
import com.example.domain.model.LoanApplication
import com.example.domain.model.LoanStatus
import com.example.domain.model.TransactionRecord
import com.example.domain.repository.LoanRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SubmitLoanUseCase(
    private val repository: LoanRepository
) {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    suspend fun execute(
        calcResult: LoanCalculationResult,
        bankAccount: BankAccount,
        signatureText: String,
        emandateConsent: Boolean
    ): Result<LoanApplication> {
        val loanId = "LOAN-${UUID.randomUUID().toString().take(6).uppercase()}"
        val refCode = "APP-${(1000..9999).random()}"
        val now = dateTimeFormat.format(Date())

        val emis = calcResult.amortizedEmis.map { emi ->
            emi.copy(loanId = loanId)
        }

        val application = LoanApplication(
            id = loanId,
            applicationRef = refCode,
            principalAmount = calcResult.principal,
            tenureMonths = calcResult.tenureMonths,
            interestRateAnnual = calcResult.annualInterestRate,
            monthlyEmi = calcResult.monthlyEmi,
            totalInterest = calcResult.totalInterest,
            totalRepayment = calcResult.totalRepayment,
            processingFee = calcResult.processingFee,
            netDisbursementAmount = calcResult.netDisbursement,
            status = LoanStatus.SUBMITTED,
            appliedDate = now,
            disbursementDate = null,
            bankAccount = bankAccount,
            agreementSigned = true,
            signatureText = signatureText,
            emandateEnabled = emandateConsent,
            disbursementStep = 1
        )

        repository.createLoanApplication(application, emis)

        // Record initial Penny drop transaction to verify bank
        val pennyDropTxn = TransactionRecord(
            id = UUID.randomUUID().toString(),
            loanId = loanId,
            title = "Bank Account Penny-Drop Verification",
            amount = 1.00,
            type = "PENNY_DROP",
            date = now,
            status = "SUCCESS",
            refNumber = "REF-PD-${UUID.randomUUID().toString().take(6).uppercase()}",
            note = "Credited $1.00 to verify ${bankAccount.bankName} (${bankAccount.accountNumber})"
        )
        repository.recordTransaction(pennyDropTxn)

        return Result.success(application)
    }
}
