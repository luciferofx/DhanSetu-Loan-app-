package com.example.domain.repository

import com.example.domain.model.EmiItem
import com.example.domain.model.LoanApplication
import com.example.domain.model.TransactionRecord
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)

    fun getAllLoans(): Flow<List<LoanApplication>>
    fun getActiveLoan(): Flow<LoanApplication?>
    fun getLoanById(id: String): Flow<LoanApplication?>
    suspend fun createLoanApplication(loan: LoanApplication, emis: List<EmiItem>)
    suspend fun updateLoanApplication(loan: LoanApplication)

    fun getEmisForLoan(loanId: String): Flow<List<EmiItem>>
    suspend fun payEmi(emi: EmiItem, paymentMethod: String): Result<TransactionRecord>

    fun getAllTransactions(): Flow<List<TransactionRecord>>
    suspend fun recordTransaction(transaction: TransactionRecord)

    suspend fun advanceDisbursementStep(loanId: String, currentLoan: LoanApplication): LoanApplication
    suspend fun populateInitialDataIfEmpty()
    suspend fun resetDemoData()
}
