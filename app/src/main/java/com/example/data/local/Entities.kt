package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.BankAccount
import com.example.domain.model.EmploymentType
import com.example.domain.model.KycStatus
import com.example.domain.model.LoanStatus
import com.example.domain.model.UserProfile
import com.example.domain.model.LoanApplication
import com.example.domain.model.EmiItem
import com.example.domain.model.TransactionRecord

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val panNumber: String,
    val aadhaarNumber: String,
    val employmentType: String,
    val monthlyIncome: Double,
    val companyName: String,
    val kycStatus: String,
    val creditScore: Int,
    val maxEligibleAmount: Double
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        panNumber = panNumber,
        aadhaarNumber = aadhaarNumber,
        employmentType = runCatching { EmploymentType.valueOf(employmentType) }.getOrDefault(EmploymentType.SALARIED),
        monthlyIncome = monthlyIncome,
        companyName = companyName,
        kycStatus = runCatching { KycStatus.valueOf(kycStatus) }.getOrDefault(KycStatus.VERIFIED),
        creditScore = creditScore,
        maxEligibleAmount = maxEligibleAmount
    )

    companion object {
        fun fromDomain(p: UserProfile): UserProfileEntity = UserProfileEntity(
            id = p.id,
            fullName = p.fullName,
            phone = p.phone,
            email = p.email,
            panNumber = p.panNumber,
            aadhaarNumber = p.aadhaarNumber,
            employmentType = p.employmentType.name,
            monthlyIncome = p.monthlyIncome,
            companyName = p.companyName,
            kycStatus = p.kycStatus.name,
            creditScore = p.creditScore,
            maxEligibleAmount = p.maxEligibleAmount
        )
    }
}

@Entity(tableName = "loan_applications")
data class LoanApplicationEntity(
    @PrimaryKey val id: String,
    val applicationRef: String,
    val principalAmount: Double,
    val tenureMonths: Int,
    val interestRateAnnual: Double,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
    val processingFee: Double,
    val netDisbursementAmount: Double,
    val status: String,
    val appliedDate: String,
    val disbursementDate: String?,
    val bankName: String,
    val accountNumber: String,
    val ifscOrRouting: String,
    val holderName: String,
    val isPennyDropVerified: Boolean,
    val agreementSigned: Boolean,
    val signatureText: String?,
    val emandateEnabled: Boolean,
    val disbursementStep: Int
) {
    fun toDomain(): LoanApplication = LoanApplication(
        id = id,
        applicationRef = applicationRef,
        principalAmount = principalAmount,
        tenureMonths = tenureMonths,
        interestRateAnnual = interestRateAnnual,
        monthlyEmi = monthlyEmi,
        totalInterest = totalInterest,
        totalRepayment = totalRepayment,
        processingFee = processingFee,
        netDisbursementAmount = netDisbursementAmount,
        status = runCatching { LoanStatus.valueOf(status) }.getOrDefault(LoanStatus.ACTIVE),
        appliedDate = appliedDate,
        disbursementDate = disbursementDate,
        bankAccount = BankAccount(
            bankName = bankName,
            accountNumber = accountNumber,
            ifscOrRouting = ifscOrRouting,
            holderName = holderName,
            isPennyDropVerified = isPennyDropVerified
        ),
        agreementSigned = agreementSigned,
        signatureText = signatureText,
        emandateEnabled = emandateEnabled,
        disbursementStep = disbursementStep
    )

    companion object {
        fun fromDomain(l: LoanApplication): LoanApplicationEntity = LoanApplicationEntity(
            id = l.id,
            applicationRef = l.applicationRef,
            principalAmount = l.principalAmount,
            tenureMonths = l.tenureMonths,
            interestRateAnnual = l.interestRateAnnual,
            monthlyEmi = l.monthlyEmi,
            totalInterest = l.totalInterest,
            totalRepayment = l.totalRepayment,
            processingFee = l.processingFee,
            netDisbursementAmount = l.netDisbursementAmount,
            status = l.status.name,
            appliedDate = l.appliedDate,
            disbursementDate = l.disbursementDate,
            bankName = l.bankAccount.bankName,
            accountNumber = l.bankAccount.accountNumber,
            ifscOrRouting = l.bankAccount.ifscOrRouting,
            holderName = l.bankAccount.holderName,
            isPennyDropVerified = l.bankAccount.isPennyDropVerified,
            agreementSigned = l.agreementSigned,
            signatureText = l.signatureText,
            emandateEnabled = l.emandateEnabled,
            disbursementStep = l.disbursementStep
        )
    }
}

@Entity(tableName = "emi_items")
data class EmiItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: String,
    val emiNumber: Int,
    val dueDate: String,
    val amount: Double,
    val principalPart: Double,
    val interestPart: Double,
    val isPaid: Boolean,
    val paidAt: String?,
    val transactionRef: String?
) {
    fun toDomain(): EmiItem = EmiItem(
        id = id,
        loanId = loanId,
        emiNumber = emiNumber,
        dueDate = dueDate,
        amount = amount,
        principalPart = principalPart,
        interestPart = interestPart,
        isPaid = isPaid,
        paidAt = paidAt,
        transactionRef = transactionRef
    )

    companion object {
        fun fromDomain(e: EmiItem): EmiItemEntity = EmiItemEntity(
            id = e.id,
            loanId = e.loanId,
            emiNumber = e.emiNumber,
            dueDate = e.dueDate,
            amount = e.amount,
            principalPart = e.principalPart,
            interestPart = e.interestPart,
            isPaid = e.isPaid,
            paidAt = e.paidAt,
            transactionRef = e.transactionRef
        )
    }
}

@Entity(tableName = "transaction_records")
data class TransactionRecordEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val title: String,
    val amount: Double,
    val type: String,
    val date: String,
    val status: String,
    val refNumber: String,
    val note: String
) {
    fun toDomain(): TransactionRecord = TransactionRecord(
        id = id,
        loanId = loanId,
        title = title,
        amount = amount,
        type = type,
        date = date,
        status = status,
        refNumber = refNumber,
        note = note
    )

    companion object {
        fun fromDomain(t: TransactionRecord): TransactionRecordEntity = TransactionRecordEntity(
            id = t.id,
            loanId = t.loanId,
            title = t.title,
            amount = t.amount,
            type = t.type,
            date = t.date,
            status = t.status,
            refNumber = t.refNumber,
            note = t.note
        )
    }
}
