package api.v1.saifu.resourcehandlers

import api.v1.saifu.models.{CreateSaifuInput, UpdateSaifuInput}
import com.saifu_mlm.engine.saifu.{Saifu, SaifuDAO}
import javax.inject.Inject
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class SaifuResource(
    id: String,
    subCategoryID: String,
    name: String,
    explain: String,
    initialBalance: Long,
    currentBalance: Long
)

object SaifuResource {
  implicit val format: Format[SaifuResource] = Json.format
}

class SaifuResourceHandler @Inject() (saifuDAO: SaifuDAO)(implicit ec: ExecutionContext) {

  def lookup(userID: String, saifuID: String)(implicit mc: MarkerContext): Future[Option[SaifuResource]] = {
    saifuDAO
      .lookup(userID, saifuID)
      .map(mayBeItem =>
        mayBeItem.map(item =>
          SaifuResource(item.id, item.subCategoryID, item.name, item.explain, item.initialBalance, item.currentBalance)
        )
      )
  }

  def all(userID: String)(implicit mc: MarkerContext): Future[Seq[SaifuResource]] = {
    saifuDAO
      .all(userID)
      .map(items =>
        items.map(item =>
          SaifuResource(item.id, item.subCategoryID, item.name, item.explain, item.initialBalance, item.currentBalance)
        )
      )
  }

  def create(userID: String, createSaifuInput: CreateSaifuInput)(implicit
      mc: MarkerContext
  ): Future[Int] = {
    saifuDAO
      .create(
        Saifu(
          subCategoryID = createSaifuInput.subCategoryID,
          userID = userID,
          name = createSaifuInput.name,
          explain = createSaifuInput.explain,
          initialBalance = createSaifuInput.initialBalance,
          currentBalance = createSaifuInput.initialBalance
        )
      )
  }

  def update(userID: String, updateSaifuInput: UpdateSaifuInput)(implicit mc: MarkerContext): Future[Int] = {
    saifuDAO.update(
      Saifu(
        updateSaifuInput.saifuID,
        updateSaifuInput.subCategoryID,
        userID,
        updateSaifuInput.name,
        updateSaifuInput.explain,
        updateSaifuInput.initialBalance
      )
    )
  }

}
