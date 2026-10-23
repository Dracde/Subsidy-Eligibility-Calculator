ThisBuild / scalaVersion := "3.4.2"
ThisBuild / version      := "0.1.0"

// JavaFX ships as OS-specific native artifacts, so we need the right classifier per platform.
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
lazy val osName = System.getProperty("os.name") match {
  case name if name.startsWith("Mac")  => "mac"
  case name if name.startsWith("Win")  => "win"
  case _                               => "linux"
}

lazy val root = (project in file("."))
  .settings(
    name := "SubsidyEligibilityCalculator",
    Compile / mainClass := Some("app.SubsidyEligibilityApp"),

    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    libraryDependencies ++= javaFXModules.map(module =>
      "org.openjfx" % s"javafx-$module" % "21.0.2" classifier osName
    ),

    // Persistence: ScalikeJDBC over an embedded Apache Derby database.
    libraryDependencies += "org.scalikejdbc" %% "scalikejdbc" % "4.3.0",
    libraryDependencies += "org.apache.derby" % "derby" % "10.16.1.1",
    libraryDependencies += "org.apache.derby" % "derbyshared" % "10.16.1.1",
    libraryDependencies += "org.apache.derby" % "derbytools" % "10.16.1.1",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6",

    // Testing
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.2" % Test,
    testFrameworks += new TestFramework("munit.Framework"),

    Compile / scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all",
      "-Wconf:msg=unset private variable:s",
      "-Wconf:msg=method handle in trait EventIncludes:s",
      "-Wconf:msg=unused import:s"
    )
  )
