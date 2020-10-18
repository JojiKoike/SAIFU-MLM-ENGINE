package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.{string2UUID, ERROR_CODE, UPDATE_SUCCESS_CODE}
import com.saifu_mlm.engine.saifu.{Saifu, SaifuDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.Inject
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class SlickSaifuDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends SaifuDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryByUserID =
    (userID: Rep[UUID]) => MSaifu.filter(item => !item.deleteFlag && item.userId === userID)

  override def lookup(userID: String, saifuID: String): Future[Option[Saifu]] = {
    db.run(
        queryByUserID(string2UUID(userID))
          .filter(_.id === string2UUID(saifuID))
          .result
          .headOption
      )
      .map(mayBeRow => mayBeRow.map(rowToCase))
  }

  override def all(userID: String): Future[Seq[Saifu]] = {
    db.run(
        queryByUserID(string2UUID(userID)).result
      )
      .map(results => results.map(rowToCase))
  }

  override def create(saifu: Saifu): Future[Int] = {
    db.run(
      // Duplication Check
      queryByUserID(string2UUID(saifu.userID))
        .filter(item =>
          item.saifuSubCategoryId === saifu.subCategoryID.toInt &&
          item.name === saifu.name
        )
        .exists
        .result
        .flatMap {
          case true => DBIO.successful(ERROR_CODE) // Already Exists
          case false =>
            (
              // Insert New Saifu Record
              MSaifu returning MSaifu.map(_.id)
                += MSaifuRow(
                    id = UUID.randomUUID(),
                    userId = Option(string2UUID(saifu.userID)),
                    saifuSubCategoryId = Option(saifu.subCategoryID.toInt),
                    name = saifu.name,
                    explain = Option(saifu.explain),
                    initialBalance = saifu.initialBalance,
                    currentBalance = saifu.currentBalance,
                    createdAt = DateTime.now()
                  )
            ).flatMap(
              // Insert Initial Saifu History Record
              saifuID =>
                TSaifuHistories +=
                  TSaifuHistoriesRow(
                    id = UUID.randomUUID(),
                    saifuId = Option(saifuID),
                    income = 0L,
                    outcome = 0L,
                    balance = saifu.currentBalance,
                    transactionDate = DateTime.now(),
                    createdAt = DateTime.now()
                  )
            )
        }
        .transactionally
    )
  }

  override def update(saifu: Saifu): Future[Int] = {
    val querySaifu =
      (userID: String, saifuID: String) =>
        MSaifu.filter { item =>
          !item.deleteFlag &&
          item.userId === string2UUID(userID)
          item.id === string2UUID(saifuID)
        }

    db.run(
      querySaifu(saifu.userID, saifu.id).exists.result.flatMap {
        case false => DBIO.successful(ERROR_CODE)
        case true  =>
          // Calc Difference
          var delta: Long = 0
          querySaifu(saifu.userID, saifu.id)
            .map(item => (item.initialBalance, item.currentBalance))
            .result
            .head
            .flatMap {
              item =>
                delta = saifu.initialBalance - item._1
                // Update MSaifu
                querySaifu(saifu.userID, saifu.id)
                  .map(target =>
                    (
                      target.saifuSubCategoryId,
                      target.name,
                      target.explain,
                      target.initialBalance,
                      target.currentBalance
                    )
                  )
                  .update(
                    (
                      Option(saifu.subCategoryID.toInt), // New SubCategoryID
                      saifu.name,                        // New Name
                      Option(saifu.explain),             // New Explain
                      saifu.initialBalance,              // New Initial Balance
                      item._2 + delta                    // New Current Balance
                    )
                  )
                  .andThen {
                    if (delta != 0L) {
                      sqlu"""update t_saifu_histories 
                            set 
                            balance = balance + $delta 
                            where 
                            delete_flag = false 
                            and saifu_id::text = ${saifu.id}"""
                    } else {
                      DBIO.successful(UPDATE_SUCCESS_CODE)
                    }
                  }
            }
      }.transactionally
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private val rowToCase = (mSaifuRow: MSaifuRow) =>
    Saifu(
      mSaifuRow.id.toString,
      mSaifuRow.saifuSubCategoryId.getOrElse("").toString,
      mSaifuRow.userId.getOrElse("").toString,
      mSaifuRow.name,
      mSaifuRow.explain.getOrElse(""),
      mSaifuRow.initialBalance,
      mSaifuRow.currentBalance
    )

}
