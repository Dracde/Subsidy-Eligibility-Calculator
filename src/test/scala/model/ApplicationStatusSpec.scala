package model

import munit.FunSuite
import java.time.LocalDate

class ApplicationStatusSpec extends FunSuite:

  test("a pending application past its deadline is overdue"):
    val application = Application(1, 1, 1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(1), Pending)
    assert(application.isOverdue())

  test("an approved application is never overdue even if its old deadline has passed"):
    val application = Application(1, 1, 1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(1), Approved)
    assert(!application.isOverdue())

  test("a pending application before its deadline is not overdue"):
    val application = Application(1, 1, 1, LocalDate.now(), LocalDate.now().plusDays(14), Pending)
    assert(!application.isOverdue())

  test("fromLabel round-trips every known status label"):
    assertEquals(ApplicationStatus.fromLabel(Pending.label), Pending)
    assertEquals(ApplicationStatus.fromLabel(Approved.label), Approved)
    assertEquals(ApplicationStatus.fromLabel(Rejected.label), Rejected)
