package controller

import javafx.fxml.FXML
import scalafx.scene.control.{Button, ComboBox, DatePicker, Label, TableCell, TableColumn, TableView, TextField}
import scalafx.scene.layout.{HBox, VBox, GridPane}
import javafx.util.StringConverter
import scalafx.Includes.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import model.*
import service.EligibilityService

import java.time.LocalDate
import scala.util.{Failure, Success}


case class ApplicationRow(application: Application, householdName: String, subsidyName: String)

class ApplicationsController:

  @FXML private var householdComboBox: javafx.scene.control.ComboBox[Household] = scala.compiletime.uninitialized
  @FXML private var subsidyComboBox: javafx.scene.control.ComboBox[Subsidy] = scala.compiletime.uninitialized
  @FXML private var reviewDeadlinePicker: javafx.scene.control.DatePicker = scala.compiletime.uninitialized
  // Note: the Submit Application button is wired directly in applications.fxml via
  // onAction="#handleApply"; no Scala-side field is needed for it.
  @FXML private var statusLabel: javafx.scene.control.Label = scala.compiletime.uninitialized
  @FXML private var applicationTableView: javafx.scene.control.TableView[ApplicationRow] = scala.compiletime.uninitialized
  @FXML private var applicationIdColumn: javafx.scene.control.TableColumn[ApplicationRow, Integer] = scala.compiletime.uninitialized
  @FXML private var applicationHouseholdColumn: javafx.scene.control.TableColumn[ApplicationRow, String] = scala.compiletime.uninitialized
  @FXML private var applicationSubsidyColumn: javafx.scene.control.TableColumn[ApplicationRow, String] = scala.compiletime.uninitialized
  @FXML private var appliedDateColumn: javafx.scene.control.TableColumn[ApplicationRow, String] = scala.compiletime.uninitialized
  @FXML private var reviewDeadlineColumn: javafx.scene.control.TableColumn[ApplicationRow, String] = scala.compiletime.uninitialized
  @FXML private var statusColumn: javafx.scene.control.TableColumn[ApplicationRow, String] = scala.compiletime.uninitialized
  @FXML private var actionsColumn: javafx.scene.control.TableColumn[ApplicationRow, ApplicationRow] = scala.compiletime.uninitialized

  private val householdRepository = new HouseholdRepository
  private val subsidyRepository = new SubsidyRepository
  private val applicationRepository = new ApplicationRepository
  private val eligibilityService = new EligibilityService

  var onDataChanged: () => Unit = () => ()

  @FXML
  def initialize(): Unit =
    householdComboBox.converter = new StringConverter[Household]:
      override def toString(household: Household): String =
        if household == null then "" else s"#${household.id} ${household.fullName}"
      override def fromString(text: String): Household = null

    subsidyComboBox.converter = new StringConverter[Subsidy]:
      override def toString(subsidy: Subsidy): String =
        if subsidy == null then "" else s"${subsidy.name} (${subsidy.remainingQuota} left)"
      override def fromString(text: String): Subsidy = null

    reviewDeadlinePicker.value = LocalDate.now().plusDays(14)

    householdComboBox.value.onChange { (_, _, _) => refreshEligibleSubsidyOptions() }

    applicationIdColumn.cellValueFactory = data => ObjectProperty[Integer](data.value.application.id)
    applicationHouseholdColumn.cellValueFactory = data => StringProperty(data.value.householdName)
    applicationSubsidyColumn.cellValueFactory = data => StringProperty(data.value.subsidyName)
    appliedDateColumn.cellValueFactory = data => StringProperty(data.value.application.appliedDate.toString)
    reviewDeadlineColumn.cellValueFactory = data => StringProperty(data.value.application.reviewDeadline.toString)
    statusColumn.cellValueFactory = data => StringProperty(data.value.application.status.label)
    actionsColumn.cellValueFactory = data => ObjectProperty(data.value)

    // ai-assisted: #9
    // why: AI showed how to colour the Overdue? column reactively via a custom cellFactory
    // (reading the row's own Application on every updateItem) instead of looping over the
    // table's rows and mutating styles by hand after each refresh.
    statusColumn.cellFactory = (col: javafx.scene.control.TableColumn[ApplicationRow, String]) =>
      new javafx.scene.control.TableCell[ApplicationRow, String]:
        override protected def updateItem(value: String, empty: Boolean): Unit =
          super.updateItem(value, empty)
          if empty || value == null then
            setText(null)
            setStyle("")
          else
            val row = getTableRow.getItem
            val overdue = row != null && row.application.isOverdue()
            setText(if row != null && row.application.status.isPending && overdue then s"$value (Overdue)" else value)
            setStyle(
              if row == null then ""
              else if row.application.status.isPending && overdue then "-fx-text-fill: #b00020; -fx-font-weight: bold;"
              else if row.application.status.isPending then "-fx-text-fill: #2e7d32;"
              else row.application.status.statusColor
            )

    // ai-assisted: #10
    // why: AI proposed embedding Approve/Reject buttons directly inside a TableCell so a case
    // worker can act on a row without a separate selection step, disabling both buttons once
    // an application is no longer pending.
    actionsColumn.cellFactory = (col: javafx.scene.control.TableColumn[ApplicationRow, ApplicationRow]) =>
      new javafx.scene.control.TableCell[ApplicationRow, ApplicationRow]:
        private val approveButton = new Button("Approve")
        private val rejectButton = new Button("Reject")
        private val actionsBox = new HBox(6.0, approveButton, rejectButton)

        approveButton.onAction = handle { approveApplication(getItem) }
        rejectButton.onAction = handle { rejectApplication(getItem) }

        override protected def updateItem(value: ApplicationRow, empty: Boolean): Unit =
          super.updateItem(value, empty)
          if empty || value == null then
            setGraphic(null)
          else
            val stillPending = value.application.status.isPending
            approveButton.setDisable(!stillPending)
            rejectButton.setDisable(!stillPending)
            setGraphic(actionsBox)

    refreshHouseholdOptions()
    refresh()

  def refresh(): Unit =
    refreshHouseholdOptions()
    loadApplications()

  private def refreshHouseholdOptions(): Unit =
    householdRepository.findAll() match
      case Success(households) => householdComboBox.items = ObservableBuffer.from(households)
      case Failure(exception)  => showWarning(s"Could not load households: ${exception.getMessage}")
    refreshEligibleSubsidyOptions()

  private def refreshEligibleSubsidyOptions(): Unit =
    val selectedHousehold = Option(householdComboBox.value.value)
    (selectedHousehold, subsidyRepository.findAll()) match
      case (Some(household), Success(subsidies)) =>
        subsidyComboBox.items = ObservableBuffer.from(eligibilityService.eligibleSubsidiesFor(household, subsidies))
      case (None, _) =>
        subsidyComboBox.items = ObservableBuffer.empty[Subsidy]
      case (_, Failure(exception)) =>
        showWarning(s"Could not load subsidies: ${exception.getMessage}")

  private def loadApplications(): Unit =
    (applicationRepository.findAll(), householdRepository.findAll(), subsidyRepository.findAll()) match
      case (Success(applications), Success(households), Success(subsidies)) =>
        val householdNamesById = households.map(household => household.id -> household.fullName).toMap
        val subsidyNamesById = subsidies.map(subsidy => subsidy.id -> subsidy.name).toMap
        val rows = applications.map { application =>
          ApplicationRow(
            application,
            householdNamesById.getOrElse(application.householdId, "Unknown household"),
            subsidyNamesById.getOrElse(application.subsidyId, "Unknown subsidy")
          )
        }
        applicationTableView.items = ObservableBuffer.from(rows)
      case _ =>
        showWarning("Could not load applications.")

  @FXML
  def handleApply(): Unit =
    val selectedHousehold = Option(householdComboBox.value.value)
    val selectedSubsidy = Option(subsidyComboBox.value.value)
    val selectedDeadline = Option(reviewDeadlinePicker.value.value)

    (selectedHousehold, selectedSubsidy, selectedDeadline) match
      case (None, _, _) =>
        showWarning("Select a household first.")
      case (_, None, _) =>
        showWarning("This household has no eligible subsidy with remaining quota to apply for.")
      case (_, _, None) =>
        showWarning("Pick a review deadline.")
      case (Some(household), Some(subsidy), Some(deadline)) =>
        // A borrow attempt on an unavailable book must fail cleanly — same rule here: an
        // application for a subsidy the household isn't eligible for, or with no quota left,
        // must fail with a clear warning, never a stack trace.
        if !subsidy.isHouseholdEligible(household) || !subsidy.hasQuotaAvailable then
          showWarning(s"${household.fullName} is not eligible for ${subsidy.name}, or its quota is full.")
        else
          applicationRepository.nextId() match
            case Failure(exception) => showWarning(s"Could not generate an ID: ${exception.getMessage}")
            case Success(newId) =>
              val application = Application(newId, household.id, subsidy.id, LocalDate.now(), deadline, Pending)
              val outcome =
                for
                  _ <- applicationRepository.insert(application)
                  _ <- subsidyRepository.adjustQuota(subsidy.id, -1)
                yield ()
              outcome match
                case Success(_) =>
                  statusLabel.text = s"Application submitted for ${household.fullName}."
                  reviewDeadlinePicker.value = LocalDate.now().plusDays(14)
                  refresh()
                  onDataChanged()
                case Failure(exception) =>
                  showWarning(s"Could not submit application: ${exception.getMessage}")

  private def approveApplication(row: ApplicationRow): Unit =
    applicationRepository.updateStatus(row.application.id, Approved) match
      case Success(_) =>
        statusLabel.text = s"Approved application #${row.application.id}."
        refresh()
        onDataChanged()
      case Failure(exception) =>
        showWarning(s"Could not approve application: ${exception.getMessage}")

  private def rejectApplication(row: ApplicationRow): Unit =
    val outcome =
      for
        _ <- applicationRepository.updateStatus(row.application.id, Rejected)
        _ <- subsidyRepository.adjustQuota(row.application.subsidyId, 1)
      yield ()
    outcome match
      case Success(_) =>
        statusLabel.text = s"Rejected application #${row.application.id}; quota released."
        refresh()
        onDataChanged()
      case Failure(exception) =>
        showWarning(s"Could not reject application: ${exception.getMessage}")

  private def showWarning(message: String): Unit =
    val alert = new Alert(AlertType.Warning):
      headerText = "Please check your input"
      contentText = message
    alert.showAndWait()
