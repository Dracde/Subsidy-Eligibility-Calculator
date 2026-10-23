package model

import scalikejdbc.*
import scala.util.Try

class HouseholdRepository extends Repository[Household]("households"):

  override protected def rowMapper(resultSet: WrappedResultSet): Household =
    Household(
      id = resultSet.int("id"),
      fullName = resultSet.string("full_name"),
      icNumber = resultSet.string("ic_number"),
      monthlyIncome = BigDecimal(resultSet.bigDecimal("monthly_income")),
      dependents = resultSet.int("dependents")
    )

  def nextId(): Try[Int] =
    Try {
      DB readOnly { implicit session =>
        SQL("SELECT COALESCE(MAX(id), 0) AS maxId FROM households")
          .map(_.int("maxId")).single.apply().getOrElse(0) + 1
      }
    }

  def insert(household: Household): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("INSERT INTO households (id, full_name, ic_number, monthly_income, dependents) VALUES (?, ?, ?, ?, ?)")
          .bind(household.id, household.fullName, household.icNumber, household.monthlyIncome, household.dependents)
          .update.apply()
      }
    }

  def update(household: Household): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL("UPDATE households SET full_name = ?, ic_number = ?, monthly_income = ?, dependents = ? WHERE id = ?")
          .bind(household.fullName, household.icNumber, household.monthlyIncome, household.dependents, household.id)
          .update.apply()
      }
    }

  def searchByNameOrIc(keyword: String): Try[List[Household]] =
    Try {
      val likePattern = s"%$keyword%"
      DB readOnly { implicit session =>
        SQL("SELECT * FROM households WHERE full_name LIKE ? OR ic_number LIKE ?")
          .bind(likePattern, likePattern)
          .map(rowMapper)
          .list
          .apply()
      }
    }
