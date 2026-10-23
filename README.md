# Subsidy Eligibility Calculator

**PRG2104 Final Project — Tier C (AI-Integrated)**
**Theme:** SDG 1 — No Poverty · **Sub-domain:** Subsidy Eligibility Calculator

A ScalaFX + FXML desktop application that helps case workers register households, manage
government subsidy programmes, and track applications against those subsidies — matching each
household to the subsidies it's actually eligible for.

## Features (4 required, end-to-end)

1. **Households** — register, update, delete, and search households (by name or IC number).
2. **Subsidies** — register, update, remove, and search subsidy programmes; selecting a subsidy
   reactively shows every household currently eligible for it.
3. **Applications** — submit a household's application for an eligible subsidy (only subsidies
   the household actually qualifies for, with remaining quota, are offered); approve or reject
   pending applications directly from the table, with an overdue-review indicator.
4. **Dashboard** — summary counts (households, subsidies, pending/approved/rejected
   applications) and a proportional bar per subsidy showing remaining quota.

## Tech stack

- **Language:** Scala 3.4
- **UI:** ScalaFX 21 over JavaFX 21, with FXML views (one FXML file + one controller per tab)
- **Persistence:** Embedded Apache Derby via ScalikeJDBC (`jdbc:derby:subsidydb;create=true`)
- **Build tool:** sbt
- **Testing:** munit

## Setup & run instructions

**Prerequisites:** JDK 21, sbt 1.10+. No separate database installation is needed — Derby runs
embedded and creates `subsidydb/` inside the project folder on first launch, with a few sample
households and subsidies seeded automatically.

```bash
cd Project_<StudentID>
sbt run
```

The app window should open on the **Households** tab. Sample data is already loaded so you can
try Subsidies/Applications/Dashboard immediately, or add your own household first.

To run the unit tests:

```bash
sbt test
```

To check for compiler warnings before submission:

```bash
sbt clean compile
```

## Project structure

```
Project_24020737/
├── build.sbt / project/build.properties
├── src/main/scala/
│   ├── model/        # Household, Subsidy, Application, ApplicationStatus, Repository[T] + 3 repos, DatabaseConfig
│   ├── service/       # ValidationUtils, EligibilityService (pure logic, unit tested)
│   ├── controller/    # ShellController + one controller per tab
│   └── app/           # SubsidyEligibilityApp (JFXApp3 entry point)
├── src/main/resources/  # shell.fxml + one FXML per tab
├── src/test/scala/     # munit tests for service/ and model/
├── docs/                # UML, reflection, ai_reflection, dev_log, citations, demo video
├── ai/                  # AI interaction log + AI use declaration
└── submission_manifest.md
```

## AI-use summary

AI was used throughout under the PRG2104 Tier C AI-Integrated policy.
Every significant prompt is logged in `ai/interaction_log.md`, and every
AI-assisted code block is tagged in source with `// ai-assisted: #N` referencing the matching
log entry.

## Third-party citations & licenses

Full details with URLs are in `docs/citations.md`. Summary:

- **ScalaFX** — BSD 3-Clause License — UI idioms and property bindings.
- **OpenJFX (JavaFX)** — GPL v2 with Classpath Exception — FXML loading, TableCell/cellFactory APIs.
- **ScalikeJDBC** — Apache License 2.0 — database access layer.
- **Apache Derby** — Apache License 2.0 — embedded database engine.
- **munit** — Apache License 2.0 — test framework.
