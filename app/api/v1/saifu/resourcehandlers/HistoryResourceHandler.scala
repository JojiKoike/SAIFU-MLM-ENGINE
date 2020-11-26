package api.v1.saifu.resourcehandlers

import com.saifu_mlm.engine.saifu.{SaifuHistory, SaifuHistoryDAO}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class SaifuHistoryResource(
    id: String,
    tenantName: String,
    userName: String,
    saifuName: String,
    transactionAmount: Long,
    transactionDate: DateTime
)

object SaifuHistoryResource {
  implicit val format: Format[SaifuHistoryResource] = Json.format
}

class SaifuHistoryResourceHandler @Inject() (
    saifuHistoryDAO: SaifuHistoryDAO
)(implicit ec: ExecutionContext) {

  def lookup(
      userID: String,
      saifuID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistoryResource]] = {
    saifuHistoryDAO
      .lookup(userID, saifuID, transactionDateFrom, transactionDateTo)
      .map(items => items.map(entityToResource))
  }

  def tenantAll(
      tenantID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistoryResource]] = {
    saifuHistoryDAO
      .tenantAll(tenantID, transactionDateFrom, transactionDateTo)
      .map(items => items.map(entityToResource))
  }

  def userAll(
      userID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistoryResource]] = {
    saifuHistoryDAO
      .userAll(userID, transactionDateFrom, transactionDateTo)
      .map(items => items.map(entityToResource))
  }

  private val entityToResource: SaifuHistory => SaifuHistoryResource =
    saifuHistory =>
      SaifuHistoryResource(
        saifuHistory.id,
        saifuHistory.tenantName,
        saifuHistory.userName,
        saifuHistory.saifuName,
        saifuHistory.transactionAmount,
        saifuHistory.transactionDate
      )

}
