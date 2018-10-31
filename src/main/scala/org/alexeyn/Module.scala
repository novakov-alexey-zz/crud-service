package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import cats.instances.future._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.http.{CommandRoutes, QueryRoutes}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class Module(createSchema: Boolean = true, cfg: Config = ConfigFactory.load())(
  implicit system: ActorSystem,
  executionContext: ExecutionContext
) extends StrictLogging {

  val db = Database.forConfig("storage", cfg)
  val dao = new CarAdDao(db)
  val service = new CarAdService(dao)
  val routes: Route = concat(QueryRoutes.routes(service), CommandRoutes.routes(service))

  if (createSchema) _createSchema()

  private def _createSchema(): Unit =
    dao.createSchema().failed.foreach(t => logger.error(s"Failed to create schema: $t"))

  def close(): Unit = db.close()
}
