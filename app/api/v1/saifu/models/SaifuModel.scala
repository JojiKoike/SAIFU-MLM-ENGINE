package api.v1.saifu.models

import play.api.data.Form
import play.api.data.Forms._

case class CreateSaifuInput(subCategoryID: String, name: String, explain: String, initialBalance: Long)

case class UpdateSaifuInput(saifuID: String, subCategoryID: String, name: String, explain: String, initialBalance: Long)

object Saifu {

  val createSaifuInput: Form[CreateSaifuInput] = Form {
    mapping(
      "subCategoryID"  -> nonEmptyText,
      "name"           -> nonEmptyText,
      "explain"        -> text,
      "initialBalance" -> longNumber
    )(CreateSaifuInput.apply)(CreateSaifuInput.unapply)
  }

  val updateSaifuInput: Form[UpdateSaifuInput] = Form {
    mapping(
      "saifuID"        -> nonEmptyText(minLength = 36, maxLength = 36),
      "subCategoryID"  -> nonEmptyText,
      "name"           -> nonEmptyText,
      "explain"        -> text,
      "initialBalance" -> longNumber(min = 0)
    )(UpdateSaifuInput.apply)(UpdateSaifuInput.unapply)
  }

}
