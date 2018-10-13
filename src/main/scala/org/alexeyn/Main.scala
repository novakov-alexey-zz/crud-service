package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import cats.instances.future._
import com.typesafe.scalalogging.StrictLogging
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object Main extends App with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("crud-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  //TODO: use macwire
  val db = Database.forConfig("ads")
  val dao = new CarAdDao(db)
  dao.createSchema()

  val service = new CarAdService(dao)
  val routes = concat(new QueryRoutes(service).routes) // ~ new CommandRoutes().routes
  val serverBinding = Http().bindAndHandle(routes, "localhost", 8080) //TODO: pass from Config

  serverBinding.onComplete {
    case Success(bound) =>
      logger.info("Server launched at http://{}:{}/", bound.localAddress.getHostString, bound.localAddress.getPort)
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
      db.close()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
