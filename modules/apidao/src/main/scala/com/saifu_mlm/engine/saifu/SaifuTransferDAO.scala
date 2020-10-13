package com.saifu_mlm.engine.saifu

import org.joda.time.DateTime

import scala.concurrent.Future

trait SaifuTransferDAO {

  def lookup(userID: String, saifuID: String): Future[Seq[SaifuTransfer]]

  def all(userID: String): Future[Seq[SaifuTransfer]]

  def create(saifuTransfer: SaifuTransfer): Future[Int]

  def update(saifuTransfer: SaifuTransfer): Future[Int]

  def close(): Future[Unit]

}

case class SaifuTransfer(
    id: String = "",
    fromSaifuID: String,
    fromSaifuName: String = "",
    fromSaifuUserName: String = "",
    toSaifuID: String,
    toSaifuName: String = "",
    toSaifuUserName: String = "",
    amount: Long,
    comment: String,
    transactionDate: DateTime
)
