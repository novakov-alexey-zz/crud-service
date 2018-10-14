package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object Main extends App with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("crud-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val mod = new Module()
  val serverBinding = Http().bindAndHandle(mod.routes, "localhost", 8080) //TODO: pass from Config

  serverBinding.onComplete {
    case Success(bound) =>
      logger.info("Server launched at http://{}:{}/", bound.localAddress.getHostString, bound.localAddress.getPort)
    case Failure(e) =>
      logger.error("Server could not start!")
      e.printStackTrace()
      system.terminate()
      mod.close()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
