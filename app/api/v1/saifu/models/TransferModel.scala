package api.v1.saifu.models

import java.util.Date

import play.api.data.format._
import play.api.data.Form
import play.api.data.Forms._

case class CreateSaifuTransferInput(fromSaifuID: String, toSaifuID: String, amount: Long, comment: String, transactionDate: Date)

case class UpdateSaifuTransferInput(id: String, fromSaifuID: String, toSaifuID: String, amount: Long, comment: String, transactionDate: Date)

object SaifuTransfer {

  val date: Formatter[Date] = Formats.dateFormat("yyyy-MM-dd")

  val createSaifuTransferInput: Form[CreateSaifuTransferInput] = Form {
    mapping(
      "fromSaifuID" -> nonEmptyText(minLength = 36, maxLength = 36),
      "toSaifuID" -> nonEmptyText(minLength = 36, maxLength = 36),
      "amount" -> longNumber(min = 0),
      "comment" -> text,
      "transactionDate" -> of(date)
    )(CreateSaifuTransferInput.apply)(CreateSaifuTransferInput.unapply)
  }

  val updateSaifuTransferInput: Form[UpdateSaifuTransferInput] = Form {
    mapping(
      "id" -> nonEmptyText(minLength = 36, maxLength = 36),
      "fromSaifuID" -> nonEmptyText(minLength = 36, maxLength = 36),
      "toSaifuID" -> nonEmptyText(minLength = 36, maxLength = 36),
      "amount" -> longNumber(min = 0),
      "comment" -> text,
      "transactionDate" -> of(date)
    )(UpdateSaifuTransferInput.apply)(UpdateSaifuTransferInput.unapply)
  }

}
