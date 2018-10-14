package org.alexeyn

import java.time.LocalDate

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future._
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonCodes {
  val adId = 1
  val toyotaAd = CarAd(adId, "toyota", Fuel.Diesel, 20000, `new` = true, None, Some(LocalDate.of(2010, 4, 22)))

  private val mockDao = createMockDao
  private val service = new CarAdService[Future](mockDao)

  val routes: Route = new CommandRoutes(service).routes

  "CommandRoutes" should {
    "insert new carAd and return its id" in {
      val entity = HttpEntity(MediaTypes.`application/json`, toyotaAd.toJson.toString())
      val request = HttpRequest(uri = "/api/v1/cars", method = HttpMethods.POST, entity = entity)

      request ~> routes ~> check {
        commonChecks
        val id = entityAs[CarAddInserted].insertedId
        id should ===(adId)
      }
    }

    "update existing carAd and return its id" in {
      val entity = HttpEntity(MediaTypes.`application/json`, toyotaAd.toJson.toString())
      val request = HttpRequest(uri = s"/api/v1/cars/$adId", method = HttpMethods.PUT, entity = entity)

      request ~> routes ~> check {
        commonChecks
        val id = entityAs[CarAddUpdated].updatedId
        id should ===(adId)
      }
    }

    "delete existing carAd and return its id" in {
      val request = HttpRequest(uri = s"/api/v1/cars/$adId", method = HttpMethods.DELETE)

      request ~> routes ~> check {
        commonChecks
        val id = entityAs[CarAddDeleted].deletedId
        id should ===(adId)
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
      override def insert(row: CarAd): Future[Int] = Future.successful(adId)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[CarAd]] = ???
      override def select(id: Int): Future[Option[CarAd]] = ???
      override def delete(id: Int): Future[Int] = Future.successful(adId)
      override def update(id: Int, row: CarAd): Future[Int] = Future.successful(adId)
    }
  }
}
