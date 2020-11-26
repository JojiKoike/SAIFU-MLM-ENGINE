package api.v1.saifu.controllers

import api.v1.common.{someRemover, RequestMarkerContext, SaifuDefaultActionBuilder}
import api.v1.saifu.resourcehandlers.SaifuHistoryResourceHandler
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  DefaultActionBuilder,
  PlayBodyParsers
}

import scala.concurrent.{ExecutionContext, Future}

class HistoryController @Inject() (shc: SaifuHistoryControllerComponents)(implicit
    ec: ExecutionContext
) extends SaifuHistoryBaseController(shc) {

  private val logger = Logger(getClass)

  def show(
      target: String,
      saifuID: String = "",
      dateFrom: DateTime,
      dateTo: DateTime
  ): Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace(s"index: target $target, From $dateFrom, to $dateTo")
      request.userResource match {
        case Some(userResource) =>
          target match {
            case "User" =>
              saifuHistoryResourceHandler
                .userAll(userResource.id, dateFrom, dateTo)
                .map { items =>
                  Ok(Json.toJson(items))
                }
            case "Tenant" =>
              saifuHistoryResourceHandler
                .tenantAll(someRemover(userResource.tenantID), dateFrom, dateTo)
                .map { items =>
                  Ok(Json.toJson(items))
                }
            case "Saifu" =>
              saifuHistoryResourceHandler
                .lookup(userResource.id, saifuID, dateFrom, dateTo)
                .map { items =>
                  Ok(Json.toJson(items))
                }
            case _ =>
              logger.error("BadRequest")
              Future.successful(BadRequest)
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }
}

case class SaifuHistoryControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    saifuHistoryResourceHandler: SaifuHistoryResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

class SaifuHistoryBaseController @Inject() (shc: SaifuHistoryControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = shc

  def saifuHistoryResourceHandler: SaifuHistoryResourceHandler = shc.saifuHistoryResourceHandler

  def saifuDefaultAction: SaifuDefaultActionBuilder = shc.saifuDefaultActionBuilder
}
