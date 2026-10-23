package model

/** A government subsidy programme a household may apply for.
  *
  * `remainingQuota` behaves the same way `Book.available` behaves in a library system:
  * it drops by one when an application is approved, and is restored by one if an
  * application is later rejected, so the Applications screen only ever offers subsidies
  * that still have room left (mirrors "only books where available > 0").
  */
final case class Subsidy(
    id: Int,
    name: String,
    description: String,
    incomeThreshold: BigDecimal,
    remainingQuota: Int
):
  def isHouseholdEligible(household: Household): Boolean =
    household.monthlyIncome <= incomeThreshold

  def hasQuotaAvailable: Boolean = remainingQuota > 0
