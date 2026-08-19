package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.EmploymentType
import com.example.domain.model.KycStatus
import com.example.domain.model.UserProfile
import com.example.domain.usecase.CalculateEligibilityUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Instant Loan", appName)
  }

  @Test
  fun `calculate eligibility calculates correct EMI and fees`() {
    val useCase = CalculateEligibilityUseCase()
    val profile = UserProfile(
      id = "TEST-01",
      monthlyIncome = 5000.0,
      creditScore = 760,
      employmentType = EmploymentType.SALARIED
    )
    val result = useCase.execute(principal = 3000.0, tenureMonths = 6, userProfile = profile)
    assertTrue(result.monthlyEmi > 0)
    assertEquals(6, result.tenureMonths)
    assertEquals(3000.0, result.principal, 0.01)
    assertTrue(result.processingFee >= 25.0)
    assertEquals(result.principal - result.processingFee, result.netDisbursement, 0.01)
    assertEquals(6, result.amortizedEmis.size)
  }
}

