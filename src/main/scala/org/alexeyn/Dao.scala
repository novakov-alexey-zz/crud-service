package org.alexeyn

trait Dao[T] {

  def createSchema(): Unit

  def insert(row: T): Unit

  def selectAll(sort: String): Seq[T]

  def select(id: Int): Option[T]
}
