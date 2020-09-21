package api.v1.account

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class TenantController @Inject() (cc: TenantControllerComponents)(implicit
    ec: ExecutionContext
) extends TenantBaseController(cc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] =
    TenantAction.async { implicit request =>
      logger.trace("index: ")
      tenantResourceHandler.find.map { tenants =>
        Ok(Json.toJson(tenants))
      }
    }

  def process: Action[AnyContent] =
    TenantAction.async { implicit request =>
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
    TenantAction.async { implicit request =>
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
    TenantAction.async { implicit request =>
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
    TenantAction.async { implicit request =>
      logger.trace(s"show: id = $id")
      tenantResourceHandler.lookup(id).map { tenant =>
        Ok(Json.toJson(tenant))
      }
    }
  }

}
