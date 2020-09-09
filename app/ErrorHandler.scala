import javax.inject.{Inject, Provider}
import org.slf4j.LoggerFactory
import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}
import play.core.SourceMapper

import scala.concurrent.Future

class ErrorHandler(
    environment: Environment,
    configuration: Configuration,
    sourceMapper: Option[SourceMapper] = None,
    optionRouter: => Option[Router] = None
) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, optionRouter) {

  private val logger = LoggerFactory.getLogger("application.ErrorHandler")

  @Inject
  def this(
      environment: Environment,
      configuration: Configuration,
      sourceMapper: OptionalSourceMapper,
      router: Provider[Router]
  ) = {
    this(environment, configuration, sourceMapper.sourceMapper, Some(router.get))
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.debug(
      s"onClientError: statusCode=$statusCode, uri = ${request.uri}, message = $message"
    )

    Future.successful {
      val result = statusCode match {
        case BAD_REQUEST =>
          Results.BadRequest(message)
        case FORBIDDEN =>
          Results.Forbidden(message)
        case NOT_FOUND =>
          Results.NotFound(message)
        case clientError if statusCode >= 400 && statusCode <= 500 =>
          Results.Status(statusCode)
        case nonClientError =>
          val msg =
            s"onClientError invoked with non client error status code $statusCode: $message"
          throw new IllegalArgumentException(msg)
      }
      result
    }
  }

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful(
      InternalServerError(Json.obj("exception" -> exception.toString))
    )
  }

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful(
      InternalServerError
    )
  }
}
