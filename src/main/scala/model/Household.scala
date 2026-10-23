package model


final case class Household(
    id: Int,
    fullName: String,
    icNumber: String,
    monthlyIncome: BigDecimal,
    dependents: Int
)
