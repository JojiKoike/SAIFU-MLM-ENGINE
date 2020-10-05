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

  override def lookup(userID: String, id: String): Future[Option[Saifu]] = ???

  override def all(userID: String): Future[Seq[Saifu]] = ???

  override def create(saifu: Saifu): Future[Int] = {
    db.run(
      (
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
                        income = 0,
                        outcome = 0,
                        balance = saifu.currentBalance.toLong,
                        transactionDate = DateTime.now(),
                        createdAt = DateTime.now()
                      )
                )
              )
          }
        )
        .transactionally
    )
  }

  override def update(saifu: Saifu): Future[Int] = ???

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

}
