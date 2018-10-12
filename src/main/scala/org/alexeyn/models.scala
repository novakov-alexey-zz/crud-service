package org.alexeyn

import java.time.LocalDate

import org.alexeyn.Fuel.Fuel

case class CarAd(
  id: Int,
  title: String,
  fuel: Fuel,
  price: Int,
  isNew: Boolean,
  mileage: Option[Int],
  firstRegistration: Option[LocalDate])

object Fuel extends Enumeration {
  type Fuel = Value
  val Gasoline, Diesel = Value
}
