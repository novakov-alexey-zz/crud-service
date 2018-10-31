package org.alexeyn

import cats.Functor
import cats.syntax.functor._
import org.alexeyn.CarAdService._

import scala.language.higherKinds

class CarAdService[F[_]](dao: Dao[CarAd, F])(implicit F: Functor[F]) {

  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Either[String, F[CarAds]] = {
    val sortBy = sort
      .map(s => if (dao.sortingFields.contains(s)) Right(s) else Left(s"Unknown sort field $s"))
      .getOrElse(Right(defaultSortField))

    sortBy.map { s =>
      val res = dao.selectAll(page.getOrElse(0), pageSize.getOrElse(10), s)
      res.map(CarAds)
    }
  }

  def select(id: Int): F[Option[CarAd]] = dao.select(id)

  def insert(carAd: CarAd): Either[String, F[Int]] =
    validateCarAd(carAd).map(_ => dao.insert(carAd))

  def update(id: Int, carAd: CarAd): Either[String, F[Int]] =
    validateCarAd(carAd).map(_ => dao.update(id, carAd))

  def delete(id: Int): F[Int] = dao.delete(id)

  private def validateCarAd(carAd: CarAd): Either[String, Unit] = {
    if (carAd.`new` && carAd.mileage.isDefined)
      Left("Only used car can have non-empty 'mileage'")
    else if (carAd.`new` && carAd.firstRegistration.isDefined)
      Left("Only used car can have non-empty 'first registration' date")
    else Right()
  }
}

object CarAdService {
  val defaultSortField: String = "id"
}
