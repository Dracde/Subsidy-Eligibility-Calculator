package model

import scalikejdbc.*
import scala.util.Try


abstract class Repository[T](
    private val tableName: String // private: subclasses read it only through the protected accessor below, never reassign it
):
  protected def tableNameForQueries: String = tableName

  protected def rowMapper(resultSet: WrappedResultSet): T

  def findAll(): Try[List[T]] =
    Try {
      DB readOnly { implicit session =>
        SQL(s"SELECT * FROM $tableNameForQueries").map(rowMapper).list.apply()
      }
    }

  def deleteById(id: Int): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        SQL(s"DELETE FROM $tableNameForQueries WHERE id = ?").bind(id).update.apply()
      }
    }
