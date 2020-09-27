package api.v1

import api.v1.account.resourcehandlers.UserResource
import javax.inject.{Inject, Singleton}
import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.{HttpVerbs, SecretConfiguration}
import play.api.i18n.MessagesApi
import play.api.mvc.{
  ActionBuilder,
  AnyContent,
  BodyParser,
  Cookie,
  MessagesRequestHeader,
  PlayBodyParsers,
  PreferredMessagesProvider,
  Request,
  RequestHeader,
  Result,
  WrappedRequest
}
import services.encryption.{EncryptedCookieBaker, EncryptionService}
import services.session.SessionService

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

package object common {

  val SESSION_ID = "sessionId"

  val SESSION_DATA_COOKIE_NAME = "sessionData"

  /**
    * Default Request Header for SAIFU
    */
  trait SaifuDefaultRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider

  /**
    * Default Request Class for SAIFU
    * @param request
    * @param messagesApi
    * @tparam A
    */
  class SaifuDefaultRequest[A](request: Request[A], val messagesApi: MessagesApi)
      extends WrappedRequest(request)
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
  class SaifuDefaultActionBuilder @Inject() (messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)(implicit
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

      val future = block(new SaifuDefaultRequest(request, messagesApi))

      future.map { result =>
        request.method match {
          case GET | HEAD =>
            result.withHeaders("Cache-Control" -> s"max-age: 100")
          case other =>
            result
        }
      }
    }
  }

  @Singleton
  class UserResourceCookieBakerFactory @Inject() (
      encryptionService: EncryptionService,
      secretConfiguration: SecretConfiguration
  ) {

    def createCookieBaker(secretKey: Array[Byte]): EncryptedCookieBaker[UserResource] = {
      new EncryptedCookieBaker[UserResource](secretKey, encryptionService, secretConfiguration) {
        // Set session expiration duration and cookie name
        override def expirationDate: FiniteDuration = 1.hours
        override def COOKIE_NAME: String            = SESSION_DATA_COOKIE_NAME
      }
    }
  }

  @Singleton
  class SessionGenerator @Inject() (
      sessionService: SessionService,
      encryptionService: EncryptionService,
      userResourceCookieBakerFactory: UserResourceCookieBakerFactory
  )(implicit ec: ExecutionContext) {

    def createSession(userResource: UserResource): Future[(String, Cookie)] = {
      // Create UserResource Cookie encrypted with this specific secret key
      val secretKey          = encryptionService.newSecretKey
      val cookieBaker        = userResourceCookieBakerFactory.createCookieBaker(secretKey)
      val userResourceCookie = cookieBaker.encodeAsCookie(Some(userResource))

      // Create New Session Id and tie it to encrypted UserResource data and then,
      // store the pair in client side cookie
      sessionService
        .create(secretKey)
        .map(sessionId => (sessionId, userResourceCookie))
    }
  }
}
