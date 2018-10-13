package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("crud-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val dao = new CarAdDao
  val service = new CarAdService(dao)
  lazy val routes = new QueryRoutes(service).routes// ~ new CommandRoutes().routes
  val serverBinding = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server launced at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
