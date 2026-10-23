# Citations — Third-Party Code, Libraries, and Documentation

| Source | URL | License | Where used |
|---|---|---|---|
| ScalaFX documentation & examples | https://www.scalafx.org/docs/ | BSD 3-Clause | Idioms for `onAction = handle { ... }`, ScalaFX property wrapping via `scalafx.Includes.*`, and `cellValueFactory` binding patterns across all controllers in `src/main/scala/controller/`. |
| OpenJFX (JavaFX) documentation | https://openjfx.io/javadoc/21/ | GPL v2 with Classpath Exception | `FXMLLoader`, `TableCell`/`TableColumn` cell factory APIs, `DatePicker`, `ComboBox` + `StringConverter` usage in `src/main/scala/controller/ApplicationsController.scala`. |
| ScalikeJDBC documentation | https://scalikejdbc.org/documentation/ | Apache License 2.0 | `DB.autoCommit` / `DB.readOnly` session blocks, the `sql"..."`/`SQL(...)` interpolator, and `ConnectionPool.singleton` setup in `src/main/scala/model/DatabaseConfig.scala` and the three repository classes. |
| Apache Derby documentation | https://db.apache.org/derby/docs/10.16/ | Apache License 2.0 | Embedded driver class name, JDBC URL format (`jdbc:derby:subsidydb;create=true`), and the DDL syntax used in `src/main/scala/model/DatabaseConfig.scala`. |
| munit documentation | https://scalameta.org/munit/docs/getting-started.html | Apache License 2.0 | Test suite structure and `FunSuite`/`test(...)` syntax in `src/test/scala/`. |
| Scala 3 reference — enums & sealed traits | https://docs.scala-lang.org/scala3/reference/enums/enums.html | Apache License 2.0 | Design of the `ApplicationStatus` sealed trait hierarchy in `src/main/scala/model/ApplicationStatus.scala`. |

No third-party fonts, images, icons, or media assets were used in this project; the UI relies
only on ScalaFX/JavaFX built-in controls, layouts, and CSS-style inline styling.

No code was copied verbatim from any of the above sources — each was consulted for API usage
and idiom, then adapted to this project's own domain model and file structure, as logged in
`ai/interaction_log.md`.
