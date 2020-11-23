package api.v1.saifu.resourcehandlers

import api.v1.saifu.models.{CreateSaifuTransferInput, UpdateSaifuTransferInput}
import com.saifu_mlm.engine.saifu.{SaifuTransfer, SaifuTransferDAO}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class SaifuTransferResource(
    id: String,
    fromSaifuName: String,
    fromSaifuUserName: String,
    toSaifuName: String,
    toSaifuUserName: String,
    amount: Long,
    comment: String,
    transactionDate: DateTime
                                )

object SaifuTransferResource {
  implicit val format: Format[SaifuTransferResource] = Json.format
}


class TransferResourceHandler @Inject() (saifuTransferDAO: SaifuTransferDAO)(implicit ec: ExecutionContext) {

  /**
   * Lookup SaifuTransferRecords of Specified Saifu
   * @param userID UserID
   * @param saifuID SaifuID
   * @param mc MarkerContext
   * @return SaifuTransferResource List
   */
  def lookup(userID: String, saifuID: String)(implicit mc: MarkerContext): Future[Seq[SaifuTransferResource]] = {
    saifuTransferDAO
      .lookup(userID, saifuID)
      .map(items =>
        items.map(item =>
          SaifuTransferResource(
            item.id,
            item.fromSaifuName,
            item.fromSaifuUserName,
            item.toSaifuName,
            item.toSaifuUserName,
            item.amount,
            item.comment,
            item.transactionDate
          )
        ))
  }


  def all (userID: String)(implicit mc: MarkerContext): Future[Seq[SaifuTransferResource]] = {
    saifuTransferDAO
      .all(userID)
      .map(items =>
        items.map(item =>
          SaifuTransferResource(
            item.id,
            item.fromSaifuName,
            item.fromSaifuUserName,
            item.toSaifuName,
            item.toSaifuUserName,
            item.amount,
            item.comment,
            item.transactionDate
          )
        )
      )
  }

  def create(createSaifuTransferInput: CreateSaifuTransferInput)(implicit mc: MarkerContext): Future[Int] = {
    saifuTransferDAO.create(
      SaifuTransfer(
        fromSaifuID = createSaifuTransferInput.fromSaifuID,
        toSaifuID = createSaifuTransferInput.toSaifuID,
        amount = createSaifuTransferInput.amount,
        comment = createSaifuTransferInput.comment,
        transactionDate = new DateTime(createSaifuTransferInput.transactionDate)
      )
    )
  }

  def update(updateSaifuTransferInput: UpdateSaifuTransferInput)(implicit mc: MarkerContext): Future[Int] = {
    saifuTransferDAO.update(
      SaifuTransfer(
        id = updateSaifuTransferInput.id,
        fromSaifuID = updateSaifuTransferInput.fromSaifuID,
        toSaifuID = updateSaifuTransferInput.toSaifuID,
        amount = updateSaifuTransferInput.amount,
        comment = updateSaifuTransferInput.comment,
        transactionDate = new DateTime(updateSaifuTransferInput.transactionDate)
      )
    )
  }


}
