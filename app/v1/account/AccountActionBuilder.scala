package v1.account

import javax.inject.Inject
import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{
  ActionBuilder,
  AnyContent,
  BaseController,
  BodyParser,
  ControllerComponents,
  DefaultActionBuilder,
  MessagesRequestHeader,
  PlayBodyParsers,
  PreferredMessagesProvider,
  Request,
  RequestHeader,
  Result,
  WrappedRequest
}

import scala.concurrent.{ExecutionContext, Future}

trait TenantRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class TenantRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with TenantRequestHeader

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

class TenantActionBuilder @Inject() (messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[TenantRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type TenantRequestBlock[A] = TenantRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: TenantRequestBlock[A]): Future[Result] = {

    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)

    logger.trace(s"invokeBlock: ")

    val future = block(new TenantRequest(request, messagesApi))

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

/**
  * Packages up the component dependencies for the tenant controller
  * @param tenantActionBuilder
  * @param tenantResourceHandler
  * @param actionBuilder
  * @param parsers
  * @param messagesApi
  * @param langs
  * @param fileMimeTypes
  * @param executionContext
  */
case class TenantControllerComponents @Inject() (
    tenantActionBuilder: TenantActionBuilder,
    tenantResourceHandler: TenantResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

class TenantBaseController @Inject() (tcc: TenantControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = tcc

  def TenantAction: TenantActionBuilder = tcc.tenantActionBuilder

  def tenantResourceHandler: TenantResourceHandler = tcc.tenantResourceHandler
}
