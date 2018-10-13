package org.alexeyn
import java.time.LocalDate

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class QueryRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonCodes {
  lazy val routes: Route = new QueryRoutes().routes

  "QueryRoutes" should {
    "return all users sorted by sort parameter" in {
      val request = HttpRequest(uri = "/api/v1/cars?sort=title")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        println(responseAs[String])
        entityAs[CarAds] should ===(
          CarAds(Seq(CarAd(1, "toyota", Fuel.Diesel, 20000, isNew = true, None, Some(LocalDate.of(2010, 4, 22)))))
        )
      }
    }
  }
}
