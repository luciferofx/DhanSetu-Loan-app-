package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanAppDao {

    // User Profile
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // Loan Applications
    @Query("SELECT * FROM loan_applications ORDER BY appliedDate DESC")
    fun getAllLoanApplications(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE id = :id LIMIT 1")
    fun getLoanApplicationById(id: String): Flow<LoanApplicationEntity?>

    @Query("SELECT * FROM loan_applications WHERE status IN ('ACTIVE', 'SUBMITTED', 'KYC_VERIFIED', 'UNDERWRITING_APPROVED', 'DISBURSED') ORDER BY appliedDate DESC LIMIT 1")
    fun getActiveLoan(): Flow<LoanApplicationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanApplication(loan: LoanApplicationEntity)

    @Update
    suspend fun updateLoanApplication(loan: LoanApplicationEntity)

    // EMI Items
    @Query("SELECT * FROM emi_items WHERE loanId = :loanId ORDER BY emiNumber ASC")
    fun getEmisForLoan(loanId: String): Flow<List<EmiItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmis(emis: List<EmiItemEntity>)

    @Update
    suspend fun updateEmi(emi: EmiItemEntity)

    // Transactions
    @Query("SELECT * FROM transaction_records ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionRecordEntity>>

    @Query("SELECT * FROM transaction_records WHERE loanId = :loanId ORDER BY date DESC")
    fun getTransactionsForLoan(loanId: String): Flow<List<TransactionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecordEntity)

    @Query("DELETE FROM loan_applications")
    suspend fun clearAllLoans()

    @Query("DELETE FROM emi_items")
    suspend fun clearAllEmis()

    @Query("DELETE FROM transaction_records")
    suspend fun clearAllTransactions()
}
