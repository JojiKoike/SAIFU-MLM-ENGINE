package com.saifu_mlm.engine.saifu.slick

import java.sql.Timestamp
import java.util.UUID

import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.engine.saifu.{SaifuTransfer, SaifuTransferDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import java.sql.Types.VARCHAR

import slick.ast.BaseTypedType
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{JdbcProfile, JdbcType, SetParameter}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSaifuTransferDAO @Inject() (db: Database)(implicit ec: ExecutionContext)
    extends SaifuTransferDAO
    with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  implicit val jodatimeColumnType: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[DateTime, Timestamp](
      { jodatime => new Timestamp(jodatime.getMillis) },
      { sqltime => new DateTime(sqltime.getTime) }
    )
  implicit val dateTimeSetter: AnyRef with SetParameter[DateTime] = SetParameter[DateTime] {
    case (dateTime, params) => params.setTimestamp(new Timestamp(dateTime.getMillis))
  }
  implicit val uuidSetter: AnyRef with SetParameter[Option[UUID]] = SetParameter[Option[UUID]] {
    case (Some(uuid), params) => params.setString(uuid.toString)
    case (None, params)       => params.setNull(VARCHAR)
  }

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

  private val mSaifuQueryByID =
    (saifuID: Rep[UUID]) => MSaifu.filter(item => !item.deleteFlag && item.id === saifuID)

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
            TSaifuHistories +=
              TSaifuHistoriesRow(
                id = UUID.randomUUID(),
                saifuId = Option(string2UUID(saifuTransfer.fromSaifuID)),
                saifuTransferId = Option(saifuTransferID),
                income = 0L,
                outcome = saifuTransfer.amount,
                balance = newFromSaifuCurrentBalance,
                transactionDate = saifuTransfer.transactionDate,
                createdAt = DateTime.now()
              )
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
                TSaifuHistories +=
                  TSaifuHistoriesRow(
                    id = UUID.randomUUID(),
                    saifuId = Option(string2UUID(saifuTransfer.toSaifuID)),
                    saifuTransferId = Option(saifuTransferID),
                    income = saifuTransfer.amount,
                    outcome = 0L,
                    balance = newToSaifuCurrentBalance,
                    transactionDate = saifuTransfer.transactionDate,
                    createdAt = DateTime.now()
                  )
              }
          }
      }.transactionally
    )
  }

  override def update(saifuTransfer: SaifuTransfer): Future[Int] =
    db.run(
      TSaifuTransfers
        .filter(item =>
          !item.deleteFlag &&
          item.id === string2UUID(saifuTransfer.id)
        )
        .result
        .head
        .flatMap { oldSaifuTransfer =>
          // Rollback Stage
          // Delete From-Saifu History Record
          TSaifuHistories
            .filter(target =>
              !target.deleteFlag &&
              target.saifuTransferId === string2UUID(saifuTransfer.id) &&
              target.saifuId === oldSaifuTransfer.fromSaifuId
            )
            .map(_.deleteFlag)
            .update(true)
            .andThen {
              // Rollback From Saifu Current Balance
              mSaifuQueryByID(oldSaifuTransfer.fromSaifuId.get)
                .map(_.currentBalance)
                .result
                .head
                .flatMap { orgCurrentBalance =>
                  mSaifuQueryByID(oldSaifuTransfer.fromSaifuId.get)
                    .map(_.currentBalance)
                    .update(orgCurrentBalance + oldSaifuTransfer.amount)
                }
            }
            .andThen {
              // Rollback From Saifu History Records After Transfer Transaction Date
              sqlu"update t_saifu_histories set balance = balance + ${oldSaifuTransfer.amount} where delete_flag = false and saifu_id::text = ${oldSaifuTransfer.fromSaifuId} and transaction_date > ${oldSaifuTransfer.transactionDate}"
            }
            .andThen {
              // Delete To-Saifu History Record
              TSaifuHistories
                .filter(target =>
                  !target.deleteFlag &&
                  target.saifuTransferId === string2UUID(saifuTransfer.id) &&
                  target.saifuId === oldSaifuTransfer.toSaifuId
                )
                .map(_.deleteFlag)
                .update(true)
            }
            .andThen {
              // Rollback To Saifu Current Balance
              mSaifuQueryByID(oldSaifuTransfer.toSaifuId.get)
                .map(_.currentBalance)
                .result
                .head
                .flatMap { orgCurrentBalance =>
                  mSaifuQueryByID(oldSaifuTransfer.toSaifuId.get)
                    .map(_.currentBalance)
                    .update(orgCurrentBalance - oldSaifuTransfer.amount)
                }
            }
            .andThen {
              // Rollback To-Saifu History Record
              sqlu"update t_saifu_histories set balance = balance - ${oldSaifuTransfer.amount} where delete_flag = false and saifu_id::text = ${oldSaifuTransfer.toSaifuId.get.toString} and transaction_date > ${oldSaifuTransfer.transactionDate}"
            }
            .andThen {
              // Update Stage
              // Update From-Saifu Current Balance and Create History Record
              mSaifuQueryByID(string2UUID(saifuTransfer.fromSaifuID))
                .map(_.currentBalance)
                .result
                .head
                .flatMap {
                  orgCurrentBalance =>
                    // Update New From-Saifu Current Balance
                    mSaifuQueryByID(string2UUID(saifuTransfer.fromSaifuID))
                      .map(_.currentBalance)
                      .update(orgCurrentBalance - saifuTransfer.amount)
                      .andThen {
                        // Create New From-Saifu History Record
                        TSaifuHistories +=
                          TSaifuHistoriesRow(
                            id = UUID.randomUUID(),
                            saifuId = Option(string2UUID(saifuTransfer.fromSaifuID)),
                            saifuTransferId = Option(string2UUID(saifuTransfer.id)),
                            income = 0L,
                            outcome = saifuTransfer.amount,
                            balance = orgCurrentBalance - saifuTransfer.amount,
                            transactionDate = saifuTransfer.transactionDate,
                            createdAt = DateTime.now()
                          )
                      }
                      .andThen {
                        sqlu"update t_saifu_histories set balance = balance - ${saifuTransfer.amount} where delete_flag = false and saifu_id::text = ${saifuTransfer.fromSaifuID} and transaction_date > ${saifuTransfer.transactionDate}"
                      }
                }
            }
            .andThen {
              // Update From-Saifu Current Balance and Create History Record
              mSaifuQueryByID(string2UUID(saifuTransfer.toSaifuID))
                .map(_.currentBalance)
                .result
                .head
                .flatMap {
                  orgCurrentBalance =>
                    // Update New From-Saifu Current Balance
                    mSaifuQueryByID(string2UUID(saifuTransfer.toSaifuID))
                      .map(_.currentBalance)
                      .update(orgCurrentBalance + saifuTransfer.amount)
                      .andThen {
                        // Create New From-Saifu History Record
                        TSaifuHistories +=
                          TSaifuHistoriesRow(
                            id = UUID.randomUUID(),
                            saifuId = Option(string2UUID(saifuTransfer.toSaifuID)),
                            saifuTransferId = Option(string2UUID(saifuTransfer.id)),
                            income = saifuTransfer.amount,
                            outcome = 0L,
                            balance = orgCurrentBalance + saifuTransfer.amount,
                            transactionDate = saifuTransfer.transactionDate,
                            createdAt = DateTime.now()
                          )
                      }
                      .andThen {
                        sqlu"update t_saifu_histories set balance = balance + ${saifuTransfer.amount} where delete_flag = false and saifu_id::text = ${saifuTransfer.toSaifuID} and transaction_date > ${saifuTransfer.transactionDate}"
                      }
                }
            }
            .andThen {
              // Update SaifuTransfer Record
              sqlu"update t_saifu_transfers set from_saifu_id = ${saifuTransfer.fromSaifuID}, to_saifu_id = ${saifuTransfer.toSaifuID}, amount = ${saifuTransfer.amount}, comment = ${saifuTransfer.comment}, transaction_date = ${saifuTransfer.transactionDate} where delete_flag = false and id::text = ${saifuTransfer.id}"
            }
            .transactionally
        }
    )

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

}
