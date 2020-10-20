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
  * @param income Income Amount
  * @param outcome Outcome Amount
  * @param balance Current Balance
  * @param transactionDate Transaction Date
  */
case class SaifuHistory(
    id: String,
    tenantName: String,
    userName: String,
    saifuName: String,
    income: Long = 0,
    outcome: Long = 0,
    balance: Long = 0,
    transactionDate: DateTime
)
