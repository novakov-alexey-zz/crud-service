package org.alexeyn

class CarAdDao extends Dao[CarAd] {

  override def createSchema(): Unit = ???
  override def insert(row: CarAd): Unit = ???
  override def selectAll(sort: String): Seq[CarAd] = ???
  override def select(id: Int): Option[CarAd] = ???
}
