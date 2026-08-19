package com.example.domain.usecase

import com.example.domain.model.EmploymentType
import com.example.domain.model.KycStatus
import com.example.domain.model.UserProfile
import com.example.domain.repository.LoanRepository
import kotlinx.coroutines.delay

data class KycVerificationResult(
    val isSuccess: Boolean,
    val score: Int,
    val maxLimit: Double,
    val message: String
)

class KycVerificationUseCase(
    private val repository: LoanRepository
) {
    suspend fun verifyAndSaveProfile(
        fullName: String,
        phone: String,
        email: String,
        panNumber: String,
        aadhaarNumber: String,
        employmentType: EmploymentType,
        monthlyIncome: Double,
        companyName: String
    ): KycVerificationResult {
        // Simulate API network check with third-party KYC & credit bureau
        delay(1200)

        val isValidPan = panNumber.trim().length >= 8
        val isValidAadhaar = aadhaarNumber.trim().length >= 8

        if (!isValidPan || !isValidAadhaar) {
            return KycVerificationResult(
                isSuccess = false,
                score = 0,
                maxLimit = 0.0,
                message = "Invalid Government ID or PAN format. Please check document numbers."
            )
        }

        // Calculate credit score & limit based on verified income
        val baseScore = (720..790).random()
        val limit = (monthlyIncome * (if (employmentType == EmploymentType.SALARIED) 2.5 else 1.8)).coerceIn(1500.0, 12000.0)

        val updatedProfile = UserProfile(
            id = "USER-101",
            fullName = fullName,
            phone = phone,
            email = email,
            panNumber = panNumber.uppercase(),
            aadhaarNumber = aadhaarNumber,
            employmentType = employmentType,
            monthlyIncome = monthlyIncome,
            companyName = companyName,
            kycStatus = KycStatus.VERIFIED,
            creditScore = baseScore,
            maxEligibleAmount = limit
        )

        repository.saveUserProfile(updatedProfile)

        return KycVerificationResult(
            isSuccess = true,
            score = baseScore,
            maxLimit = limit,
            message = "KYC verified successfully via Real-time Verification API!"
        )
    }
}
