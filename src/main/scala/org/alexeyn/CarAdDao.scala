package org.alexeyn

import java.sql.Timestamp
import java.time._

import org.alexeyn.Fuel.Fuel
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.Future

class CarAdDao(db: Database) extends Dao[CarAd, Future] {
  implicit val fuelEnumMapper =
    MappedColumnType.base[Fuel, String](_.toString, Fuel.withName)

  implicit val localDateColumnType = MappedColumnType
    .base[LocalDate, Timestamp](
      d => Timestamp.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant),
      d => d.toLocalDateTime.toLocalDate
    )

  class CarAds(tag: Tag) extends Table[CarAd](tag, "car_ads") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def title = column[String]("title")
    def fuel = column[Fuel]("fuel")
    def price = column[Int]("price")
    def `new` = column[Boolean]("new")
    def mileage = column[Option[Int]]("mileage", O.Default(None))
    def firstRegistration = column[Option[LocalDate]]("first_registration")

    def * =
      (id, title, fuel, price, `new`, mileage, firstRegistration) <>
        (CarAd.tupled, CarAd.unapply)
  }

  val carAds = TableQuery[CarAds]

  private val sorting = Map(
    "id" -> carAds.sortBy(_.id),
    "title" -> carAds.sortBy(_.title),
    "fuel" -> carAds.sortBy(_.fuel),
    "price" -> carAds.sortBy(_.price),
    "new" -> carAds.sortBy(_.`new`),
    "mileage" -> carAds.sortBy(_.mileage),
    "first_registration" -> carAds.sortBy(_.firstRegistration)
  )

  override def createSchema(): Future[Unit] = db.run(carAds.schema.create)

  override def sortingFields: Set[String] = sorting.keys.toSet

  // for testing purpose only
  def dropSchema(): FixedSqlAction[Unit, NoStream, Effect.Schema] = carAds.schema.drop

  /**
   * inserts new CarAd supplied CarAd.id will be ignored, since id spec is auto-increment
   */
  override def insert(ca: CarAd): Future[Int] = db.run(carAds += ca)

  override def selectAll(page: Int = 0, pageSize: Int = 10, sort: String): Future[Seq[CarAd]] = {
    sorting.get(sort) match {
      case Some(q) => db.run(q.drop(page * pageSize).take(pageSize).result)
      case None => Future.failed(new RuntimeException(s"Unknown sorting field: $sort"))
    }
  }

  override def select(id: Int): Future[Option[CarAd]] =
    db.run(carAds.filter(_.id === id).take(1).result.headOption)

  override def update(id: Int, row: CarAd): Future[Int] = db.run(carAds.filter(_.id === id).update(row))

  override def delete(id: Int): Future[Int] = db.run(carAds.filter(_.id === id).delete)
}
