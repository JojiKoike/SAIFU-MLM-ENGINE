package api.v1.common

import api.v1.account.resourcehandlers.UserResource
import javax.inject.Inject
import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{
  ActionBuilder,
  AnyContent,
  BodyParser,
  MessagesRequestHeader,
  PlayBodyParsers,
  PreferredMessagesProvider,
  Request,
  RequestHeader,
  Result,
  WrappedRequest
}
import services.session.SessionService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Default Request Header for SAIFU
  */
trait SaifuDefaultRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider {
  def userResource: Option[UserResource]
}

/**
  * Default Request Class for SAIFU
  * @param request
  * @param messagesApi
  * @tparam A
  */
class SaifuDefaultRequest[A](
    request: Request[A],
    val userResource: Option[UserResource],
    val messagesApi: MessagesApi
) extends WrappedRequest(request)
    with SaifuDefaultRequestHeader

/**
  * Provides an implicit marker that will show the request in all logger statements.
  */
trait RequestMarkerContext {
  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id"   -> request.id) &&
      marker("host" -> request.host) &&
      marker("remoteAddress" -> request.remoteAddress)
    }
  }
}

/**
  * Default Action Builder for SAIFU
  * @param messagesApi
  * @param playBodyParsers
  * @param executionContext
  */
class SaifuDefaultActionBuilder @Inject() (
    sessionService: SessionService,
    factory: UserResourceCookieBakerFactory,
    messagesApi: MessagesApi,
    playBodyParsers: PlayBodyParsers
)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[SaifuDefaultRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type DefaultRequestBlock[A] = SaifuDefaultRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: DefaultRequestBlock[A]): Future[Result] = {

    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)

    logger.trace(s"invokeBlock: ")

    val maybeFutureResult: Option[Future[Result]] = for {
      sessionId          <- request.session.get(SESSION_ID)
      userResourceCookie <- request.cookies.get(SESSION_DATA_COOKIE_NAME)
    } yield {
      // Lookup Session Secret key in Akka Distributed Storage
      sessionService.lookup(sessionId).flatMap {
        case Some(secretKey) =>
          val cookieBaker       = factory.createCookieBaker(secretKey)
          val maybeUserResource = cookieBaker.decodeFromCookie(Some(userResourceCookie))
          block(new SaifuDefaultRequest[A](request, maybeUserResource, messagesApi))
        case None =>
          Future.successful(Unauthorized)
      }
    }
    maybeFutureResult
      .getOrElse(block(new SaifuDefaultRequest[A](request, None, messagesApi)))
  }
}
