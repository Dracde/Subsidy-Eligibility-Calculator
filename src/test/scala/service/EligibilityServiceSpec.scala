package service

import munit.FunSuite
import model.{Household, Subsidy}

class EligibilityServiceSpec extends FunSuite:

  private val eligibilityService = new EligibilityService

  private val household = Household(1, "Test Household", "900101-10-1234", BigDecimal(1500), 2)
  private val eligibleSubsidy = Subsidy(1, "Food Assistance Subsidy", "Groceries", BigDecimal(2000), 10)
  private val ineligibleSubsidy = Subsidy(2, "Housing Subsidy", "Rent", BigDecimal(1000), 10)
  private val fullQuotaSubsidy = Subsidy(3, "Education Grant", "Fees", BigDecimal(2000), 0)

  test("household under the income threshold is eligible"):
    assert(eligibleSubsidy.isHouseholdEligible(household))

  test("household over the income threshold is not eligible"):
    assert(!ineligibleSubsidy.isHouseholdEligible(household))

  test("eligibleSubsidiesFor excludes subsidies with no remaining quota"):
    val result = eligibilityService.eligibleSubsidiesFor(household, List(eligibleSubsidy, fullQuotaSubsidy))
    assertEquals(result, List(eligibleSubsidy))

  test("eligibleHouseholdsFor only returns households under the subsidy's threshold"):
    val higherIncomeHousehold = household.copy(id = 2, monthlyIncome = BigDecimal(5000))
    val result = eligibilityService.eligibleHouseholdsFor(eligibleSubsidy, List(household, higherIncomeHousehold))
    assertEquals(result, List(household))
