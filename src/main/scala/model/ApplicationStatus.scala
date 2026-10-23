package model

// ai-assisted: #5s
// why: AI proposed modelling application status as a sealed trait hierarchy instead of a
// plain String column, so the UI colour/label logic lives once, next to the data it describes.
sealed trait ApplicationStatus:
  def label: String
  def isPending: Boolean
  def statusColor: String

case object Pending extends ApplicationStatus:
  override val label = "Pending"
  override val isPending = true
  override val statusColor = "-fx-text-fill: #b8860b;" // amber while a case worker still needs to review it

case object Approved extends ApplicationStatus:
  override val label = "Approved"
  override val isPending = false
  override val statusColor = "-fx-text-fill: #2e7d32;" // green once the household has been approved

case object Rejected extends ApplicationStatus:
  override val label = "Rejected"
  override val isPending = false
  override val statusColor = "-fx-text-fill: #b00020;" // red once rejected, quota is released back

object ApplicationStatus:
  def fromLabel(raw: String): ApplicationStatus = raw match
    case "Approved" => Approved
    case "Rejected" => Rejected
    case _          => Pending
