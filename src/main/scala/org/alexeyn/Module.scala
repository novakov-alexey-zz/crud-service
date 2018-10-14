package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import cats.instances.future._
import com.typesafe.scalalogging.StrictLogging
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class Module(implicit system: ActorSystem, executionContext: ExecutionContext) extends StrictLogging {
  val db = Database.forConfig("ads")
  val dao = new CarAdDao(db)
  dao.createSchema().failed.foreach(t => logger.error(s"Failed to create schema: t"))

  val service = new CarAdService(dao)
  val routes: Route = concat(QueryRoutes.routes(service), CommandRoutes.routes(service))

  def close(): Unit = {
    db.close()
  }
}
