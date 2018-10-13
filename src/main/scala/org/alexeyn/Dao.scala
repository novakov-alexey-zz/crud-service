package org.alexeyn

trait Dao[T, U[_]] {

  def createSchema(): Unit

  def insert(row: T): U[Int]

  def selectAll(page: Int, pageSize: Int, sort: String): U[Seq[T]]

  def select(id: Int): U[Option[T]]
}
