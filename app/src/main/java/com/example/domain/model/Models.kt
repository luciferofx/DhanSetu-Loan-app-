package com.example.domain.model

enum class KycStatus {
    NOT_STARTED,
    IN_PROGRESS,
    VERIFIED,
    REJECTED
}

enum class EmploymentType(val label: String) {
    SALARIED("Salaried Employee"),
    GIG_WORKER("Gig Worker / Freelancer"),
    SELF_EMPLOYED("Self-Employed / Business")
}

data class UserProfile(
    val id: String = "USER-101",
    val fullName: String = "Alex Morgan",
    val phone: String = "+1 (555) 234-5678",
    val email: String = "alex.morgan@workmail.com",
    val panNumber: String = "ABCDE1234F",
    val aadhaarNumber: String = "XXXX-XXXX-8921",
    val employmentType: EmploymentType = EmploymentType.SALARIED,
    val monthlyIncome: Double = 4500.0,
    val companyName: String = "Apex Tech Innovations",
    val kycStatus: KycStatus = KycStatus.VERIFIED,
    val creditScore: Int = 760,
    val maxEligibleAmount: Double = 8000.0
)

enum class LoanStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    KYC_VERIFIED("KYC Verified"),
    UNDERWRITING_APPROVED("Approved"),
    DISBURSED("Funds Disbursed"),
    ACTIVE("Active Repayment"),
    CLOSED("Closed / Repaid")
}

data class BankAccount(
    val bankName: String = "JPMorgan Chase / HDFC Bank",
    val accountNumber: String = "•••• •••• 9482",
    val ifscOrRouting: String = "CHASUS33 / HDFC0001234",
    val holderName: String = "Alex Morgan",
    val isPennyDropVerified: Boolean = true
)

data class EmiItem(
    val id: Int,
    val loanId: String,
    val emiNumber: Int,
    val dueDate: String,
    val amount: Double,
    val principalPart: Double,
    val interestPart: Double,
    val isPaid: Boolean = false,
    val paidAt: String? = null,
    val transactionRef: String? = null
)

data class LoanApplication(
    val id: String,
    val applicationRef: String,
    val principalAmount: Double,
    val tenureMonths: Int,
    val interestRateAnnual: Double,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
    val processingFee: Double,
    val netDisbursementAmount: Double,
    val status: LoanStatus,
    val appliedDate: String,
    val disbursementDate: String? = null,
    val bankAccount: BankAccount,
    val agreementSigned: Boolean = false,
    val signatureText: String? = null,
    val emandateEnabled: Boolean = true,
    val disbursementStep: Int = 1 // 1: Submitted, 2: KYC Verified, 3: Underwriting, 4: Penny Drop, 5: Disbursed
)

data class TransactionRecord(
    val id: String,
    val loanId: String,
    val title: String,
    val amount: Double,
    val type: String, // "DISBURSEMENT", "EMI_PAYMENT", "PENNY_DROP"
    val date: String,
    val status: String = "SUCCESS",
    val refNumber: String,
    val note: String
)
