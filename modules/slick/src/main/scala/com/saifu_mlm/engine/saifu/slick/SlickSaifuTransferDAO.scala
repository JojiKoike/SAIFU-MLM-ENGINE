package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.engine.saifu.{SaifuTransfer, SaifuTransferDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSaifuTransferDAO @Inject() (db: Database)(implicit ec: ExecutionContext)
    extends SaifuTransferDAO
    with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryByUserID = (userID: Rep[UUID]) =>
    TSaifuTransfers
      .join(MSaifu)
      .on(_.fromSaifuId === _.id)
      .join(MUsers)
      .on(_._2.userId === _.id)
      .join(MSaifu)
      .on(_._1._1.toSaifuId === _.id)
      .join(MUsers)
      .on(_._2.userId === _.id)
      .filter(item =>
        !item._1._1._1._1.deleteFlag &&
        item._1._1._2.id === userID ||
        item._2.id === userID
      )

  override def lookup(userID: String, saifuID: String): Future[Seq[SaifuTransfer]] = {
    db.run(
        queryByUserID(string2UUID(userID))
          .filter(item =>
            item._1._1._1._1.fromSaifuId === string2UUID(saifuID) ||
            item._1._1._1._1.toSaifuId === string2UUID(saifuID)
          )
          .result
      )
      .map(items =>
        items.map((item: ((((TSaifuTransfersRow, MSaifuRow), MUsersRow), MSaifuRow), MUsersRow)) =>
          SaifuTransfer(
            item._1._1._1._1.id.toString,
            item._1._1._1._1.fromSaifuId.getOrElse("").toString,
            item._1._1._1._2.name,
            item._1._1._2.name,
            item._1._1._1._1.toSaifuId.getOrElse("").toString,
            item._1._2.name,
            item._2.name,
            item._1._1._1._1.amount,
            item._1._1._1._1.comment.getOrElse(""),
            item._1._1._1._1.transactionDate
          )
        )
      )
  }

  override def all(userID: String): Future[Seq[SaifuTransfer]] = {
    db.run(
        queryByUserID(string2UUID(userID)).result
      )
      .map(items =>
        items.map((item: ((((TSaifuTransfersRow, MSaifuRow), MUsersRow), MSaifuRow), MUsersRow)) =>
          SaifuTransfer(
            item._1._1._1._1.id.toString,
            item._1._1._1._1.fromSaifuId.getOrElse("").toString,
            item._1._1._1._2.name,
            item._1._1._2.name,
            item._1._1._1._1.toSaifuId.getOrElse("").toString,
            item._1._2.name,
            item._2.name,
            item._1._1._1._1.amount,
            item._1._1._1._1.comment.getOrElse(""),
            item._1._1._1._1.transactionDate
          )
        )
      )
  }

  override def create(saifuTransfer: SaifuTransfer): Future[Int] = {

    val mSaifuQueryByID =
      (saifuID: Rep[UUID]) => MSaifu.filter(item => !item.deleteFlag && item.id === saifuID)

    var newFromSaifuCurrentBalance: Long = 0
    var newToSaifuCurrentBalance: Long   = 0
    db.run(
      (
        // Create New Saifu Transfer Record
        TSaifuTransfers returning TSaifuTransfers.map(_.id)
          += TSaifuTransfersRow(
              id = UUID.randomUUID(),
              fromSaifuId = Option(string2UUID(saifuTransfer.fromSaifuID)),
              toSaifuId = Option(string2UUID(saifuTransfer.toSaifuID)),
              amount = saifuTransfer.amount,
              comment = Option(saifuTransfer.comment),
              transactionDate = saifuTransfer.transactionDate,
              createdAt = DateTime.now()
            )
      ).flatMap { saifuTransferID =>
        // Get From Saifu current balance
        mSaifuQueryByID(string2UUID(saifuTransfer.fromSaifuID))
          .map(_.currentBalance)
          .result
          .head
          .flatMap { orgCurrentBalance =>
            // Update From Saifu current balance
            newFromSaifuCurrentBalance = orgCurrentBalance - saifuTransfer.amount
            mSaifuQueryByID(string2UUID(saifuTransfer.fromSaifuID))
              .map(target => target.currentBalance)
              .update(orgCurrentBalance - saifuTransfer.amount)
          }
          .andThen {
            // Create From Saifu History Record
            TSaifuHistories
              .map(item => (item.saifuId, item.saifuTransferId, item.outcome, item.balance)) +=
              (Option(string2UUID(saifuTransfer.fromSaifuID)),
              Option(saifuTransferID),
              saifuTransfer.amount,
              newFromSaifuCurrentBalance)
          }
          .andThen {
            // Get To Saifu current balance
            mSaifuQueryByID(string2UUID(saifuTransfer.toSaifuID))
              .map(_.currentBalance)
              .result
              .head
              .flatMap { orgCurrentBalance =>
                newToSaifuCurrentBalance = orgCurrentBalance + saifuTransfer.amount
                // Update To Saifu Current Balance
                mSaifuQueryByID(string2UUID(saifuTransfer.toSaifuID))
                  .map(target => target.currentBalance)
                  .update(newToSaifuCurrentBalance)
              }
              .andThen {
                // Create To Saifu History Record
                TSaifuHistories
                  .map(item => (item.saifuId, item.saifuTransferId, item.income, item.balance)) +=
                  (Option(string2UUID(saifuTransfer.toSaifuID)),
                  Option(saifuTransferID),
                  saifuTransfer.amount,
                  newToSaifuCurrentBalance)
              }
          }
      }.transactionally
    )
  }

  override def update(saifuTransfer: SaifuTransfer): Future[Int] = ???

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

}
