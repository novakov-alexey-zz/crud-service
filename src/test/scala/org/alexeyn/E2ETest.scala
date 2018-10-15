package org.alexeyn

import java.time.LocalDate

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.alexeyn.RequestsSupport._
import org.alexeyn.TestData._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

class E2ETest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonCodes
    with BeforeAndAfter
    with ForAllTestContainer {

  override val container = PostgreSQLContainer()

  lazy val cfg: Config = ConfigFactory.load(
    ConfigFactory
      .parseMap(
        Map(
          "port" -> container.mappedPort(5432),
          "url" -> container.jdbcUrl,
          "user" -> container.username,
          "password" -> container.password
        ).asJava
      ).atKey("storage")
      .withFallback(ConfigFactory.load())
  )

  lazy val mod = new Module(createSchema = false, cfg)

  before {
    Await.ready(mod.db.run(mod.dao.dropSchema()), 10.seconds)
    Await.ready(mod.dao.createSchema(), 10.seconds)
  }

  "CarAds service" should {
    "insert new carAds" in {
      insertData()
    }

    "select carAds sorted by any field" in {
      insertData()

      checkSorting("id", _.id)
      checkSorting("title", _.title)
      checkSorting("fuel", _.fuel)
      checkSorting("price", _.price)
      checkSorting("new", _.`new`)

      // None/Null is the highest order in Postgres, so default is MaxValue then
      implicit val mileageOrdering: Ordering[Option[Int]] = Ordering.by(_.getOrElse(Int.MaxValue))
      checkSorting("mileage", _.mileage)

      // None/Null is the highest order in Postgres, so default is Max LocalDate then
      implicit val localDateOrdering: Ordering[Option[LocalDate]] = Ordering.by(_.getOrElse(LocalDate.MAX).toEpochDay)
      checkSorting("first_registration", _.firstRegistration)
    }

    "select carAds by id" in {
      insertData()
      mockData.indices.foreach(i => selectAndCheck(i + 1))
    }

    "update carAds by id" in {
      insertData()
      mockData.indices.foreach(i => updateAndCheck(i + 1))
    }

    "delete carAds by id" in {
      insertData()
      deleteData()
    }

  }

  private def deleteData(): Unit = {
    mockData.indices.foreach(i => deleteAndCheck(i + 1))
  }

  private def insertData(): Unit = {
    mockData.foreach { c =>
      val insert = insertRequest(c)
      insertAndCheck(insert)
    }
  }

  private def deleteAndCheck(id: Int) = {
    val delete = deleteRequest(id)
    delete ~> mod.routes ~> check {
      commonChecks
      val result = entityAs[CommandResult]
      result.count should ===(1)
    }

    val select = selectByRequest(id)
    select ~> Route.seal(mod.routes) ~> check {
      status should ===(StatusCodes.NotFound)
    }
  }

  private def updateAndCheck(id: Int) = {
    val prefix = "updated"
    val ad = mockData(id - 1)
    val update = updateRequest(ad.copy(id, title = prefix + ad.title), id)

    update ~> mod.routes ~> check {
      commonChecks
      val result = entityAs[CommandResult]
      result.count should ===(1)
    }

    val select = selectByRequest(id)
    select ~> mod.routes ~> check {
      commonChecks
      val selected = entityAs[CarAd]
      selected should ===(ad.copy(id, prefix + ad.title))
    }
  }

  private def selectAndCheck(id: Int) = {
    val select = selectByRequest(id)
    select ~> mod.routes ~> check {
      commonChecks
      val selected = entityAs[CarAd]
      selected should ===(mockData(id - 1).copy(id))
    }
  }
  private def checkSorting[T](field: String, sort: CarAd => T)(implicit ev: Ordering[T]) = {
    val sorted = mockData.sortBy(sort)
    val isSortedByField = (seq: Seq[CarAd]) => seq.map(sort) === sorted.map(sort)
    selectAndCheck(selectAllRequest(field), sorted, isSortedByField)
  }

  private def insertAndCheck(insert: HttpRequest) = {
    insert ~> mod.routes ~> check {
      commonChecks
      val count = entityAs[CommandResult].count
      count should ===(1)
    }
  }

  private def selectAndCheck(select: HttpRequest, expected: Seq[CarAd], verify: Seq[CarAd] => Boolean) = {
    select ~> mod.routes ~> check {
      commonChecks
      val ads = entityAs[CarAds]

      ads.carAds.length should ===(expected.length)
      ads.carAds.map(_.title).toSet should ===(expected.map(_.title).toSet)
      verify(ads.carAds) should ===(true)
    }
  }

  private def commonChecks = {
    status should ===(StatusCodes.OK)
    contentType should ===(ContentTypes.`application/json`)
  }
}
