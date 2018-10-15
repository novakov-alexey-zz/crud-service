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

object CommandRoutes extends JsonCodes {

  def routes(service: CarAdService[Future])(implicit ec: ExecutionContext, system: ActorSystem): Route = {
    lazy val log = Logging(system, CommandRoutes.getClass)

    pathPrefix("api" / "v1" / "cars") {
      concat(
        pathEndOrSingleSlash {
          post {
            entity(as[CarAd]) { carAd =>
              log.debug("Create new ad '{}'", carAd)
              val inserted = service.insert(carAd)
              complete {
                toCommandResponse(inserted, CommandResult)
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
                toCommandResponse(updated, CommandResult)
              }
            }
          })
        },
        path(IntNumber) { id =>
          concat(delete {
            log.debug("Delete carAd: {}", id)
            val deleted = service.delete(id)
            complete {
              toCommandResponse(Right(deleted), CommandResult)
            }
          })
        }
      )
    }
  }

  private def toCommandResponse[T](
    count: Either[String, Future[Int]],
    f: Int => T
  )(implicit ev: JsonWriter[T], ec: ExecutionContext): Future[HttpResponse] = {

    count match {
      case Right(c) =>
        c.map(i => {
          val entity = HttpEntity(ContentTypes.`application/json`, f(i).toJson.toString())
          HttpResponse(StatusCodes.OK, entity = entity)
        })
      case Left(e) =>
        Future.successful(HttpResponse(StatusCodes.PreconditionFailed, entity = e))
    }
  }
}
