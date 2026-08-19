package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BankAccount
import com.example.domain.model.EmiItem
import com.example.domain.model.EmploymentType
import com.example.domain.model.KycStatus
import com.example.domain.model.LoanApplication
import com.example.domain.model.LoanStatus
import com.example.domain.model.TransactionRecord
import com.example.domain.model.UserProfile
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.CalculateEligibilityUseCase
import com.example.domain.usecase.KycVerificationUseCase
import com.example.domain.usecase.LoanCalculationResult
import com.example.domain.usecase.SubmitLoanUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class LoanSubmittedSuccess(val loanId: String) : UiEvent
    data class PaymentSuccess(val amount: Double, val ref: String) : UiEvent
}

data class AuthUiState(
    val isLoggedIn: Boolean = true,
    val phoneNumber: String = "+1 (555) 019-2834",
    val otpCode: String = "4892",
    val isOtpSent: Boolean = false,
    val isVerifying: Boolean = false,
    val userEmail: String = "alex.morgan@workmail.com"
)

data class KycUiState(
    val fullName: String = "Alex Morgan",
    val phone: String = "+1 (555) 019-2834",
    val email: String = "alex.morgan@workmail.com",
    val panNumber: String = "ABCDE1234F",
    val aadhaarNumber: String = "9482-1204-8921",
    val employmentType: EmploymentType = EmploymentType.SALARIED,
    val monthlyIncomeText: String = "4800",
    val companyName: String = "Apex Tech Innovations",
    val panDocumentAttached: Boolean = true,
    val aadhaarDocumentAttached: Boolean = true,
    val selfieAttached: Boolean = true,
    val isVerifying: Boolean = false,
    val verificationError: String? = null,
    val verificationSuccessMessage: String? = null
)

data class CalculatorUiState(
    val selectedAmount: Double = 3500.0,
    val selectedTenureMonths: Int = 6,
    val calculationResult: LoanCalculationResult? = null
)

data class ApplicationSubmissionUiState(
    val bankName: String = "Chase Commercial Bank",
    val accountNumber: String = "•••• •••• 4921",
    val ifscOrRouting: String = "CHASUS33XX",
    val accountHolder: String = "Alex Morgan",
    val isPennyDropVerified: Boolean = true,
    val isVerifyingPennyDrop: Boolean = false,
    val signatureText: String = "Alex Morgan",
    val isAgreementChecked: Boolean = true,
    val isEmandateChecked: Boolean = true,
    val isSubmitting: Boolean = false
)

data class RepaymentUiState(
    val selectedEmiForPayment: EmiItem? = null,
    val isPaymentProcessing: Boolean = false,
    val paymentSuccessDialogShown: Boolean = false,
    val lastPaidTransaction: TransactionRecord? = null,
    val statementDialogShown: Boolean = false,
    val nocCertificateDialogShown: Boolean = false
)

class LoanViewModel(
    private val repository: LoanRepository,
    private val calculateEligibilityUseCase: CalculateEligibilityUseCase = CalculateEligibilityUseCase(),
    private val submitLoanUseCase: SubmitLoanUseCase = SubmitLoanUseCase(repository),
    private val kycVerificationUseCase: KycVerificationUseCase = KycVerificationUseCase(repository)
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // Auth State
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // KYC State
    private val _kycState = MutableStateFlow(KycUiState())
    val kycState: StateFlow<KycUiState> = _kycState.asStateFlow()

    // Calculator State
    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    // Submission State
    private val _submissionState = MutableStateFlow(ApplicationSubmissionUiState())
    val submissionState: StateFlow<ApplicationSubmissionUiState> = _submissionState.asStateFlow()

    // Repayment State
    private val _repaymentState = MutableStateFlow(RepaymentUiState())
    val repaymentState: StateFlow<RepaymentUiState> = _repaymentState.asStateFlow()

    // Database reactive streams
    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeLoan: StateFlow<LoanApplication?> = repository.getActiveLoan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLoans: StateFlow<List<LoanApplication>> = repository.getAllLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoanEmis: StateFlow<List<EmiItem>> = combine(activeLoan) { loans ->
        loans.firstOrNull()?.id
    }.let { loanIdFlow ->
        MutableStateFlow<List<EmiItem>>(emptyList())
    }

    private val _currentLoanEmis = MutableStateFlow<List<EmiItem>>(emptyList())
    val currentLoanEmis: StateFlow<List<EmiItem>> = _currentLoanEmis.asStateFlow()

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var emiObservationJob: Job? = null
    private var disbursementSimulationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
            recalculateLoan()
        }

        // Observe active loan to fetch its EMIs
        viewModelScope.launch {
            activeLoan.collect { loan ->
                emiObservationJob?.cancel()
                if (loan != null) {
                    emiObservationJob = viewModelScope.launch {
                        repository.getEmisForLoan(loan.id).collect { emis ->
                            _currentLoanEmis.value = emis
                        }
                    }
                } else {
                    _currentLoanEmis.value = emptyList()
                }
            }
        }
    }

    // --- Auth Actions ---
    fun onPhoneChanged(newPhone: String) {
        _authState.value = _authState.value.copy(phoneNumber = newPhone)
    }

    fun onOtpChanged(otp: String) {
        _authState.value = _authState.value.copy(otpCode = otp)
    }

    fun sendOtp() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isOtpSent = true)
            _eventFlow.emit(UiEvent.ShowToast("OTP sent to ${_authState.value.phoneNumber}: 4892"))
        }
    }

    fun verifyLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isVerifying = true)
            delay(800)
            _authState.value = _authState.value.copy(isVerifying = false, isLoggedIn = true)
            _eventFlow.emit(UiEvent.ShowToast("Logged in successfully!"))
            onSuccess()
        }
    }

    // --- KYC Actions ---
    fun updateKycField(
        fullName: String = _kycState.value.fullName,
        phone: String = _kycState.value.phone,
        email: String = _kycState.value.email,
        pan: String = _kycState.value.panNumber,
        aadhaar: String = _kycState.value.aadhaarNumber,
        employment: EmploymentType = _kycState.value.employmentType,
        income: String = _kycState.value.monthlyIncomeText,
        company: String = _kycState.value.companyName
    ) {
        _kycState.value = _kycState.value.copy(
            fullName = fullName,
            phone = phone,
            email = email,
            panNumber = pan,
            aadhaarNumber = aadhaar,
            employmentType = employment,
            monthlyIncomeText = income,
            companyName = company,
            verificationError = null
        )
    }

    fun toggleKycDocument(docType: String) {
        when (docType) {
            "pan" -> _kycState.value = _kycState.value.copy(panDocumentAttached = !_kycState.value.panDocumentAttached)
            "aadhaar" -> _kycState.value = _kycState.value.copy(aadhaarDocumentAttached = !_kycState.value.aadhaarDocumentAttached)
            "selfie" -> _kycState.value = _kycState.value.copy(selfieAttached = !_kycState.value.selfieAttached)
        }
    }

    fun submitKycVerification() {
        val state = _kycState.value
        val income = state.monthlyIncomeText.toDoubleOrNull() ?: 3000.0

        viewModelScope.launch {
            _kycState.value = _kycState.value.copy(isVerifying = true, verificationError = null)
            val result = kycVerificationUseCase.verifyAndSaveProfile(
                fullName = state.fullName,
                phone = state.phone,
                email = state.email,
                panNumber = state.panNumber,
                aadhaarNumber = state.aadhaarNumber,
                employmentType = state.employmentType,
                monthlyIncome = income,
                companyName = state.companyName
            )

            _kycState.value = _kycState.value.copy(
                isVerifying = false,
                verificationError = if (!result.isSuccess) result.message else null,
                verificationSuccessMessage = if (result.isSuccess) result.message else null
            )

            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowToast("Instant KYC Approved! Credit Score: ${result.score}"))
                recalculateLoan()
            }
        }
    }

    // --- Calculator Actions ---
    fun updateLoanAmount(amount: Double) {
        _calcState.value = _calcState.value.copy(selectedAmount = amount)
        recalculateLoan()
    }

    fun updateLoanTenure(tenure: Int) {
        _calcState.value = _calcState.value.copy(selectedTenureMonths = tenure)
        recalculateLoan()
    }

    private fun recalculateLoan() {
        val profile = userProfile.value
        val result = calculateEligibilityUseCase.execute(
            principal = _calcState.value.selectedAmount,
            tenureMonths = _calcState.value.selectedTenureMonths,
            userProfile = profile
        )
        _calcState.value = _calcState.value.copy(calculationResult = result)
    }

    // --- Submission Actions ---
    fun updateBankDetails(bank: String, acc: String, ifsc: String, holder: String) {
        _submissionState.value = _submissionState.value.copy(
            bankName = bank,
            accountNumber = acc,
            ifscOrRouting = ifsc,
            accountHolder = holder
        )
    }

    fun triggerPennyDropVerification() {
        viewModelScope.launch {
            _submissionState.value = _submissionState.value.copy(isVerifyingPennyDrop = true)
            delay(1200)
            _submissionState.value = _submissionState.value.copy(
                isVerifyingPennyDrop = false,
                isPennyDropVerified = true
            )
            _eventFlow.emit(UiEvent.ShowToast("Bank verified via $1.00 Penny-Drop deposit!"))
        }
    }

    fun updateSignature(sig: String) {
        _submissionState.value = _submissionState.value.copy(signatureText = sig)
    }

    fun toggleAgreement(checked: Boolean) {
        _submissionState.value = _submissionState.value.copy(isAgreementChecked = checked)
    }

    fun toggleEmandate(checked: Boolean) {
        _submissionState.value = _submissionState.value.copy(isEmandateChecked = checked)
    }

    fun submitLoanApplication(onNavToTracking: (String) -> Unit) {
        val calc = _calcState.value.calculationResult ?: return
        val sub = _submissionState.value

        viewModelScope.launch {
            _submissionState.value = _submissionState.value.copy(isSubmitting = true)
            val bank = BankAccount(
                bankName = sub.bankName,
                accountNumber = sub.accountNumber,
                ifscOrRouting = sub.ifscOrRouting,
                holderName = sub.accountHolder,
                isPennyDropVerified = sub.isPennyDropVerified
            )

            val result = submitLoanUseCase.execute(
                calcResult = calc,
                bankAccount = bank,
                signatureText = sub.signatureText.ifBlank { "Alex Morgan" },
                emandateConsent = sub.isEmandateChecked
            )

            _submissionState.value = _submissionState.value.copy(isSubmitting = false)

            result.onSuccess { loan ->
                _eventFlow.emit(UiEvent.LoanSubmittedSuccess(loan.id))
                onNavToTracking(loan.id)
                startAutoDisbursementSimulation(loan.id)
            }.onFailure {
                _eventFlow.emit(UiEvent.ShowToast("Submission failed: ${it.localizedMessage}"))
            }
        }
    }

    // --- Disbursement Simulation ---
    fun startAutoDisbursementSimulation(loanId: String) {
        disbursementSimulationJob?.cancel()
        disbursementSimulationJob = viewModelScope.launch {
            var currentLoan = repository.getLoanById(loanId).stateIn(this).value ?: activeLoan.value
            while (currentLoan != null && currentLoan.disbursementStep < 5) {
                delay(2000)
                currentLoan = repository.advanceDisbursementStep(loanId, currentLoan)
            }
            if (currentLoan != null && currentLoan.disbursementStep == 5) {
                _eventFlow.emit(UiEvent.ShowToast("Funds Disbursed! $${currentLoan.netDisbursementAmount} credited."))
            }
        }
    }

    fun manualAdvanceStep(loanId: String) {
        viewModelScope.launch {
            val loan = repository.getLoanById(loanId).stateIn(this).value ?: activeLoan.value ?: return@launch
            val updated = repository.advanceDisbursementStep(loanId, loan)
            _eventFlow.emit(UiEvent.ShowToast("Advanced to step ${updated.disbursementStep}: ${updated.status.label}"))
        }
    }

    // --- Repayment Actions ---
    fun selectEmiForPayment(emi: EmiItem?) {
        _repaymentState.value = _repaymentState.value.copy(selectedEmiForPayment = emi)
    }

    fun processEmiPayment(method: String) {
        val emi = _repaymentState.value.selectedEmiForPayment ?: return

        viewModelScope.launch {
            _repaymentState.value = _repaymentState.value.copy(isPaymentProcessing = true)
            delay(1000)

            val result = repository.payEmi(emi, method)
            _repaymentState.value = _repaymentState.value.copy(
                isPaymentProcessing = false,
                selectedEmiForPayment = null,
                paymentSuccessDialogShown = true,
                lastPaidTransaction = result.getOrNull()
            )

            result.onSuccess { txn ->
                _eventFlow.emit(UiEvent.PaymentSuccess(txn.amount, txn.refNumber))
            }
        }
    }

    fun dismissPaymentSuccessDialog() {
        _repaymentState.value = _repaymentState.value.copy(paymentSuccessDialogShown = false)
    }

    fun toggleStatementDialog(show: Boolean) {
        _repaymentState.value = _repaymentState.value.copy(statementDialogShown = show)
    }

    fun toggleNocDialog(show: Boolean) {
        _repaymentState.value = _repaymentState.value.copy(nocCertificateDialogShown = show)
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetDemoData()
            recalculateLoan()
            _eventFlow.emit(UiEvent.ShowToast("Demo data reset to default test state."))
        }
    }

    class Factory(private val repository: LoanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoanViewModel(repository) as T
        }
    }
}
