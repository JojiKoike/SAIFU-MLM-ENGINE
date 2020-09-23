package api.v1.account.controllers

import api.v1.account.models.Role
import api.v1.account.resourcehandlers.RoleResourceHandler
import api.v1.common.{RequestMarkerContext, SaifuDefaultActionBuilder}
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class RoleController @Inject() (cc: RoleControllerComponents)(implicit ec: ExecutionContext)
    extends RoleBaseController(cc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("index: ")
      roleResourceHandler.find.map { roles =>
        Ok(Json.toJson(roles))
      }
    }

  def process: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("process: ")
      Role.createRoleInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          roleResourceHandler.create(success).map { role =>
            Created(Json.toJson(role))
          }
        }
      )
    }

  def update: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("update:")
      Role.updateRoleInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          roleResourceHandler.update(success).map { _ =>
            NoContent
          }
        }
      )
    }

  def delete: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("delete: ")
      Role.deleteRoleInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          roleResourceHandler.delete(success).map { _ =>
            NoContent
          }
        }
      )
    }

  def show(id: String): Action[AnyContent] = {
    saifuDefaultAction.async { implicit request =>
      logger.trace(s"show: id = $id")
      roleResourceHandler.lookup(id).map { role =>
        Ok(Json.toJson(role))
      }
    }
  }

}

/**
  * Packages up the component dependencies for role controller
  * @param saifuDefaultActionBuilder
  * @param roleResourceHandler
  * @param actionBuilder
  * @param parsers
  * @param messagesApi
  * @param langs
  * @param fileMimeTypes
  * @param executionContext
  */
case class RoleControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    roleResourceHandler: RoleResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

/**
  * Base Controller for Role
  * @param rcc RoleControllerComponents : RoleController Dependency Bundler
  */
class RoleBaseController @Inject() (rcc: RoleControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = rcc

  def saifuDefaultAction: SaifuDefaultActionBuilder = rcc.saifuDefaultActionBuilder

  def roleResourceHandler: RoleResourceHandler = rcc.roleResourceHandler
}
