package org.alexeyn

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future._
import org.alexeyn.TestData._
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonCodes {

  private val mockDao = createMockDao
  private val service = new CarAdService[Future](mockDao)

  val routes: Route = CommandRoutes.routes(service)

  "CommandRoutes" should {
    "insert new carAd and return its id" in {
      val entity = HttpEntity(MediaTypes.`application/json`, toyotaAd.toJson.toString())
      val request = HttpRequest(uri = "/api/v1/cars", method = HttpMethods.POST, entity = entity)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }

    "update existing carAd and return count" in {
      val entity = HttpEntity(MediaTypes.`application/json`, toyotaAd.toJson.toString())
      val request = HttpRequest(uri = s"/api/v1/cars/$adId", method = HttpMethods.PUT, entity = entity)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }

    "delete existing carAd and return count" in {
      val request = HttpRequest(uri = s"/api/v1/cars/$adId", method = HttpMethods.DELETE)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }
  }

  private def commonChecks = {
    status should ===(StatusCodes.OK)
    contentType should ===(ContentTypes.`application/json`)
  }

  private def createMockDao = {
    new Dao[CarAd, Future] {
      override def createSchema(): Future[Unit] = Future.successful()
      override def insert(row: CarAd): Future[Int] = Future.successful(adId)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[CarAd]] = ???
      override def select(id: Int): Future[Option[CarAd]] = ???
      override def delete(id: Int): Future[Int] = Future.successful(adId)
      override def update(id: Int, row: CarAd): Future[Int] = Future.successful(adId)
      override def sortingFields: Set[String] = ???
    }
  }
}
