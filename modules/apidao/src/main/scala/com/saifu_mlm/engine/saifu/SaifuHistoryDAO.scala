package com.saifu_mlm.engine.saifu

import org.joda.time.DateTime

import scala.concurrent.Future

trait SaifuHistoryDAO {

  def lookup(
      userID: String,
      saifuID: String,
      transactionDateFrom: DateTime = DateTime.now().minusYears(1),
      transactionDateTo: DateTime = DateTime.now()
  ): Future[Seq[SaifuHistory]]

  def tenantAll(
      tenantID: String,
      transactionDateFrom: DateTime = DateTime.now().minusYears(1),
      transactionDateTo: DateTime = DateTime.now()
  ): Future[Seq[SaifuHistory]]

  def userAll(
      userID: String,
      transactionDateFrom: DateTime = DateTime.now().minusYears(1),
      transactionDateTo: DateTime = DateTime.now()
  ): Future[Seq[SaifuHistory]]

  def close(): Future[Unit]
}

/**
  * SaifuHistory Entity
  * @param id SaifuHistoryID
  * @param tenantName TenantName
  * @param userName UserName
  * @param saifuName SaifuName
  * @param transactionAmount Transaction Amount
  * @param transactionDate Transaction Date
  */
case class SaifuHistory(
    id: String,
    tenantName: String,
    userName: String,
    saifuName: String,
    transactionAmount: Long,
    transactionDate: DateTime
)
