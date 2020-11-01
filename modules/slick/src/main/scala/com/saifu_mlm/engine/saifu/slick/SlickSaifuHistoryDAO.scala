package com.saifu_mlm.engine.saifu.slick

import java.sql.Timestamp
import java.sql.Types.VARCHAR
import java.util.UUID

import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.engine.saifu.{SaifuHistory, SaifuHistoryDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{JdbcProfile, JdbcType, SetParameter}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSaifuHistoryDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends SaifuHistoryDAO with Tables {

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

  override def lookup(
      userID: String,
      saifuID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = {
    db.run(
      TSaifuHistories
        .filter(item =>
          !item.deleteFlag &&
          item.saifuId === string2UUID(saifuID) &&
          item.transactionDate >= transactionDateFrom &&
          item.transactionDate <= transactionDateTo
        )
        .join(
          MSaifu
            .filter(item =>
              !item.deleteFlag &&
              item.userId === Option(string2UUID(userID)) &&
              item.id === string2UUID(saifuID)
            )
        )
        .on(_.saifuId === _.id)
        .join(
          MUsers
            .filter(item =>
              !item.deleteFlag &&
              item.id === string2UUID(userID)
            )
        )
        .on(_._2.userId === _.id)
        .join(
          MTenants
            .filter(item => !item.deleteFlag)
        )
        .on(_._2.tenantId === _.id)
        .result
        .map(results =>
          results.map(item =>
            SaifuHistory(
              id = item._1._1._1.id.toString,
              tenantName = item._2.name,
              userName = item._1._2.name,
              saifuName = item._1._1._2.name,
              transactionAmount = item._1._1._1.transactionAmount,
              transactionDate = item._1._1._1.transactionDate
            )
          )
        )
    )
  }

  override def tenantAll(
      tenantID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = {
    db.run(
      MTenants
        .filter(item =>
          !item.deleteFlag &&
          item.id === string2UUID(tenantID)
        )
        .join(
          MUsers.filter(item =>
            !item.deleteFlag &&
            item.tenantId === Option(string2UUID(tenantID))
          )
        )
        .on(_.id === _.tenantId)
        .join(MSaifu.filter(!_.deleteFlag))
        .on(_._2.id === _.id)
        .join(TSaifuHistories.filter(!_.deleteFlag))
        .on(_._2.id === _.saifuId)
        .filter(item =>
          item._2.transactionDate >= transactionDateFrom &&
          item._2.transactionDate <= transactionDateTo
        )
        .sortBy(_._2.transactionDate.desc)
        .result
        .map(results =>
          results.map(item =>
            SaifuHistory(
              id = item._2.id.toString,
              tenantName = item._1._1._1.name,
              userName = item._1._1._2.name,
              saifuName = item._1._2.name,
              transactionAmount = item._2.transactionAmount,
              transactionDate = item._2.transactionDate
            )
          )
        )
    )
  }

  override def userAll(
      userID: String,
      transactionDateFrom: DateTime,
      transactionDateTo: DateTime
  ): Future[Seq[SaifuHistory]] = {
    db.run(
      MUsers
        .filter(item =>
          !item.deleteFlag &&
          item.id === string2UUID(userID)
        )
        .join(MTenants.filter(!_.deleteFlag))
        .on(_.tenantId === _.id)
        .join(
          MSaifu
            .filter(item =>
              !item.deleteFlag &&
              item.userId === Option(string2UUID(userID))
            )
        )
        .on(_._1.id === _.userId)
        .join(TSaifuHistories.filter(!_.deleteFlag))
        .on(_._2.id === _.saifuId)
        .filter(item =>
          item._2.transactionDate >= transactionDateFrom &&
          item._2.transactionDate <= transactionDateTo
        )
        .result
        .map(results =>
          results
            .map(item =>
              SaifuHistory(
                id = item._2.id.toString,
                tenantName = item._1._1._2.name,
                userName = item._1._1._1.name,
                saifuName = item._1._2.name,
                transactionAmount = item._2.transactionAmount,
                transactionDate = item._2.transactionDate
              )
            )
        )
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }
}
