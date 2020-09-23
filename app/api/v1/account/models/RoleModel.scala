package api.v1.account.models

import play.api.data.Form
import play.api.data.Forms._

// Role Input Model
case class CreateRoleInput(name: String, explain: String)
case class UpdateRoleInput(id: String, name: String, explain: String)
case class DeleteRoleInput(id: String)

object Role {
  val createRoleInput: Form[CreateRoleInput] = Form {
    mapping(
      "name"    -> nonEmptyText,
      "explain" -> text
    )(CreateRoleInput.apply)(CreateRoleInput.unapply)
  }

  val updateRoleInput: Form[UpdateRoleInput] = Form {
    mapping(
      "id"      -> nonEmptyText,
      "name"    -> nonEmptyText,
      "explain" -> text
    )(UpdateRoleInput.apply)(UpdateRoleInput.unapply)
  }

  val deleteRoleInput: Form[DeleteRoleInput] = Form {
    mapping(
      "id" -> nonEmptyText
    )(DeleteRoleInput.apply)(DeleteRoleInput.unapply)
  }
}
