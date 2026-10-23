package model

import scalikejdbc.*
import scala.util.Try

class SubsidyRepository extends Repository[Subsidy]("subsidies"):

  override protected def rowMapper(resultSet: WrappedResultSet): Subsidy =
    Subsidy(
      id = resultSet.int("id"),
      name = resultSet.string("name"),
      description = resultSet.string("description"),
      incomeThreshold = BigDecimal(resultSet.bigDecimal("income_threshold")),
      remainingQuota = resultSet.int("remaining_quota")
    )

  def nextId(): Try[Int] =
    Try {
      DB readOnly { implicit session =>
        SQL("SELECT COALESCE(MAX(id), 0) AS maxId FROM subsidies")
          .map(_.int("maxId")).single.apply().getOrElse(0) + 1
      }
    }

  def insert(subsidy: Subsidy): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("""INSERT INTO subsidies (id, name, description, income_threshold, remaining_quota)
               VALUES (?, ?, ?, ?, ?)""")
          .bind(subsidy.id, subsidy.name, subsidy.description, subsidy.incomeThreshold, subsidy.remainingQuota)
          .update.apply()
      }
    }

  def update(subsidy: Subsidy): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("UPDATE subsidies SET name = ?, description = ?, income_threshold = ?, remaining_quota = ? WHERE id = ?")
          .bind(subsidy.name, subsidy.description, subsidy.incomeThreshold, subsidy.remainingQuota, subsidy.id)
          .update.apply()
      }
    }

  /** Positive delta restores quota (e.g. a rejected application), negative delta consumes it
    * (e.g. a newly submitted application) — same idea as a library incrementing/decrementing
    * `books.available` on return/borrow.
    */
  def adjustQuota(subsidyId: Int, delta: Int): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("UPDATE subsidies SET remaining_quota = remaining_quota + ? WHERE id = ?")
          .bind(delta, subsidyId)
          .update.apply()
      }
    }

  def searchByNameOrDescription(keyword: String): Try[List[Subsidy]] =
    Try {
      val likePattern = s"%$keyword%"
      DB readOnly { implicit session =>
        SQL("SELECT * FROM subsidies WHERE name LIKE ? OR description LIKE ?")
          .bind(likePattern, likePattern)
          .map(rowMapper)
          .list
          .apply()
      }
    }
