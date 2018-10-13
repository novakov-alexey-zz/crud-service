package org.alexeyn

import java.time.LocalDate

import org.alexeyn.Fuel.Fuel

final case class CarAd(
  id: Int,
  title: String,
  fuel: Fuel,
  price: Int,
  `new`: Boolean,
  mileage: Option[Int],
  firstRegistration: Option[LocalDate]
)

final case class CarAds(carAds: Seq[CarAd])

object Fuel extends Enumeration {
  type Fuel = Value
  val Gasoline, Diesel = Value
}

final case class CarAddInserted(insertedId: Int)
final case class CarAddUpdated(updatedId: Int)
final case class CarAddDeleted(deletedId: Int)
