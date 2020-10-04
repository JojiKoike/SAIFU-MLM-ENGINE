package api.v1.saifu.controllers

import api.v1.common.{someRemover, RequestMarkerContext, SaifuDefaultActionBuilder}
import api.v1.saifu.models.SaifuCategory
import api.v1.saifu.resourcehandlers.SaifuCategoryResourceHandler
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class SaifuCategoryController @Inject() (scc: SaifuCategoryControllerComponents)(implicit ec: ExecutionContext)
    extends SaifuCategoryBaseController(scc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("index: ")
      request.userResource match {
        case Some(userResource) =>
          saifuCategoryResourceHandler.all(someRemover(userResource.tenantID)).map { categories =>
            Ok(Json.toJson(categories))
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }

  def show(id: String): Action[AnyContent] = {
    saifuDefaultAction.async { implicit request =>
      logger.trace(s"sho: id = $id")
      request.userResource match {
        case Some(userResource) =>
          saifuCategoryResourceHandler.lookup(someRemover(userResource.tenantID), id).map { category =>
            Ok(Json.toJson(category))
          }
        case None =>
          logger.error("Unauthorized")
          Future.successful(Unauthorized)
      }
    }
  }

  def processMain: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      SaifuCategory.createMainCategoryInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuCategoryResourceHandler
                .createMain(someRemover(userResource.tenantID), success)
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

  def processSub: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Process Sub:")
      SaifuCategory.createSubCategoryInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuCategoryResourceHandler
                .createSub(someRemover(userResource.tenantID), success)
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

  def updateMain(): Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Update Main:")
      SaifuCategory.updateMainCategoryInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuCategoryResourceHandler
                .updateMain(someRemover(userResource.tenantID), success)
                .map { _ =>
                  NoContent
                }
            case None =>
              logger.error("Unauthorized")
              Future.successful(InternalServerError)
          }
        }
      )
    }

  def updateSub(): Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("Update Sub:")
      SaifuCategory.updateSubCategoryInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          request.userResource match {
            case Some(userResource) =>
              saifuCategoryResourceHandler
                .updateSub(someRemover(userResource.tenantID), success)
                .map { _ => NoContent }
            case None =>
              logger.error("Unauthorized")
              Future.successful(InternalServerError)
          }
        }
      )
    }

}

case class SaifuCategoryControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    saifuCategoryResourceHandler: SaifuCategoryResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

class SaifuCategoryBaseController @Inject() (scc: SaifuCategoryControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = scc

  def saifuCategoryResourceHandler: SaifuCategoryResourceHandler = scc.saifuCategoryResourceHandler

  def saifuDefaultAction: SaifuDefaultActionBuilder = scc.saifuDefaultActionBuilder
}