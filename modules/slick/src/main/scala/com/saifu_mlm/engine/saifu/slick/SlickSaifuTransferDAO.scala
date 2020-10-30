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
        {
          // Create From-Saifu History Record
          TSaifuHistories.map(item =>
            (item.saifuId, item.saifuTransferId, item.transactionAmount, item.transactionDate)
          ) += (Option(
            string2UUID(saifuTransfer.fromSaifuID)
          ), Option(saifuTransferID), -saifuTransfer.amount, saifuTransfer.transactionDate)
        }.andThen {
          // Create From-Saifu History Record
          TSaifuHistories.map(item =>
            (item.saifuId, item.saifuTransferId, item.transactionAmount, item.transactionDate)
          ) += (Option(
            string2UUID(saifuTransfer.toSaifuID)
          ), Option(saifuTransferID), saifuTransfer.amount, saifuTransfer.transactionDate)
        }
      }.transactionally
    )
  }

  override def update(saifuTransfer: SaifuTransfer): Future[Int] =
    db.run(
      // Update SaifuTransfer Transaction Record
      TSaifuTransfers
        .filter(item => item.id === string2UUID(saifuTransfer.id))
        .map(target => (target.fromSaifuId, target.toSaifuId, target.amount, target.comment, target.transactionDate))
        .update(
          Option(string2UUID(saifuTransfer.id)),
          Option(string2UUID(saifuTransfer.fromSaifuID)),
          saifuTransfer.amount,
          Option(saifuTransfer.comment),
          saifuTransfer.transactionDate
        )
        .andThen {
          // Update From Saifu History Record
          TSaifuHistories
            .filter(item =>
              item.saifuTransferId === string2UUID(saifuTransfer.id)
              && item.transactionAmount < 0L
            )
            .map(target => (target.saifuId, target.transactionAmount, target.transactionDate))
            .update(
              (Option(string2UUID(saifuTransfer.fromSaifuID)), -saifuTransfer.amount, saifuTransfer.transactionDate)
            )
        }
        .andThen {
          // Update To Saifu History Record
          TSaifuHistories
            .filter(item =>
              item.saifuTransferId === string2UUID(saifuTransfer.id)
              && item.transactionAmount > 0L
            )
            .map(target => (target.saifuId, target.transactionAmount, target.transactionDate))
            .update(
              (Option(string2UUID(saifuTransfer.toSaifuID)), saifuTransfer.amount, saifuTransfer.transactionDate)
            )
        }
        .transactionally
    )

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

}
