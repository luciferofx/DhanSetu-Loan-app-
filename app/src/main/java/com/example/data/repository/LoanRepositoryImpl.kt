package com.example.data.repository

import com.example.data.local.EmiItemEntity
import com.example.data.local.LoanAppDao
import com.example.data.local.LoanApplicationEntity
import com.example.data.local.TransactionRecordEntity
import com.example.data.local.UserProfileEntity
import com.example.domain.model.BankAccount
import com.example.domain.model.EmiItem
import com.example.domain.model.EmploymentType
import com.example.domain.model.KycStatus
import com.example.domain.model.LoanApplication
import com.example.domain.model.LoanStatus
import com.example.domain.model.TransactionRecord
import com.example.domain.model.UserProfile
import com.example.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LoanRepositoryImpl(
    private val dao: LoanAppDao
) : LoanRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun getUserProfile(): Flow<UserProfile?> {
        return dao.getUserProfile().map { it?.toDomain() }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertOrUpdateProfile(UserProfileEntity.fromDomain(profile))
    }

    override fun getAllLoans(): Flow<List<LoanApplication>> {
        return dao.getAllLoanApplications().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveLoan(): Flow<LoanApplication?> {
        return dao.getActiveLoan().map { it?.toDomain() }
    }

    override fun getLoanById(id: String): Flow<LoanApplication?> {
        return dao.getLoanApplicationById(id).map { it?.toDomain() }
    }

    override suspend fun createLoanApplication(loan: LoanApplication, emis: List<EmiItem>) {
        dao.insertLoanApplication(LoanApplicationEntity.fromDomain(loan))
        dao.insertEmis(emis.map { EmiItemEntity.fromDomain(it) })
    }

    override suspend fun updateLoanApplication(loan: LoanApplication) {
        dao.updateLoanApplication(LoanApplicationEntity.fromDomain(loan))
    }

    override fun getEmisForLoan(loanId: String): Flow<List<EmiItem>> {
        return dao.getEmisForLoan(loanId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun payEmi(emi: EmiItem, paymentMethod: String): Result<TransactionRecord> {
        val now = dateFormat.format(Date())
        val refId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val updatedEmi = emi.copy(
            isPaid = true,
            paidAt = now,
            transactionRef = refId
        )
        dao.updateEmi(EmiItemEntity.fromDomain(updatedEmi))

        val txn = TransactionRecord(
            id = UUID.randomUUID().toString(),
            loanId = emi.loanId,
            title = "EMI #${emi.emiNumber} Payment",
            amount = emi.amount,
            type = "EMI_PAYMENT",
            date = now,
            status = "SUCCESS",
            refNumber = refId,
            note = "Paid via $paymentMethod"
        )
        dao.insertTransaction(TransactionRecordEntity.fromDomain(txn))

        // Check if all EMIs for this loan are paid
        val remainingEmis = dao.getEmisForLoan(emi.loanId).firstOrNull() ?: emptyList()
        val allPaid = remainingEmis.all { it.isPaid || it.id == emi.id }
        if (allPaid) {
            val loanEntity = dao.getLoanApplicationById(emi.loanId).firstOrNull()
            if (loanEntity != null) {
                dao.updateLoanApplication(loanEntity.copy(status = LoanStatus.CLOSED.name))
            }
        }

        return Result.success(txn)
    }

    override fun getAllTransactions(): Flow<List<TransactionRecord>> {
        return dao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordTransaction(transaction: TransactionRecord) {
        dao.insertTransaction(TransactionRecordEntity.fromDomain(transaction))
    }

    override suspend fun advanceDisbursementStep(loanId: String, currentLoan: LoanApplication): LoanApplication {
        val nextStep = (currentLoan.disbursementStep + 1).coerceAtMost(5)
        val newStatus = when (nextStep) {
            1 -> LoanStatus.SUBMITTED
            2 -> LoanStatus.KYC_VERIFIED
            3 -> LoanStatus.UNDERWRITING_APPROVED
            4 -> LoanStatus.UNDERWRITING_APPROVED
            5 -> LoanStatus.DISBURSED
            else -> LoanStatus.ACTIVE
        }

        val updatedLoan = currentLoan.copy(
            disbursementStep = nextStep,
            status = newStatus,
            disbursementDate = if (nextStep == 5) dateFormat.format(Date()) else currentLoan.disbursementDate
        )

        dao.updateLoanApplication(LoanApplicationEntity.fromDomain(updatedLoan))

        if (nextStep == 5) {
            // Create disbursement transaction record
            val txn = TransactionRecord(
                id = UUID.randomUUID().toString(),
                loanId = updatedLoan.id,
                title = "Loan Disbursement Transfer",
                amount = updatedLoan.netDisbursementAmount,
                type = "DISBURSEMENT",
                date = dateFormat.format(Date()),
                status = "SUCCESS",
                refNumber = "UTR-${UUID.randomUUID().toString().take(10).uppercase()}",
                note = "Credited to ${updatedLoan.bankAccount.bankName} (${updatedLoan.bankAccount.accountNumber})"
            )
            dao.insertTransaction(TransactionRecordEntity.fromDomain(txn))
        }

        return updatedLoan
    }

    override suspend fun populateInitialDataIfEmpty() {
        val existingProfile = dao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            val initialProfile = UserProfile(
                id = "USER-101",
                fullName = "Alex Morgan",
                phone = "+1 (555) 019-2834",
                email = "alex.morgan@workmail.com",
                panNumber = "ABCDE1234F",
                aadhaarNumber = "XXXX-XXXX-8921",
                employmentType = EmploymentType.SALARIED,
                monthlyIncome = 4800.0,
                companyName = "Apex Tech Innovations",
                kycStatus = KycStatus.VERIFIED,
                creditScore = 768,
                maxEligibleAmount = 8000.0
            )
            dao.insertOrUpdateProfile(UserProfileEntity.fromDomain(initialProfile))
        }

        val existingLoans = dao.getAllLoanApplications().firstOrNull()
        if (existingLoans.isNullOrEmpty()) {
            val initialLoan = LoanApplication(
                id = "LOAN-DEMO-01",
                applicationRef = "APP-8942",
                principalAmount = 3500.0,
                tenureMonths = 6,
                interestRateAnnual = 12.0,
                monthlyEmi = 604.12,
                totalInterest = 124.72,
                totalRepayment = 3624.72,
                processingFee = 52.50,
                netDisbursementAmount = 3447.50,
                status = LoanStatus.ACTIVE,
                appliedDate = "2026-07-15 11:30",
                disbursementDate = "2026-07-15 11:45",
                bankAccount = BankAccount(
                    bankName = "Chase Commercial Bank",
                    accountNumber = "•••• •••• 4921",
                    ifscOrRouting = "CHASUS33XX",
                    holderName = "Alex Morgan",
                    isPennyDropVerified = true
                ),
                agreementSigned = true,
                signatureText = "Alex Morgan",
                emandateEnabled = true,
                disbursementStep = 5
            )

            val emis = listOf(
                EmiItem(
                    id = 1,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 1,
                    dueDate = "Aug 15, 2026",
                    amount = 604.12,
                    principalPart = 569.12,
                    interestPart = 35.00,
                    isPaid = true,
                    paidAt = "2026-08-14 09:20",
                    transactionRef = "TXN-894120A"
                ),
                EmiItem(
                    id = 2,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 2,
                    dueDate = "Sep 15, 2026",
                    amount = 604.12,
                    principalPart = 574.81,
                    interestPart = 29.31,
                    isPaid = false
                ),
                EmiItem(
                    id = 3,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 3,
                    dueDate = "Oct 15, 2026",
                    amount = 604.12,
                    principalPart = 580.56,
                    interestPart = 23.56,
                    isPaid = false
                ),
                EmiItem(
                    id = 4,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 4,
                    dueDate = "Nov 15, 2026",
                    amount = 604.12,
                    principalPart = 586.37,
                    interestPart = 17.75,
                    isPaid = false
                ),
                EmiItem(
                    id = 5,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 5,
                    dueDate = "Dec 15, 2026",
                    amount = 604.12,
                    principalPart = 592.23,
                    interestPart = 11.89,
                    isPaid = false
                ),
                EmiItem(
                    id = 6,
                    loanId = "LOAN-DEMO-01",
                    emiNumber = 6,
                    dueDate = "Jan 15, 2027",
                    amount = 604.12,
                    principalPart = 598.15,
                    interestPart = 5.97,
                    isPaid = false
                )
            )

            val initialTxns = listOf(
                TransactionRecord(
                    id = "TXN-01",
                    loanId = "LOAN-DEMO-01",
                    title = "Disbursement to Bank Account",
                    amount = 3447.50,
                    type = "DISBURSEMENT",
                    date = "2026-07-15 11:45",
                    status = "SUCCESS",
                    refNumber = "UTR-9832478129",
                    note = "Funds transferred to Chase (•••• 4921)"
                ),
                TransactionRecord(
                    id = "TXN-02",
                    loanId = "LOAN-DEMO-01",
                    title = "EMI #1 Payment",
                    amount = 604.12,
                    type = "EMI_PAYMENT",
                    date = "2026-08-14 09:20",
                    status = "SUCCESS",
                    refNumber = "TXN-894120A",
                    note = "Auto-debit mandate processed"
                )
            )

            dao.insertLoanApplication(LoanApplicationEntity.fromDomain(initialLoan))
            dao.insertEmis(emis.map { EmiItemEntity.fromDomain(it) })
            initialTxns.forEach { dao.insertTransaction(TransactionRecordEntity.fromDomain(it)) }
        }
    }

    override suspend fun resetDemoData() {
        dao.clearAllLoans()
        dao.clearAllEmis()
        dao.clearAllTransactions()
        populateInitialDataIfEmpty()
    }
}
