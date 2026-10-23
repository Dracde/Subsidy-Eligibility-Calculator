package app

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import model.DatabaseConfig
import scalafx.Includes.*

import scala.util.{Failure, Success}

/** Application entry point. Initialises the embedded Derby database, then loads the FXML
  * shell (shell.fxml), which in turn includes the four tab FXML files, each with its own
  * controller — one FXML + one controller per tab.
  */
object SubsidyEligibilityApp extends JFXApp3:

  override def start(): Unit =
    DatabaseConfig.init() match
      case Failure(exception) =>
        exception.printStackTrace()
        val alert = new scalafx.scene.control.Alert(scalafx.scene.control.Alert.AlertType.Error):
          headerText = "Could not start the database"
          contentText = s"The application cannot continue: ${exception.getMessage}"
        alert.showAndWait()
        sys.exit(1)
      case Success(_) =>
        val loader = new FXMLLoader(getClass.getResource("/shell.fxml"))
        val root: javafx.scene.Parent = loader.load()

        stage = new PrimaryStage:
          title = "Subsidy Eligibility Calculator"
          width = 1000
          height = 680
          scene = new Scene(root)
