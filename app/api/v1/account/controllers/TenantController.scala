package api.v1.account.controllers

import api.v1.account.models.Tenant
import api.v1.account.resourcehandlers.TenantResourceHandler
import api.v1.common.{RequestMarkerContext, SaifuDefaultActionBuilder}
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class TenantController @Inject() (cc: TenantControllerComponents)(implicit
    ec: ExecutionContext
) extends TenantBaseController(cc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("index: ")
      tenantResourceHandler.find.map { tenants =>
        Ok(Json.toJson(tenants))
      }
    }

  def process: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("process: ")
      Tenant.createTenantInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          tenantResourceHandler.create(success).map { tenant =>
            Created(Json.toJson(tenant))
          }
        }
      )
    }

  def update: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("update: ")
      Tenant.updateTenantInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          tenantResourceHandler.update(success).map { _ =>
            NoContent
          }
        }
      )
    }

  def delete: Action[AnyContent] =
    saifuDefaultAction.async { implicit request =>
      logger.trace("delete: ")
      Tenant.deleteTenantInput.bindFromRequest.fold(
        failure => {
          Future.successful(BadRequest(failure.errorsAsJson))
        },
        success => {
          tenantResourceHandler.delete(success).map { _ =>
            NoContent
          }
        }
      )
    }

  def show(id: String): Action[AnyContent] = {
    saifuDefaultAction.async { implicit request =>
      logger.trace(s"show: id = $id")
      tenantResourceHandler.lookup(id).map { tenant =>
        Ok(Json.toJson(tenant))
      }
    }
  }
}

/**
  * Packages up the component dependencies for the tenant controller
  * @param saifuDefaultActionBuilder
  * @param tenantResourceHandler
  * @param actionBuilder
  * @param parsers
  * @param messagesApi
  * @param langs
  * @param fileMimeTypes
  * @param executionContext
  */
case class TenantControllerComponents @Inject() (
    saifuDefaultActionBuilder: SaifuDefaultActionBuilder,
    tenantResourceHandler: TenantResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: ExecutionContext
) extends ControllerComponents

/**
  * Base Controller for Tenant
  * @param tcc TenantControllerComponents
  */
class TenantBaseController @Inject() (tcc: TenantControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = tcc

  def saifuDefaultAction: SaifuDefaultActionBuilder = tcc.saifuDefaultActionBuilder

  def tenantResourceHandler: TenantResourceHandler = tcc.tenantResourceHandler
}
