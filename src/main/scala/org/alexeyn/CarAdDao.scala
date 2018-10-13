package org.alexeyn

import java.sql.Timestamp
import java.time._

import org.alexeyn.Fuel.Fuel
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class CarAdDao(db: Database) extends Dao[CarAd, Future] {
  implicit val fuelEnumMapper =
    MappedColumnType.base[Fuel, String](_.toString, Fuel.withName)

  implicit val localDateColumnType = MappedColumnType
    .base[LocalDate, Timestamp](
      d => Timestamp.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant),
      d => LocalDate.from(Instant.ofEpochMilli(d.getTime))
    )

  class CarAds(tag: Tag) extends Table[CarAd](tag, "car_ads") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def title = column[String]("title")
    def fuel = column[Fuel]("vehicle_type")
    def price = column[Int]("price")
    def `new` = column[Boolean]("new")
    def mileage = column[Option[Int]]("mileage", O.Default(None))
    def firstRegistration = column[Option[LocalDate]]("first_registration")

    def * =
      (id, title, fuel, price, `new`, mileage, firstRegistration) <>
        (CarAd.tupled, CarAd.unapply)
  }

  val carAds = TableQuery[CarAds]

  override def createSchema(): Unit = db.run(carAds.schema.create)

  override def insert(ca: CarAd): Future[Int] = db.run(carAds += ca)

  override def selectAll(page: Int = 0, pageSize: Int = 10, sort: String = "id"): Future[Seq[CarAd]] = {
    val q = sort match {
      case "id" => carAds.sortBy(_.id)
      case "title" => carAds.sortBy(_.title)
      case "fuel" => carAds.sortBy(_.fuel)
      case "price" => carAds.sortBy(_.price)
      case "new" => carAds.sortBy(_.`new`)
      case "mileage" => carAds.sortBy(_.mileage)
      case "first_registration" => carAds.sortBy(_.firstRegistration)
    }
    db.run(q.drop(page * pageSize).take(pageSize).result)
  }

  override def select(id: Int): Future[Option[CarAd]] =
    db.run(carAds.filter(_.id === id).take(1).result.headOption)
}
