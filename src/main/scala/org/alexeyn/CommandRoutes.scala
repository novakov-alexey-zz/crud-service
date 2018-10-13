package org.alexeyn

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.GenericMarshallers._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class CommandRoutes(service: CarAdService[Future])(implicit ec: ExecutionContext, system: ActorSystem)
    extends JsonCodes {
  lazy val log = Logging(system, classOf[QueryRoutes])

  val routes: Route =
    pathPrefix("api" / "v1" / "cars") {
      concat(
        pathEndOrSingleSlash {
          post {
            entity(as[CarAd]) { carAd =>
              log.debug("Create new ad '{}'", carAd)
              val inserted = service.insert(carAd)
              complete {
                toCommandResponse(inserted, CarAddInserted)
              }
            }
          }
        },
        path(IntNumber) { id =>
          concat(put {
            entity(as[CarAd]) { carAd =>
              log.debug("Update carAd: {}", carAd)
              val updated = service.update(id, carAd)
              complete {
                toCommandResponse(updated, CarAddUpdated)
              }
            }
          })
        },
        path(IntNumber) { id =>
          concat(delete {
            log.debug("Delete carAd: {}", id)
            val deleted = service.delete(id)
            complete {
              toCommandResponse(deleted, CarAddDeleted)
            }
          })
        }
      )
    }

  private def toCommandResponse[T](id: Future[Int], f: Int => T)(implicit ev: JsonWriter[T]) = {
    id.map(
      i => HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, f(i).toJson.toString()))
    )
  }
}
