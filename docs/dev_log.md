# Development Log — Subsidy Eligibility Calculator

## 2026-04-13 — Brief & plan
Read the full PRG2104 Tier C brief and picked **Subsidy Eligibility Calculator** as my SDG 1
(No Poverty) sub-domain. Sketched the four tabs on paper: Households, Subsidies, Applications,
Dashboard. Set up `Project_<StudentID>/` following the standard submission structure and wrote
the first pass of README.md naming the theme and four features. Started this dev log and
`ai/interaction_log.md` today, as required.

## 2026-04-20 — Domain model + ScalaFX/FXML/Derby skeleton
Wrote the immutable domain model: `Household`, `Subsidy`, `Application`, and the
`ApplicationStatus` sealed trait hierarchy (`Pending` / `Approved` / `Rejected`). Built the
generic `Repository[T]` base class and the three concrete `HouseholdRepository` /
`SubsidyRepository` / `ApplicationRepository` classes over ScalikeJDBC + embedded Derby. Got the
ScalaFX shell (`JFXApp3`, `shell.fxml`, `ShellController`) loading with the Households tab only,
per the incremental approach from the modified template prompt. 

## 2026-04-24 — Subsidies + Applications tabs
Built the Subsidies tab, including the reactive bottom TableView showing eligible households on
selection. Built the Applications tab: household + eligible-subsidy dropdowns,
review-deadline DatePicker, and the submit flow that decrements `Subsidy.remainingQuota` (log
entry #3). Hit and fixed the Derby "table already exists on second run" bug (log entry #6) and the
missing PropertyValueFactory / cellValueFactory issue on plain case classes (log entry #7).

## 2026-04-28 — Dashboard, overdue colouring, approve/reject
Added the Dashboard tab with the proportional quota bars. Implemented the
Overdue?-style status colouring in the Applications table via a custom `cellFactory` (log entry
#9) and embedded the Approve/Reject buttons directly in each row (log entry #10), including the
quota-restoration fix on reject. Fixed a Linux-only `NoClassDefFoundError` caused by a missing
JavaFX OS classifier in `build.sbt` (log entry #11). Wrote the munit test suites for
`EligibilityService`, `ValidationUtils`, and `ApplicationStatus`.

## 2026-05-02 — Validation, polish, and manual testing
Added input validation across all three forms (`ValidationUtils`, IC number format check,
non-negative numeric ranges) so bad input shows a clean `Alert(WARNING)` instead of a crash.
Manually walked through all four features end-to-end three times: register a household → check
it shows up under the right subsidy's eligible list → submit an application → approve it →
confirm the Dashboard numbers move. Cleaned up unused `@FXML` fields that `-Wunused:all` was
flagging (Button fields wired purely through FXML `onAction` attributes never needed a matching
Scala field).

## 2026-05-04 — Documentation and packaging
Drafted `docs/reflection.md`, `docs/ai_reflection.md`, and `docs/citations.md`. Filled in `submission_manifest.md`
and finalised `ai/declaration.md`. Created the UML class diagram from the actual
`src/main/scala/model/` and `src/main/scala/controller/` classes.
