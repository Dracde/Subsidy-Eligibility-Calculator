package controller

import javafx.fxml.FXML
import scalafx.scene.control.{Label, TableColumn, TableView, TextField}
import scalafx.Includes.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import model.{Household, HouseholdRepository}
import service.ValidationUtils

import scala.util.{Failure, Success}

class HouseholdsController:

  @FXML private var fullNameField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var icNumberField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var monthlyIncomeField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var dependentsField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var searchField: javafx.scene.control.TextField = scala.compiletime.uninitialized
  @FXML private var statusLabel: javafx.scene.control.Label = scala.compiletime.uninitialized
  // Note: the Add/Update/Delete/Clear buttons are wired directly in households.fxml via
  // onAction="#handleX" to the methods below; they don't need @FXML fields here since no
  // Scala code ever reads the Button objects themselves.
  @FXML private var householdTableView: javafx.scene.control.TableView[Household] = scala.compiletime.uninitialized
  @FXML private var idColumn: javafx.scene.control.TableColumn[Household, Integer] = scala.compiletime.uninitialized
  @FXML private var nameColumn: javafx.scene.control.TableColumn[Household, String] = scala.compiletime.uninitialized
  @FXML private var icColumn: javafx.scene.control.TableColumn[Household, String] = scala.compiletime.uninitialized
  @FXML private var incomeColumn: javafx.scene.control.TableColumn[Household, String] = scala.compiletime.uninitialized
  @FXML private var dependentsColumn: javafx.scene.control.TableColumn[Household, Integer] = scala.compiletime.uninitialized

  private val householdRepository = new HouseholdRepository
  
  var onDataChanged: () => Unit = () => ()

  @FXML
  def initialize(): Unit =
    // ai-assisted: #7
    // why: Household is a plain immutable case class with no ScalaFX properties, so JavaFX's
    // PropertyValueFactory (which expects getX()/xProperty() JavaBean methods) silently fails
    // to populate columns. AI's fix was a manual cellValueFactory closure per column instead.
    idColumn.cellValueFactory = data => ObjectProperty[Integer](data.value.id)
    nameColumn.cellValueFactory = data => StringProperty(data.value.fullName)
    icColumn.cellValueFactory = data => StringProperty(data.value.icNumber)
    incomeColumn.cellValueFactory = data => StringProperty(data.value.monthlyIncome.toString)
    dependentsColumn.cellValueFactory = data => ObjectProperty[Integer](data.value.dependents)

    householdTableView.selectionModel.value.selectedItem.onChange { (_, _, selected) =>
      Option(selected).foreach(populateForm)
    }

    searchField.text.onChange { (_, _, _) => refresh() }

    refresh()

  def refresh(): Unit =
    val keyword = searchField.text.value.trim
    val result =
      if keyword.isEmpty then householdRepository.findAll()
      else householdRepository.searchByNameOrIc(keyword)

    result match
      case Success(households) => householdTableView.items = ObservableBuffer.from(households)
      case Failure(exception)  => showWarning(s"Could not load households: ${exception.getMessage}")

  private def populateForm(household: Household): Unit =
    fullNameField.text = household.fullName
    icNumberField.text = household.icNumber
    monthlyIncomeField.text = household.monthlyIncome.toString
    dependentsField.text = household.dependents.toString

  @FXML
  def handleAdd(): Unit =
    validatedInput() match
      case Left(errorMessage) => showWarning(errorMessage)
      case Right((fullName, icNumber, income, dependentsCount)) =>
        householdRepository.nextId() match
          case Failure(exception) => showWarning(s"Could not generate an ID: ${exception.getMessage}")
          case Success(newId) =>
            val household = Household(newId, fullName, icNumber, income, dependentsCount)
            householdRepository.insert(household) match
              case Success(_) =>
                statusLabel.text = s"Added ${household.fullName}."
                handleClear()
                refresh()
                onDataChanged()
              case Failure(exception) =>
                showWarning(s"Could not save household: ${exception.getMessage}")

  @FXML
  def handleUpdate(): Unit =
    Option(householdTableView.selectionModel.value.selectedItem.value) match
      case None => showWarning("Select a household in the table first.")
      case Some(selectedHousehold) =>
        validatedInput() match
          case Left(errorMessage) => showWarning(errorMessage)
          case Right((fullName, icNumber, income, dependentsCount)) =>
            val updatedHousehold = selectedHousehold.copy(
              fullName = fullName,
              icNumber = icNumber,
              monthlyIncome = income,
              dependents = dependentsCount
            )
            householdRepository.update(updatedHousehold) match
              case Success(_) =>
                statusLabel.text = s"Updated ${updatedHousehold.fullName}."
                refresh()
                onDataChanged()
              case Failure(exception) =>
                showWarning(s"Could not update household: ${exception.getMessage}")

  @FXML
  def handleDelete(): Unit =
    Option(householdTableView.selectionModel.value.selectedItem.value) match
      case None => showWarning("Select a household in the table first.")
      case Some(selectedHousehold) =>
        householdRepository.deleteById(selectedHousehold.id) match
          case Success(_) =>
            statusLabel.text = s"Deleted ${selectedHousehold.fullName}."
            handleClear()
            refresh()
            onDataChanged()
          case Failure(exception) =>
            showWarning(s"Could not delete household: ${exception.getMessage}")

  @FXML
  def handleClear(): Unit =
    fullNameField.text = ""
    icNumberField.text = ""
    monthlyIncomeField.text = ""
    dependentsField.text = ""
    householdTableView.selectionModel.value.clearSelection()

  private def validatedInput(): Either[String, (String, String, BigDecimal, Int)] =
    for
      fullName <- ValidationUtils.parseName(fullNameField.text.value)
      icNumber <- ValidationUtils.parseIcNumber(icNumberField.text.value)
      income <- ValidationUtils.parseNonNegativeDecimal(monthlyIncomeField.text.value, "Monthly income")
      dependentsCount <- ValidationUtils.parseNonNegativeInt(dependentsField.text.value, "Dependents")
    yield (fullName, icNumber, income, dependentsCount)

  private def showWarning(message: String): Unit =
    val alert = new Alert(AlertType.Warning):
      headerText = "Please check your input"
      contentText = message
    alert.showAndWait()
