package model

import scalikejdbc.*
import scala.util.Try

class ApplicationRepository extends Repository[Application]("applications"):

  override protected def rowMapper(resultSet: WrappedResultSet): Application =
    Application(
      id = resultSet.int("id"),
      householdId = resultSet.int("household_id"),
      subsidyId = resultSet.int("subsidy_id"),
      appliedDate = resultSet.localDate("applied_date"),
      reviewDeadline = resultSet.localDate("review_deadline"),
      status = ApplicationStatus.fromLabel(resultSet.string("status"))
    )

  def nextId(): Try[Int] =
    Try {
      DB readOnly { implicit session =>
        SQL("SELECT COALESCE(MAX(id), 0) AS maxId FROM applications")
          .map(_.int("maxId")).single.apply().getOrElse(0) + 1
      }
    }

  def insert(application: Application): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("""INSERT INTO applications
                 (id, household_id, subsidy_id, applied_date, review_deadline, status)
               VALUES (?, ?, ?, ?, ?, ?)""")
          .bind(
            application.id,
            application.householdId,
            application.subsidyId,
            application.appliedDate,
            application.reviewDeadline,
            application.status.label
          )
          .update.apply()
      }
    }

  def updateStatus(applicationId: Int, status: ApplicationStatus): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("UPDATE applications SET status = ? WHERE id = ?")
          .bind(status.label, applicationId)
          .update.apply()
      }
    }
