package org.alexeyn

import cats.Functor
import cats.syntax.functor._
import org.alexeyn.CarAdService._

import scala.language.higherKinds

class CarAdService[F[_]](dao: Dao[CarAd, F])(implicit F: Functor[F]) {

  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Either[String, F[CarAds]] = {
    val sortBy = sort
      .map(s => dao.sortingFields.find(_ == s).toRight(s"Unknown sort field $s"))
      .getOrElse(Right(DefaultSortField))

    sortBy.map { sort =>
      dao
        .selectAll(page.getOrElse(DefaultPage), pageSize.getOrElse(DefaultPageSize), sort)
        .map(CarAds)
    }
  }

  def select(id: Int): F[Option[CarAd]] = dao.select(id)

  def insert(carAd: CarAd): Either[String, F[Int]] =
    validateCarAd(carAd).map(_ => dao.insert(carAd))

  def update(id: Int, carAd: CarAd): Either[String, F[Int]] =
    validateCarAd(carAd).map(_ => dao.update(id, carAd))

  def delete(id: Int): F[Int] = dao.delete(id)

  private def validateCarAd(carAd: CarAd): Either[String, Unit] = carAd match {
    case CarAd(_, _, _, _, true, Some(_), _) => Left("Only 'used' car can have non-empty 'mileage'")
    case CarAd(_, _, _, _, true, _, Some(_)) => Left("Only 'used' car can have non-empty 'first registration' date")
    case _ => Right()
  }
}

object CarAdService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
