package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.{string2UUID, ERROR_CODE}
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

  override def lookup(userID: String, id: String): Future[Option[Saifu]] = {
    db.run(
      MSaifu
        .join(TSaifuHistories)
        .on(_.id === _.saifuId)
        .filter {
          case (t1, t2) =>
            !t1.deleteFlag &&
            !t2.deleteFlag &&
            !t1.saifuSubCategoryId.isEmpty &&
            t1.userId === string2UUID(userID) &&
            t1.id === string2UUID(id) &&
            t2.initialRecordFlag
        }
        .map {
          case (t1, t2) =>
            (
              t1.id.toString,
              t1.saifuSubCategoryId.toString,
              t1.userId.toString,
              t1.name,
              t1.explain.toString,
              t2.balance
            ) <> (Saifu.tupled, Saifu.unapply)
        }
        .result
        .headOption
    )
  }

  override def all(userID: String): Future[Seq[Saifu]] = {
    db.run(
      MSaifu
        .join(TSaifuHistories)
        .on(_.id === _.saifuId)
        .filter {
          case (t1, t2) =>
            !t1.deleteFlag &&
            !t2.deleteFlag &&
            !t1.saifuSubCategoryId.isEmpty &&
            t1.userId === string2UUID(userID) &&
            t2.initialRecordFlag
        }
        .map {
          case (t1, t2) =>
            (
              t1.id.toString,
              t1.saifuSubCategoryId.toString,
              t1.userId.toString,
              t1.name,
              t1.explain.toString,
              t2.balance
            ) <> (Saifu.tupled, Saifu.unapply)
        }
        .result
    )
  }

  override def create(saifu: Saifu): Future[Int] = {
    db.run(
      // Duplication Check
      MSaifu
        .filter(item =>
          (!item.deleteFlag)
          && (item.userId === string2UUID(saifu.userID)
          && (item.saifuSubCategoryId === saifu.subCategoryID.toInt
          && (item.name === saifu.name)))
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
                    createdAt = DateTime.now()
                  )
            ).flatMap(
              // Insert Initial Saifu History Record
              saifuID =>
                TSaifuHistories +=
                  TSaifuHistoriesRow(
                    id = UUID.randomUUID(),
                    saifuId = Option(saifuID),
                    initialRecordFlag = true,
                    income = 0,
                    outcome = 0,
                    balance = saifu.balance,
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
      MSaifu.filter { item =>
        !item.deleteFlag &&
        item.userId === string2UUID(saifu.userID) &&
        item.id === string2UUID(saifu.id)
      }

    val queryHistory =
      TSaifuHistories.filter { item =>
        !item.deleteFlag &&
        item.initialRecordFlag &&
        item.saifuId === string2UUID(saifu.id)
      }

    db.run(
      querySaifu.exists.result.flatMap {
        case false => DBIO.successful(ERROR_CODE)
        case true  =>
          // Update MSaifu
          querySaifu
            .map(target => (target.saifuSubCategoryId, target.name, target.explain))
            .update(Option(saifu.subCategoryID.toInt), saifu.name, Option(saifu.explain))
            .andThen {
              // Get Before Update Value
              queryHistory
                .map(_.balance)
                .result
                .head
                .flatMap { initialBalance =>
                  val delta = saifu.balance - initialBalance
                  TSaifuHistories
                    .filter(target => !target.deleteFlag && target.saifuId === string2UUID(saifu.id))
                    .map(_.balance)
                    .update(+delta)
                }
            }
      }.transactionally
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

}
