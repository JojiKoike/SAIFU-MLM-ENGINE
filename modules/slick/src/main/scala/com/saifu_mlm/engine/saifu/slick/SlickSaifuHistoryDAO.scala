package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.engine.saifu.{SaifuHistory, SaifuHistoryDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSaifuHistoryDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends SaifuHistoryDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  override def lookup(
      userID: String,
      saifuID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = ???

  override def allTenant(
      tenantID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = ???

  override def allUser(
      userID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = ???

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }
}
