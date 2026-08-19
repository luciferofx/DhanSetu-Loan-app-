package com.example.domain.usecase

import com.example.domain.model.EmiItem
import com.example.domain.model.EmploymentType
import com.example.domain.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

data class LoanCalculationResult(
    val principal: Double,
    val tenureMonths: Int,
    val annualInterestRate: Double,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
    val processingFee: Double,
    val netDisbursement: Double,
    val maxEligibleAmount: Double,
    val isEligible: Boolean,
    val riskLevel: String, // "Low Risk", "Moderate", "High"
    val amortizedEmis: List<EmiItem>
)

class CalculateEligibilityUseCase {

    fun execute(
        principal: Double,
        tenureMonths: Int,
        userProfile: UserProfile?,
        customRate: Double? = null
    ): LoanCalculationResult {
        val income = userProfile?.monthlyIncome ?: 4000.0
        val creditScore = userProfile?.creditScore ?: 750

        // Determine base rate based on credit score & employment
        val baseRate = customRate ?: when {
            creditScore >= 780 -> 10.5
            creditScore >= 720 -> 12.0
            creditScore >= 650 -> 14.5
            else -> 17.5
        }

        // Determine maximum eligibility (salaried gets up to 3x monthly income, gig worker 2x)
        val incomeMultiplier = when (userProfile?.employmentType) {
            EmploymentType.SALARIED -> 2.5
            EmploymentType.GIG_WORKER -> 1.8
            EmploymentType.SELF_EMPLOYED -> 2.0
            null -> 2.0
        }
        val maxEligible = (income * incomeMultiplier).coerceIn(1000.0, 15000.0)
        val isEligible = principal <= maxEligible && creditScore >= 600

        val riskLevel = when {
            creditScore >= 750 && principal <= maxEligible * 0.7 -> "Low Risk (Instant Approval)"
            creditScore >= 680 && principal <= maxEligible -> "Moderate Risk (Standard Review)"
            else -> "Elevated Risk (Enhanced Verification)"
        }

        // EMI Calculation Formula: P * r * (1+r)^n / ((1+r)^n - 1)
        val monthlyRate = (baseRate / 12.0) / 100.0
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val monthlyEmi = if (monthlyRate > 0) {
            (principal * monthlyRate * factor) / (factor - 1.0)
        } else {
            principal / tenureMonths
        }

        val roundedEmi = (monthlyEmi * 100.0).roundToInt() / 100.0
        val totalRepayment = (roundedEmi * tenureMonths * 100.0).roundToInt() / 100.0
        val totalInterest = ((totalRepayment - principal) * 100.0).roundToInt() / 100.0
        val processingFee = ((principal * 0.015).coerceAtLeast(25.0) * 100.0).roundToInt() / 100.0
        val netDisbursement = ((principal - processingFee) * 100.0).roundToInt() / 100.0

        // Generate Amortization schedule
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        var remainingPrincipal = principal
        val emis = mutableListOf<EmiItem>()

        for (i in 1..tenureMonths) {
            cal.add(Calendar.MONTH, 1)
            val interestPart = ((remainingPrincipal * monthlyRate) * 100.0).roundToInt() / 100.0
            val principalPart = ((roundedEmi - interestPart) * 100.0).roundToInt() / 100.0
            remainingPrincipal = (remainingPrincipal - principalPart).coerceAtLeast(0.0)

            emis.add(
                EmiItem(
                    id = i,
                    loanId = "",
                    emiNumber = i,
                    dueDate = dateFormat.format(cal.time),
                    amount = roundedEmi,
                    principalPart = principalPart,
                    interestPart = interestPart,
                    isPaid = false
                )
            )
        }

        return LoanCalculationResult(
            principal = principal,
            tenureMonths = tenureMonths,
            annualInterestRate = baseRate,
            monthlyEmi = roundedEmi,
            totalInterest = totalInterest,
            totalRepayment = totalRepayment,
            processingFee = processingFee,
            netDisbursement = netDisbursement,
            maxEligibleAmount = maxEligible,
            isEligible = isEligible,
            riskLevel = riskLevel,
            amortizedEmis = emis
        )
    }
}
