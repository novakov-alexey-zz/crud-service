package org.alexeyn

import cats.Functor
import org.alexeyn.CarAdService._

import scala.language.higherKinds

class CarAdService[F[_]: Functor](dao: Dao[CarAd, F])(implicit F: Functor[F]) {
  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Either[String, F[CarAds]] = {
    val sortBy = sort
      .map(s => if (sortFields.contains(s)) Right(s) else Left(s"Unknown sort field $s"))
      .getOrElse(Right(defaultSortField))

    sortBy.map { s =>
      val res = dao.selectAll(page.getOrElse(0), pageSize.getOrElse(10), s)
      F.map(res)(sa => CarAds(sa))
    }
  }

  def select(id: Int): F[Option[CarAd]] = dao.select(id)

  def insert(carAd: CarAd): F[Int] = dao.insert(carAd) //TODO: validate carAd before insert

  def update(id: Int, carAd: CarAd): F[Int] = dao.update(id, carAd) //TODO: validate carAd before update

  def delete(id: Int): F[Int] = dao.delete(id)
}

object CarAdService {
  val sortFields: Set[String] = Set("id", "title")
  val defaultSortField: String = "id"
}
