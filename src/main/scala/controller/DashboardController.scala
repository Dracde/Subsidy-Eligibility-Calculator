package controller

import javafx.fxml.FXML
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import model.{ApplicationRepository, Approved, HouseholdRepository, Pending, Rejected, SubsidyRepository}

import scala.jdk.CollectionConverters.*

class DashboardController:

  @FXML private var summaryLabel: javafx.scene.control.Label = scala.compiletime.uninitialized
  @FXML private var chartSection: javafx.scene.layout.VBox = scala.compiletime.uninitialized

  private val householdRepository = new HouseholdRepository
  private val subsidyRepository = new SubsidyRepository
  private val applicationRepository = new ApplicationRepository

  private val maxBarWidth = 320.0

  @FXML
  def initialize(): Unit =
    refresh()

  def refresh(): Unit =
    val households = householdRepository.findAll().getOrElse(Nil)
    val subsidies = subsidyRepository.findAll().getOrElse(Nil)
    val applications = applicationRepository.findAll().getOrElse(Nil)

    val pendingCount = applications.count(_.status == Pending)
    val approvedCount = applications.count(_.status == Approved)
    val rejectedCount = applications.count(_.status == Rejected)

    summaryLabel.setText(
      s"${households.size} households - ${subsidies.size} subsidy programmes - " +
        s"$pendingCount pending - $approvedCount approved - $rejectedCount rejected"
    )

    val maxQuota = math.max(1, subsidies.map(_.remainingQuota).maxOption.getOrElse(1))
    val barRows = subsidies.map { subsidy =>
      val barWidth = math.max(2.0, (subsidy.remainingQuota.toDouble / maxQuota.toDouble) * maxBarWidth)
      val bar = new Rectangle {
        width = barWidth
        height = 16.0
        fill = Color.web("#2f6fb0")
      }
      val label = new Label(s"${subsidy.name}: ${subsidy.remainingQuota} slot(s) remaining")
      val row = new VBox(4.0)
      row.children = Seq(label, bar)
      row
    }

    chartSection.getChildren.setAll(barRows.map(_.delegate).asJava)
