package api.v1.saifu.models

import play.api.data.Form
import play.api.data.Forms._

case class CreateMainCategoryInput(name: String, explain: String)

case class UpdateMainCategoryInput(id: String, name: String, explain: String)

case class CreateSubCategoryInput(mainCategoryID: String, name: String, explain: String)

case class UpdateSubCategoryInput(id: String, mainCategoryID: String, name: String, explain: String)

object SaifuCategory {

  val createMainCategoryInput: Form[CreateMainCategoryInput] = Form {
    mapping(
      "name"    -> nonEmptyText(maxLength = 30),
      "explain" -> text
    )(CreateMainCategoryInput.apply)(CreateMainCategoryInput.unapply)
  }

  val updateMainCategoryInput: Form[UpdateMainCategoryInput] = Form {
    mapping(
      "id"      -> nonEmptyText,
      "name"    -> nonEmptyText(maxLength = 30),
      "explain" -> text
    )(UpdateMainCategoryInput.apply)(UpdateMainCategoryInput.unapply)
  }

  val createSubCategoryInput: Form[CreateSubCategoryInput] = Form {
    mapping(
      "mainCategoryID" -> nonEmptyText,
      "name"           -> nonEmptyText(maxLength = 50),
      "explain"        -> text
    )(CreateSubCategoryInput.apply)(CreateSubCategoryInput.unapply)
  }

  val updateSubCategoryInput: Form[UpdateSubCategoryInput] = Form {
    mapping(
      "id"             -> nonEmptyText,
      "mainCategoryID" -> nonEmptyText,
      "name"           -> nonEmptyText(maxLength = 50),
      "explain"        -> text
    )(UpdateSubCategoryInput.apply)(UpdateSubCategoryInput.unapply)
  }

}
