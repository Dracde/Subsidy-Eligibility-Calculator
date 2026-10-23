package service

import munit.FunSuite

class ValidationUtilsSpec extends FunSuite:

  test("parseName rejects empty input"):
    assert(ValidationUtils.parseName("").isLeft)

  test("parseName accepts a valid name"):
    assertEquals(ValidationUtils.parseName("Ali bin Ahmad"), Right("Ali bin Ahmad"))

  test("parseIcNumber rejects a malformed IC number"):
    assert(ValidationUtils.parseIcNumber("not-an-ic").isLeft)

  test("parseIcNumber accepts a correctly formatted IC number"):
    assertEquals(ValidationUtils.parseIcNumber("900101-10-1234"), Right("900101-10-1234"))

  test("parseNonNegativeDecimal rejects non-numeric input"):
    assert(ValidationUtils.parseNonNegativeDecimal("abc", "Income").isLeft)

  test("parseNonNegativeInt rejects out-of-range input"):
    assert(ValidationUtils.parseNonNegativeInt("99999999", "Dependents").isLeft)

  test("validateRange accepts a value within bounds"):
    assertEquals(ValidationUtils.validateRange(5, 0, 10, "Value"), Right(5))
