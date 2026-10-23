package model

import java.time.LocalDate

final case class Application(
    id: Int,
    householdId: Int,
    subsidyId: Int,
    appliedDate: LocalDate,
    reviewDeadline: LocalDate,
    status: ApplicationStatus
):
  def isOverdue(today: LocalDate = LocalDate.now()): Boolean =
    status.isPending && reviewDeadline.isBefore(today)
