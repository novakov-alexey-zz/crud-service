package org.alexeyn

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonCodes extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val fuelFormat: RootJsonFormat[Fuel.Value] = enumFormat(Fuel)

  implicit val localDateJsonFormat: JsonFormat[LocalDate] =
    new JsonFormat[LocalDate] {
      private val formatter = DateTimeFormatter.ISO_DATE
      override def write(x: LocalDate): JsValue = JsString(x.format(formatter))

      override def read(value: JsValue): LocalDate = value match {
        case JsString(x) => LocalDate.parse(x)
        case x => throw DeserializationException(s"Wrong time format of $x")
      }
    }

  // Generic Enumeration formatter
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse =>
            throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

  implicit val carAdJsonFormat: RootJsonFormat[CarAd] = jsonFormat7(CarAd)
  implicit val carAdsJsonFormat: RootJsonFormat[CarAds] = jsonFormat1(CarAds)
  implicit val carAdInsertedFormat: RootJsonFormat[CarAddInserted] = jsonFormat1(CarAddInserted)
  implicit val carAdUpdatedFormat: RootJsonFormat[CarAddUpdated] = jsonFormat1(CarAddUpdated)
  implicit val carAdDeletedFormat: RootJsonFormat[CarAddDeleted] = jsonFormat1(CarAddDeleted)
}
