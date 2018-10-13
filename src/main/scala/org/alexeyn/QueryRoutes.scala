package org.alexeyn

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.Future

class QueryRoutes(implicit system: ActorSystem) extends JsonCodes {
  lazy val log = Logging(system, classOf[QueryRoutes])

  val routes: Route =
    pathPrefix("api" / "v1" / "cars") {
      concat(
        pathEndOrSingleSlash {
          get {
            parameters('sort) { sort =>
              val cars =
                Future.successful(
                  CarAds(
                    Seq(CarAd(1, "toyota", Fuel.Diesel, 20000, isNew = true, None, Some(LocalDate.of(2010, 4, 22))))
                  )
                )
              complete(cars)
            }
          }
        },
        path(Segment) { id =>
          concat(get {
            val maybeCarAd = Future.successful[Option[CarAd]](None)
            log.debug("Found carAd: {}", maybeCarAd)
            rejectEmptyResponse {
              complete(maybeCarAd)
            }
          })
        }
      )
    }

}
