package v1.account

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}

case class TenantFormInput(name: String, explain: String)

class TenantController @Inject() (cc: TenantControllerComponents)(implicit
    ec: ExecutionContext
) extends TenantBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[TenantFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name"    -> nonEmptyText,
        "explain" -> text
      )(TenantFormInput.apply)(TenantFormInput.unapply)
    )
  }

  def index: Action[AnyContent] =
    TenantAction.async { implicit request =>
      logger.trace("index: ")
      tenantResourceHandler.find.map { tenants =>
        Ok(Json.toJson(tenants)).as("application/json")
      }
    }

  def process: Action[AnyContent] =
    TenantAction.async { implicit request =>
      logger.trace("process: ")
      processJsonPost()
    }

  def show(id: String): Action[AnyContent] = {
    println(id)
    TenantAction.async { implicit request =>
      logger.trace(s"show: id = $id")
      tenantResourceHandler.lookup(id).map { tenant =>
        Ok(Json.toJson(tenant))
      }
    }
  }

  private def processJsonPost[A]()(implicit
      request: TenantRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[TenantFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: TenantFormInput) = {
      tenantResourceHandler.create(input).map { tenant =>
        Created(Json.toJson(tenant))
      }
    }
    form.bindFromRequest().fold(failure, success)
  }
}
