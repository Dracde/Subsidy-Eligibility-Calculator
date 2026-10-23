package service

import model.{Household, Subsidy}

/** Pure eligibility rules, independent of where households/subsidies came from (Derby today,
  * anything else tomorrow). Kept side-effect free on purpose so it can be unit tested without
  * touching the database.
  */
class EligibilityService:

  def eligibleSubsidiesFor(household: Household, subsidies: List[Subsidy]): List[Subsidy] =
    subsidies.filter(subsidy => subsidy.isHouseholdEligible(household) && subsidy.hasQuotaAvailable)

  def eligibleHouseholdsFor(subsidy: Subsidy, households: List[Household]): List[Household] =
    households.filter(household => subsidy.isHouseholdEligible(household))
