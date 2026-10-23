package model

import scalikejdbc.*
import scala.util.Try

object DatabaseConfig:

  private val jdbcUrl = "jdbc:derby:subsidydb;create=true"
  private val jdbcDriver = "org.apache.derby.jdbc.EmbeddedDriver"

  def init(): Try[Unit] =
    Try {
      Class.forName(jdbcDriver)
      ConnectionPool.singleton(jdbcUrl, "app", "app")
      createTablesIfMissing()
      seedSampleDataIfEmpty()
    }

  private def createTablesIfMissing(): Unit =
    DB autoCommit { implicit session =>
      Try { 
        SQL("""CREATE TABLE households (
                 id INTEGER PRIMARY KEY,
                 full_name VARCHAR(120) NOT NULL,
                 ic_number VARCHAR(20) NOT NULL,
                 monthly_income DECIMAL(10,2) NOT NULL,
                 dependents INTEGER NOT NULL
               )""").execute.apply()
      }
      Try {
        SQL("""CREATE TABLE subsidies (
                 id INTEGER PRIMARY KEY,
                 name VARCHAR(120) NOT NULL,
                 description VARCHAR(255) NOT NULL,
                 income_threshold DECIMAL(10,2) NOT NULL,
                 remaining_quota INTEGER NOT NULL
               )""").execute.apply()
      }
      Try {
        SQL("""CREATE TABLE applications (
                 id INTEGER PRIMARY KEY,
                 household_id INTEGER NOT NULL,
                 subsidy_id INTEGER NOT NULL,
                 applied_date DATE NOT NULL,
                 review_deadline DATE NOT NULL,
                 status VARCHAR(20) NOT NULL
               )""").execute.apply()
      }
    }

  private def seedSampleDataIfEmpty(): Unit =
    DB autoCommit { implicit session =>
      val householdCount =
        SQL("SELECT COUNT(*) AS total FROM households").map(_.int("total")).single.apply().getOrElse(0)
      if householdCount == 0 then
        SQL("INSERT INTO households (id, full_name, ic_number, monthly_income, dependents) VALUES (?, ?, ?, ?, ?)")
          .bind(1, "Aminah binti Yusof", "900101-10-1234", BigDecimal(1500.00), 3).update.apply()
        SQL("INSERT INTO households (id, full_name, ic_number, monthly_income, dependents) VALUES (?, ?, ?, ?, ?)")
          .bind(2, "Ramesh a/l Kumar", "880212-14-5678", BigDecimal(3200.00), 1).update.apply()
        SQL("INSERT INTO households (id, full_name, ic_number, monthly_income, dependents) VALUES (?, ?, ?, ?, ?)")
          .bind(3, "Siti Nur Balqis", "950630-08-4321", BigDecimal(1800.00), 2).update.apply()

      val subsidyCount =
        SQL("SELECT COUNT(*) AS total FROM subsidies").map(_.int("total")).single.apply().getOrElse(0)
      if subsidyCount == 0 then
        SQL("""INSERT INTO subsidies (id, name, description, income_threshold, remaining_quota)
               VALUES (?, ?, ?, ?, ?)""")
          .bind(1, "Food Assistance Subsidy", "Monthly grocery vouchers for low-income households",
               BigDecimal(2000.00), 50).update.apply()
        SQL("""INSERT INTO subsidies (id, name, description, income_threshold, remaining_quota)
               VALUES (?, ?, ?, ?, ?)""")
          .bind(2, "Housing Rental Subsidy", "Partial monthly rental coverage for low-asset households",
               BigDecimal(3000.00), 30).update.apply()
        SQL("""INSERT INTO subsidies (id, name, description, income_threshold, remaining_quota)
               VALUES (?, ?, ?, ?, ?)""")
          .bind(3, "Education Support Grant", "School-fee and supplies support for households with dependents",
               BigDecimal(2500.00), 40).update.apply()
    }
