package api.v1.account.models

import play.api.data.Form
import play.api.data.Forms._

case class CreateUserInput(
    tenantId: String,
    roleId: String,
    loginId: String,
    name: String,
    password: String,
    eMail: String
)

case class UpdateUserInput(
    id: String,
    tenantId: String,
    roleId: String,
    loginId: String,
    name: String,
    password: String,
    eMail: String
)

case class DeleteUserInput(id: String)

case class LoginInput(loginID: String, password: String)

object User {
  val createUserInput: Form[CreateUserInput] = Form {
    mapping(
      "tenantId" -> nonEmptyText,
      "roleId"   -> nonEmptyText,
      "loginId"  -> nonEmptyText(maxLength = 50),
      "name"     -> nonEmptyText(maxLength = 20),
      "password" -> nonEmptyText(maxLength = 100),
      "eMail"    -> email
    )(CreateUserInput.apply)(CreateUserInput.unapply)
  }

  val updateUserInput: Form[UpdateUserInput] = Form {
    mapping(
      "id"       -> nonEmptyText,
      "tenantId" -> nonEmptyText,
      "roleId"   -> nonEmptyText,
      "loginId"  -> nonEmptyText(maxLength = 50),
      "name"     -> nonEmptyText(maxLength = 20),
      "password" -> nonEmptyText(maxLength = 100),
      "eMail"    -> email
    )(UpdateUserInput.apply)(UpdateUserInput.unapply)
  }

  val deleteUserInput: Form[DeleteUserInput] = Form {
    mapping(
      "id" -> nonEmptyText
    )(DeleteUserInput.apply)(DeleteUserInput.unapply)
  }

  val loginInput: Form[LoginInput] = Form {
    mapping(
      "loginID"  -> nonEmptyText(maxLength = 50),
      "password" -> nonEmptyText(maxLength = 100)
    )(LoginInput.apply)(LoginInput.unapply)
  }
}
