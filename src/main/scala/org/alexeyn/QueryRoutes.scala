package org.alexeyn

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.{ExecutionContext, Future}

class QueryRoutes(service: CarAdService)(implicit ec: ExecutionContext, system: ActorSystem) extends JsonCodes {
  lazy val log = Logging(system, classOf[QueryRoutes])

  val routes: Route =
    pathPrefix("api" / "v1" / "cars") {
      concat(
        pathEndOrSingleSlash {
          get {
            parameters('sort.?) { sort =>
              log.debug("Select all sorted by '{}'", sort)
              val cars = Future(service.selectAll(sort))
              complete(cars)
            }
          }
        },
        path(IntNumber) { id =>
          concat(get {
            val maybeCarAd = service.select(id)
            log.debug("Found carAd: {}", maybeCarAd)
            rejectEmptyResponse {
              complete(maybeCarAd)
            }
          })
        }
      )
    }

}
