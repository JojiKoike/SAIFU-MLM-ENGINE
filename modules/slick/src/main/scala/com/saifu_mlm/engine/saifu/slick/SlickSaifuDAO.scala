package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.{string2UUID, ERROR_CODE}
import com.saifu_mlm.engine.saifu.{Saifu, SaifuDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.Inject
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
          case true  => DBIO.successful(ERROR_CODE) // Already Exists
          case false =>
            // Insert New Saifu Record
            MSaifu.map(item => (item.userId, item.saifuSubCategoryId, item.name, item.explain, item.initialBalance)) +=
              (Option(string2UUID(saifu.userID)), Option(saifu.subCategoryID.toInt), saifu.name, Option(
                saifu.explain
              ), saifu.initialBalance)
        }
        .transactionally
    )
  }

  override def update(saifu: Saifu): Future[Int] = {
    db.run(
      MSaifu
        .filter(_.id === string2UUID(saifu.id))
        .map(item => (item.saifuSubCategoryId, item.name, item.explain, item.initialBalance))
        .update((Option(saifu.subCategoryID.toInt), saifu.name, Option(saifu.explain), saifu.initialBalance))
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
      mSaifuRow.initialBalance
    )

}
