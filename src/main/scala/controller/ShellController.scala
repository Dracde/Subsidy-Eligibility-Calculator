package controller

import javafx.fxml.FXML
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

class ShellController:

  @FXML private var householdsController: HouseholdsController = scala.compiletime.uninitialized
  @FXML private var subsidiesController: SubsidiesController = scala.compiletime.uninitialized
  @FXML private var applicationsController: ApplicationsController = scala.compiletime.uninitialized
  @FXML private var dashboardController: DashboardController = scala.compiletime.uninitialized

  @FXML
  def initialize(): Unit =
    val refreshAll: () => Unit = () =>
      householdsController.refresh()
      subsidiesController.refresh()
      applicationsController.refresh()
      dashboardController.refresh()

    householdsController.onDataChanged = refreshAll
    subsidiesController.onDataChanged = refreshAll
    applicationsController.onDataChanged = refreshAll

  @FXML
  def handleClose(): Unit =
    sys.exit(0)

  @FXML
  def handleAbout(): Unit =
    val alert = new Alert(AlertType.Information):
      headerText = "Subsidy Eligibility Calculator"
      contentText = "A ScalaFX + FXML application supporting SDG 1 (No Poverty) by matching " +
        "households to the government subsidies they qualify for."
    alert.showAndWait()
