package api.v1.saifu.controllers

import api.v1.common.{ERROR_CODE, SaifuDefaultActionBuilder}
import api.v1.saifu.models.SaifuTransfer
import api.v1.saifu.resourcehandlers.TransferResourceHandler
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class TransferController @Inject() (tsc: TransferControllerComponents)
                                   (implicit ec: ExecutionContext) extends TransferBaseController(tsc){

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("index: ")
      request.userResource match {
        case Some(userResource) =>
          transferResourceHandler.all(userResource.id).map { items =>
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
          transferResourceHandler.lookup(userResource.id, saifuID).map {
            items => Ok(Json.toJson(items))
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }

  def process: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Process SaifuTransfer:")
       SaifuTransfer.createSaifuTransferInput.bindFromRequest.fold(
         failure => {
           Future.successful(BadRequest(failure.errorsAsJson))
         },
         success => {
           request.userResource match {
             case Some(userResource) =>
               transferResourceHandler
                .create(success)
                .map {
                  case ERROR_CODE => BadRequest
                  case _ => Created
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
      logger.trace("Update SaifuTransfer:")
      SaifuTransfer.updateSaifuTransferInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              transferResourceHandler
                .update(success).map { _ =>
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

case class TransferControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    transferResourceHandler: TransferResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
                                                       ) extends ControllerComponents

class TransferBaseController @Inject() (tcc: TransferControllerComponents) extends BaseController {
  override protected def controllerComponents: ControllerComponents = tcc

  def transferResourceHandler: TransferResourceHandler = tcc.transferResourceHandler

  def saifuDefaultAction = tcc.saifuDefaultActionBuilder
}
