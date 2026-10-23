package controller

import javafx.fxml.FXML
import scalafx.scene.control.{Label, TableColumn, TableView, TextField}
import scalafx.Includes.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import model.{Household, HouseholdRepository, Subsidy, SubsidyRepository}
import service.{EligibilityService, ValidationUtils}

import scala.util.{Failure, Success}

class SubsidiesController:

  @FXML private var nameField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var descriptionField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var incomeThresholdField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var quotaField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var searchField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var statusLabel: javafx.scene.control.Label = scala.compiletime.uninitialized
  @FXML private var subsidyTableView: javafx.scene.control.TableView[Subsidy] = scala.compiletime.uninitialized
  @FXML private var subsidyIdColumn: javafx.scene.control.TableColumn[Subsidy, Integer] = scala.compiletime.uninitialized
  @FXML private var subsidyNameColumn: javafx.scene.control.TableColumn[Subsidy, String] = scala.compiletime.uninitialized
  @FXML private var subsidyDescriptionColumn: javafx.scene.control.TableColumn[Subsidy, String] = scala.compiletime.uninitialized
  @FXML private var subsidyThresholdColumn: javafx.scene.control.TableColumn[Subsidy, String] = scala.compiletime.uninitialized
  @FXML private var subsidyQuotaColumn: javafx.scene.control.TableColumn[Subsidy, Integer] = scala.compiletime.uninitialized
  @FXML private var eligibleHouseholdTableView: javafx.scene.control.TableView[Household] = scala.compiletime.uninitialized
  @FXML private var eligibleNameColumn: javafx.scene.control.TableColumn[Household, String] = scala.compiletime.uninitialized
  @FXML private var eligibleIncomeColumn: javafx.scene.control.TableColumn[Household, String] = scala.compiletime.uninitialized

  private val subsidyRepository = new SubsidyRepository
  private val householdRepository = new HouseholdRepository
  private val eligibilityService = new EligibilityService

  var onDataChanged: () => Unit = () => ()

  @FXML
  def initialize(): Unit =
    subsidyIdColumn.cellValueFactory = data => ObjectProperty[Integer](data.value.id)
    subsidyNameColumn.cellValueFactory = data => StringProperty(data.value.name)
    subsidyDescriptionColumn.cellValueFactory = data => StringProperty(data.value.description)
    subsidyThresholdColumn.cellValueFactory = data => StringProperty(data.value.incomeThreshold.toString)
    subsidyQuotaColumn.cellValueFactory = data => ObjectProperty[Integer](data.value.remainingQuota)

    eligibleNameColumn.cellValueFactory = data => StringProperty(data.value.fullName)
    eligibleIncomeColumn.cellValueFactory = data => StringProperty(data.value.monthlyIncome.toString)

    // ai-assisted: #8
    // why: AI suggested binding the bottom TableView directly to the top TableView's
    // selectedItem property so eligible households update reactively on selection, instead of
    // requiring a separate "show eligible households" button the user has to remember to click.
    subsidyTableView.selectionModel.value.selectedItem.onChange { (_, _, selected) =>
      Option(selected) match
        case Some(subsidy) =>
          populateForm(subsidy)
          showEligibleHouseholds(subsidy)
        case None =>
          eligibleHouseholdTableView.items = ObservableBuffer.empty[Household]
    }

    searchField.text.onChange { (_, _, _) => refresh() }

    refresh()

  def refresh(): Unit =
    val keyword = searchField.text.value.trim
    val result =
      if keyword.isEmpty then subsidyRepository.findAll()
      else subsidyRepository.searchByNameOrDescription(keyword)

    result match
      case Success(subsidies) => subsidyTableView.items = ObservableBuffer.from(subsidies)
      case Failure(exception) => showWarning(s"Could not load subsidies: ${exception.getMessage}")

  private def showEligibleHouseholds(subsidy: Subsidy): Unit =
    householdRepository.findAll() match
      case Success(households) =>
        eligibleHouseholdTableView.items =
          ObservableBuffer.from(eligibilityService.eligibleHouseholdsFor(subsidy, households))
      case Failure(exception) =>
        showWarning(s"Could not load households: ${exception.getMessage}")

  private def populateForm(subsidy: Subsidy): Unit =
    nameField.text = subsidy.name
    descriptionField.text = subsidy.description
    incomeThresholdField.text = subsidy.incomeThreshold.toString
    quotaField.text = subsidy.remainingQuota.toString

  @FXML
  def handleAdd(): Unit =
    validatedInput() match
      case Left(errorMessage) => showWarning(errorMessage)
      case Right((name, description, threshold, quota)) =>
        subsidyRepository.nextId() match
          case Failure(exception) => showWarning(s"Could not generate an ID: ${exception.getMessage}")
          case Success(newId) =>
            val subsidy = Subsidy(newId, name, description, threshold, quota)
            subsidyRepository.insert(subsidy) match
              case Success(_) =>
                statusLabel.text = s"Added ${subsidy.name}."
                handleClear()
                refresh()
                onDataChanged()
              case Failure(exception) =>
                showWarning(s"Could not save subsidy: ${exception.getMessage}")

  @FXML
  def handleUpdate(): Unit =
    Option(subsidyTableView.selectionModel.value.selectedItem.value) match
      case None => showWarning("Select a subsidy in the table first.")
      case Some(selectedSubsidy) =>
        validatedInput() match
          case Left(errorMessage) => showWarning(errorMessage)
          case Right((name, description, threshold, quota)) =>
            val updatedSubsidy = selectedSubsidy.copy(
              name = name,
              description = description,
              incomeThreshold = threshold,
              remainingQuota = quota
            )
            subsidyRepository.update(updatedSubsidy) match
              case Success(_) =>
                statusLabel.text = s"Updated ${updatedSubsidy.name}."
                refresh()
                onDataChanged()
              case Failure(exception) =>
                showWarning(s"Could not update subsidy: ${exception.getMessage}")

  @FXML
  def handleRemove(): Unit =
    Option(subsidyTableView.selectionModel.value.selectedItem.value) match
      case None => showWarning("Select a subsidy in the table first.")
      case Some(selectedSubsidy) =>
        subsidyRepository.deleteById(selectedSubsidy.id) match
          case Success(_) =>
            statusLabel.text = s"Removed ${selectedSubsidy.name}."
            handleClear()
            refresh()
            onDataChanged()
          case Failure(exception) =>
            showWarning(s"Could not remove subsidy: ${exception.getMessage}")

  @FXML
  def handleClear(): Unit =
    nameField.text = ""
    descriptionField.text = ""
    incomeThresholdField.text = ""
    quotaField.text = ""
    subsidyTableView.selectionModel.value.clearSelection()

  private def validatedInput(): Either[String, (String, String, BigDecimal, Int)] =
    for
      name <- ValidationUtils.parseName(nameField.text.value)
      description <- ValidationUtils.parseDescription(descriptionField.text.value)
      threshold <- ValidationUtils.parseNonNegativeDecimal(incomeThresholdField.text.value, "Income threshold")
      quota <- ValidationUtils.parseNonNegativeInt(quotaField.text.value, "Quota")
    yield (name, description, threshold, quota)

  private def showWarning(message: String): Unit =
    val alert = new Alert(AlertType.Warning):
      headerText = "Please check your input"
      contentText = message
    alert.showAndWait()
