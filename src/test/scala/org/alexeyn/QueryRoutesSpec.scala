package org.alexeyn

import java.time.LocalDate

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class QueryRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonCodes {
  private val mockData =
    IndexedSeq(
      CarAd(2, "honda", Fuel.Gasoline, 2000, `new` = false, Some(20000), Some(LocalDate.of(2000, 4, 22))),
      CarAd(1, "toyota", Fuel.Diesel, 20000, `new` = true, None, Some(LocalDate.of(2010, 4, 22))),
      CarAd(3, "ford", Fuel.Gasoline, 2000, `new` = true, None, Some(LocalDate.of(2010, 5, 12)))
    )

  private val mockDao = createMockDao
  private val service = new CarAdService[Future](mockDao)

  val routes: Route = new QueryRoutes(service).routes

  "QueryRoutes" should {
    "return all carAds sorted by some parameter" in {
      val request = HttpRequest(uri = "/api/v1/cars?sort=title")

      request ~> routes ~> check {
        commonChecks
        val ads = entityAs[CarAds].carAds
        ads.length should ===(3)
        ads.map(_.title) should ===(Seq("ford", "honda", "toyota"))
      }
    }

    "return all carAds sorted by id by default" in {
      val request = HttpRequest(uri = "/api/v1/cars")

      request ~> routes ~> check {
        commonChecks
        val ads = entityAs[CarAds].carAds
        ads.length should ===(3)
        ads.map(_.id) should ===(Seq(1, 2, 3))
      }
    }
  }

  private def commonChecks = {
    status should ===(StatusCodes.OK)
    contentType should ===(ContentTypes.`application/json`)
  }

  private def createMockDao = {
    new Dao[CarAd, Future] {
      override def createSchema(): Unit = ()
      override def insert(row: CarAd): Future[Int] = Future.successful(1)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[CarAd]] = {
        sort match {
          case "id" => Future.successful(mockData.sortBy(_.id))
          case "title" => Future.successful(mockData.sortBy(_.title))
        }
      }
      override def select(id: Int): Future[Option[CarAd]] = Future.successful(mockData.lift(id))
      override def delete(id: Int): Future[Int] = ???
      override def update(id: Int, row: CarAd): Future[Int] = ???
    }
  }
}
