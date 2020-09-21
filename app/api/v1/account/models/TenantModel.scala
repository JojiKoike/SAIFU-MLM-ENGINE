package api.v1.account.models

import play.api.data.Form
import play.api.data.Forms._

// Tenant
case class DeleteTenantInput(id: String)
case class CreateTenantInput(name: String, explain: String)
case class UpdateTenantInput(id: String, name: String, explain: String)

object Tenant {
  val createTenantInput: Form[CreateTenantInput] = Form {
    mapping(
      "name"    -> nonEmptyText,
      "explain" -> text
    )(CreateTenantInput.apply)(CreateTenantInput.unapply)
  }

  val updateTenantInput: Form[UpdateTenantInput] = Form {
    mapping(
      "id"      -> nonEmptyText,
      "name"    -> nonEmptyText,
      "explain" -> text
    )(UpdateTenantInput.apply)(UpdateTenantInput.unapply)
  }

  val deleteTenantInput: Form[DeleteTenantInput] = Form {
    mapping(
      "id" -> nonEmptyText
    )(DeleteTenantInput.apply)(DeleteTenantInput.unapply)
  }
}
