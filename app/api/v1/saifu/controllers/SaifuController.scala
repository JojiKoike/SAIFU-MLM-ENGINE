package api.v1.saifu.controllers

import api.v1.common.{someRemover, RequestMarkerContext, SaifuDefaultActionBuilder}
import api.v1.saifu.models.Saifu
import api.v1.saifu.resourcehandlers.SaifuResourceHandler
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class SaifuController @Inject() (scc: SaifuControllerComponents)(implicit ec: ExecutionContext)
    extends SaifuBaseController(scc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("index: ")
      request.userResource match {
        case Some(userResource) =>
          saifuResourceHandler.all(someRemover(userResource.id)).map { items =>
            Ok(Json.toJson(items))
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }

  def show(saifuID: String): Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace(s"show: $saifuID")
      request.userResource match {
        case Some(userResource) =>
          saifuResourceHandler.lookup(someRemover(userResource.id), saifuID).map { saifu =>
            Ok(Json.toJson(saifu))
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }

  def process: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Process Saifu:")
      Saifu.createSaifuInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuResourceHandler
                .create(someRemover(userResource.id), success)
                .map { _ =>
                  Created
                }
            case None =>
              logger.error("Unauthorized")
              Future.successful(Unauthorized)
          }
        }
      )
    }

  def update: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Update Saifu:")
      Saifu.updateSaifuInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuResourceHandler
                .update(someRemover(userResource.id), success)
                .map { _ =>
                  NoContent
                }
            case None =>
              logger.error("Unauthorized")
              Future.successful(Unauthorized)
          }
        }
      )
    }

}

case class SaifuControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    saifuResourceHandler: SaifuResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

class SaifuBaseController @Inject() (scc: SaifuControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = scc

  def saifuResourceHandler: SaifuResourceHandler = scc.saifuResourceHandler

  def saifuDefaultAction: SaifuDefaultActionBuilder = scc.saifuDefaultActionBuilder
}
