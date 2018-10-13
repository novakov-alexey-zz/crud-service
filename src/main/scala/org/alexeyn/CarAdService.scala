package org.alexeyn

import org.alexeyn.CarAdService._

class CarAdService(dao: Dao[CarAd]) {

  def selectAll(sort: Option[String]): Either[String, CarAds] = {
    val sortBy = sort
      .map(s => if (sortFields.contains(s)) Right(s) else Left(s"Unknown sort field $s"))
      .getOrElse(Right(defaultSortField))

    sortBy.map(s => CarAds(dao.selectAll(s)))
  }

  def select(id: Int): Option[CarAd] = dao.select(id)
}

object CarAdService {
  val sortFields: Set[String] = Set("id", "title")
  val defaultSortField: String = "id"
}
