package service

import scala.util.Try

/** Shared input-validation helpers used by every controller that takes user input
  * (Households, Subsidies, Applications), so number/text parsing isn't duplicated three
  * times across the codebase (DRY). Every risky parse (`toInt`, `BigDecimal(...)`) is
  * wrapped in `Try` and converted to a friendly `Either[String, T]` message.
  */
object ValidationUtils:

  def parseName(rawInput: String): Either[String, String] =
    val trimmed = rawInput.trim
    if trimmed.isEmpty then Left("Name cannot be empty.")
    else if trimmed.length < 2 then Left("Name is too short.")
    else Right(trimmed)

  def parseDescription(rawInput: String): Either[String, String] =
    val trimmed = rawInput.trim
    if trimmed.isEmpty then Left("Description cannot be empty.")
    else Right(trimmed)

  def parseIcNumber(rawInput: String): Either[String, String] =
    val trimmed = rawInput.trim
    if trimmed.isEmpty then Left("IC number cannot be empty.")
    else if !trimmed.matches("""\d{6}-\d{2}-\d{4}""") then Left("IC number must look like 900101-10-1234.")
    else Right(trimmed)

  def parseNonNegativeDecimal(rawInput: String, fieldLabel: String): Either[String, BigDecimal] =
    Try(BigDecimal(rawInput.trim)).toEither.left.map(_ => s"$fieldLabel must be a valid number.")
      .flatMap(value => validateRange(value, BigDecimal(0), BigDecimal(10_000_000), fieldLabel))

  def parseNonNegativeInt(rawInput: String, fieldLabel: String): Either[String, Int] =
    Try(rawInput.trim.toInt).toEither.left.map(_ => s"$fieldLabel must be a whole number.")
      .flatMap(value => validateRange(value, 0, 1_000_000, fieldLabel))

  /** Generic range check reused by every numeric field in the app (parametric polymorphism:
    * one method, any `Ordering`-having type `T`, instead of a separate check per field type).
    */
  def validateRange[T](value: T, minimum: T, maximum: T, fieldLabel: String)(using ordering: Ordering[T]): Either[String, T] =
    if ordering.lt(value, minimum) || ordering.gt(value, maximum) then
      Left(s"$fieldLabel must be between $minimum and $maximum.")
    else Right(value)
